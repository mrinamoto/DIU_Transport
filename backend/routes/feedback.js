// backend/routes/feedback.js
// Any authenticated user can send feedback/report an issue.
// Admins can view and update its status.

const express = require('express');
const db = require('../database');
const authenticateToken = require('../middleware/authMiddleware');
const requireRole = require('../middleware/roleMiddleware');
const { isNonEmptyString } = require('../utils/validators');

const router = express.Router();
const VALID_STATUSES = ['new', 'reviewed', 'resolved'];

// ────────────────────────────────────────────────────────────
// POST /api/feedback  (any authenticated user)
// ────────────────────────────────────────────────────────────
router.post('/', authenticateToken, (req, res) => {
  try {
    const { subject, message } = req.body || {};
    const errors = [];
    if (!isNonEmptyString(subject)) errors.push('Subject is required.');
    if (!isNonEmptyString(message)) errors.push('Message is required.');
    if (errors.length) return res.status(400).json({ status: 'error', message: errors[0], errors });

    const info = db.prepare(`INSERT INTO feedback (user_id, subject, message) VALUES (?, ?, ?)`)
      .run(req.user.id, subject, message);

    const row = db.prepare('SELECT * FROM feedback WHERE id = ?').get(info.lastInsertRowid);
    res.status(201).json({ status: 'success', message: 'Thank you — your feedback has been sent to the transport office.', data: row });
  } catch (err) {
    console.error('POST /feedback error:', err);
    res.status(500).json({ status: 'error', message: 'Failed to submit feedback.' });
  }
});

// ────────────────────────────────────────────────────────────
// GET /api/feedback  (admin only)
// ────────────────────────────────────────────────────────────
router.get('/', authenticateToken, requireRole('admin'), (req, res) => {
  try {
    const rows = db.prepare(`
      SELECT f.*, u.full_name AS user_name, u.email AS user_email
      FROM feedback f LEFT JOIN users u ON f.user_id = u.id
      ORDER BY f.created_at DESC
    `).all();
    res.json({ status: 'success', count: rows.length, data: rows });
  } catch (err) {
    console.error('GET /feedback error:', err);
    res.status(500).json({ status: 'error', message: 'Failed to fetch feedback.' });
  }
});

// ────────────────────────────────────────────────────────────
// PUT /api/feedback/:id  (admin only) — update status
// ────────────────────────────────────────────────────────────
router.put('/:id', authenticateToken, requireRole('admin'), (req, res) => {
  try {
    const { status } = req.body || {};
    if (!VALID_STATUSES.includes(status)) {
      return res.status(400).json({ status: 'error', message: `Status must be one of: ${VALID_STATUSES.join(', ')}.` });
    }
    const info = db.prepare('UPDATE feedback SET status = ? WHERE id = ?').run(status, req.params.id);
    if (info.changes === 0) return res.status(404).json({ status: 'error', message: 'Feedback not found.' });

    const row = db.prepare('SELECT * FROM feedback WHERE id = ?').get(req.params.id);
    res.json({ status: 'success', message: 'Feedback updated.', data: row });
  } catch (err) {
    console.error('PUT /feedback/:id error:', err);
    res.status(500).json({ status: 'error', message: 'Failed to update feedback.' });
  }
});

module.exports = router;
