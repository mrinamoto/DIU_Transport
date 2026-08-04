// backend/routes/notifications.js
// Users see notifications targeted at "all" or their specific role, plus
// whether they've read each one. Admins can broadcast/manage all notifications.

const express = require('express');
const db = require('../database');
const authenticateToken = require('../middleware/authMiddleware');
const requireRole = require('../middleware/roleMiddleware');
const { isNonEmptyString } = require('../utils/validators');

const router = express.Router();
const VALID_TYPES = ['info', 'warning', 'emergency', 'update'];

// ────────────────────────────────────────────────────────────
// GET /api/notifications  (any authenticated user) — role-relevant, active only
// ────────────────────────────────────────────────────────────
router.get('/', authenticateToken, (req, res) => {
  try {
    const rows = db.prepare(`
      SELECT n.*,
             CASE WHEN nr.id IS NULL THEN 0 ELSE 1 END AS is_read
      FROM notifications n
      LEFT JOIN notification_reads nr ON nr.notification_id = n.id AND nr.user_id = @userId
      WHERE n.is_active = 1 AND (n.target_role = 'all' OR n.target_role = @role)
      ORDER BY n.created_at DESC
    `).all({ userId: req.user.id, role: req.user.role });

    res.json({ status: 'success', count: rows.length, unread_count: rows.filter(r => !r.is_read).length, data: rows });
  } catch (err) {
    console.error('GET /notifications error:', err);
    res.status(500).json({ status: 'error', message: 'Failed to fetch notifications.' });
  }
});

// ────────────────────────────────────────────────────────────
// GET /api/notifications/all  (admin only) — every notification, incl. inactive
// ────────────────────────────────────────────────────────────
router.get('/all', authenticateToken, requireRole('admin'), (req, res) => {
  try {
    const rows = db.prepare(`
      SELECT n.*, u.full_name AS created_by_name
      FROM notifications n
      LEFT JOIN users u ON n.created_by = u.id
      ORDER BY n.created_at DESC
    `).all();
    res.json({ status: 'success', count: rows.length, data: rows });
  } catch (err) {
    console.error('GET /notifications/all error:', err);
    res.status(500).json({ status: 'error', message: 'Failed to fetch notifications.' });
  }
});

// ────────────────────────────────────────────────────────────
// POST /api/notifications/:id/read  (any authenticated user) — mark as read
// ────────────────────────────────────────────────────────────
router.post('/:id/read', authenticateToken, (req, res) => {
  try {
    const notif = db.prepare('SELECT id FROM notifications WHERE id = ?').get(req.params.id);
    if (!notif) return res.status(404).json({ status: 'error', message: 'Notification not found.' });

    db.prepare(`
      INSERT INTO notification_reads (notification_id, user_id)
      VALUES (?, ?)
      ON CONFLICT(notification_id, user_id) DO NOTHING
    `).run(req.params.id, req.user.id);

    res.json({ status: 'success', message: 'Notification marked as read.' });
  } catch (err) {
    console.error('POST /notifications/:id/read error:', err);
    res.status(500).json({ status: 'error', message: 'Failed to mark notification as read.' });
  }
});

// ────────────────────────────────────────────────────────────
// POST /api/notifications  (admin only) — create / broadcast
// ────────────────────────────────────────────────────────────
router.post('/', authenticateToken, requireRole('admin'), (req, res) => {
  try {
    const { title, message, type, target_role } = req.body || {};
    const errors = [];

    if (!isNonEmptyString(title)) errors.push('Title is required.');
    if (!isNonEmptyString(message)) errors.push('Message is required.');
    if (type && !VALID_TYPES.includes(type)) errors.push(`Type must be one of: ${VALID_TYPES.join(', ')}.`);

    if (errors.length) return res.status(400).json({ status: 'error', message: errors[0], errors });

    const info = db.prepare(`
      INSERT INTO notifications (title, message, type, target_role, created_by)
      VALUES (@title, @message, @type, @target_role, @created_by)
    `).run({
      title, message,
      type: type || 'info',
      target_role: target_role || 'all',
      created_by: req.user.id,
    });

    const row = db.prepare('SELECT * FROM notifications WHERE id = ?').get(info.lastInsertRowid);
    res.status(201).json({ status: 'success', message: 'Notification sent.', data: row });
  } catch (err) {
    console.error('POST /notifications error:', err);
    res.status(500).json({ status: 'error', message: 'Failed to create notification.' });
  }
});

// ────────────────────────────────────────────────────────────
// PUT /api/notifications/:id  (admin only) — edit or toggle active
// ────────────────────────────────────────────────────────────
router.put('/:id', authenticateToken, requireRole('admin'), (req, res) => {
  try {
    const existing = db.prepare('SELECT * FROM notifications WHERE id = ?').get(req.params.id);
    if (!existing) return res.status(404).json({ status: 'error', message: 'Notification not found.' });

    const merged = { ...existing, ...req.body };
    if (merged.type && !VALID_TYPES.includes(merged.type)) {
      return res.status(400).json({ status: 'error', message: `Type must be one of: ${VALID_TYPES.join(', ')}.` });
    }

    db.prepare(`
      UPDATE notifications SET title = @title, message = @message, type = @type,
        target_role = @target_role, is_active = @is_active
      WHERE id = @id
    `).run({
      id: req.params.id,
      title: merged.title,
      message: merged.message,
      type: merged.type,
      target_role: merged.target_role,
      is_active: merged.is_active ? 1 : 0,
    });

    const row = db.prepare('SELECT * FROM notifications WHERE id = ?').get(req.params.id);
    res.json({ status: 'success', message: 'Notification updated.', data: row });
  } catch (err) {
    console.error('PUT /notifications/:id error:', err);
    res.status(500).json({ status: 'error', message: 'Failed to update notification.' });
  }
});

// ────────────────────────────────────────────────────────────
// DELETE /api/notifications/:id  (admin only)
// ────────────────────────────────────────────────────────────
router.delete('/:id', authenticateToken, requireRole('admin'), (req, res) => {
  try {
    const info = db.prepare('DELETE FROM notifications WHERE id = ?').run(req.params.id);
    if (info.changes === 0) return res.status(404).json({ status: 'error', message: 'Notification not found.' });
    res.json({ status: 'success', message: 'Notification deleted.' });
  } catch (err) {
    console.error('DELETE /notifications/:id error:', err);
    res.status(500).json({ status: 'error', message: 'Failed to delete notification.' });
  }
});

module.exports = router;
