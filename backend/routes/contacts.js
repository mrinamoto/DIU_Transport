// backend/routes/contacts.js
// Emergency / support contacts. Public can view active contacts.
// Admins can add/edit/delete.

const express = require('express');
const db = require('../database');
const authenticateToken = require('../middleware/authMiddleware');
const requireRole = require('../middleware/roleMiddleware');
const { isNonEmptyString } = require('../utils/validators');

const router = express.Router();

// ────────────────────────────────────────────────────────────
// GET /api/contacts  (public) — active contacts only
// ────────────────────────────────────────────────────────────
router.get('/', (req, res) => {
  try {
    const rows = db.prepare('SELECT * FROM emergency_contacts WHERE is_active = 1 ORDER BY id').all();
    res.json({ status: 'success', count: rows.length, data: rows });
  } catch (err) {
    console.error('GET /contacts error:', err);
    res.status(500).json({ status: 'error', message: 'Failed to fetch contacts.' });
  }
});

// ────────────────────────────────────────────────────────────
// GET /api/contacts/all  (admin only) — includes inactive contacts
// ────────────────────────────────────────────────────────────
router.get('/all', authenticateToken, requireRole('admin'), (req, res) => {
  try {
    const rows = db.prepare('SELECT * FROM emergency_contacts ORDER BY id').all();
    res.json({ status: 'success', count: rows.length, data: rows });
  } catch (err) {
    console.error('GET /contacts/all error:', err);
    res.status(500).json({ status: 'error', message: 'Failed to fetch contacts.' });
  }
});

// ────────────────────────────────────────────────────────────
// POST /api/contacts  (admin only)
// ────────────────────────────────────────────────────────────
router.post('/', authenticateToken, requireRole('admin'), (req, res) => {
  try {
    const { name, role, phone, email, available_hours } = req.body || {};
    const errors = [];

    if (!isNonEmptyString(name)) errors.push('Name is required.');
    if (!isNonEmptyString(role)) errors.push('Role/title is required.');
    if (!isNonEmptyString(phone)) errors.push('Phone number is required.');

    if (errors.length) return res.status(400).json({ status: 'error', message: errors[0], errors });

    const info = db.prepare(`
      INSERT INTO emergency_contacts (name, role, phone, email, available_hours)
      VALUES (@name, @role, @phone, @email, @available_hours)
    `).run({ name, role, phone, email: email || null, available_hours: available_hours || null });

    const row = db.prepare('SELECT * FROM emergency_contacts WHERE id = ?').get(info.lastInsertRowid);
    res.status(201).json({ status: 'success', message: 'Contact added.', data: row });
  } catch (err) {
    console.error('POST /contacts error:', err);
    res.status(500).json({ status: 'error', message: 'Failed to add contact.' });
  }
});

// ────────────────────────────────────────────────────────────
// PUT /api/contacts/:id  (admin only)
// ────────────────────────────────────────────────────────────
router.put('/:id', authenticateToken, requireRole('admin'), (req, res) => {
  try {
    const existing = db.prepare('SELECT * FROM emergency_contacts WHERE id = ?').get(req.params.id);
    if (!existing) return res.status(404).json({ status: 'error', message: 'Contact not found.' });

    const merged = { ...existing, ...req.body };
    db.prepare(`
      UPDATE emergency_contacts SET name=@name, role=@role, phone=@phone, email=@email,
        available_hours=@available_hours, is_active=@is_active
      WHERE id=@id
    `).run({ id: req.params.id, ...merged, is_active: merged.is_active ? 1 : 0 });

    const row = db.prepare('SELECT * FROM emergency_contacts WHERE id = ?').get(req.params.id);
    res.json({ status: 'success', message: 'Contact updated.', data: row });
  } catch (err) {
    console.error('PUT /contacts/:id error:', err);
    res.status(500).json({ status: 'error', message: 'Failed to update contact.' });
  }
});

// ────────────────────────────────────────────────────────────
// DELETE /api/contacts/:id  (admin only)
// ────────────────────────────────────────────────────────────
router.delete('/:id', authenticateToken, requireRole('admin'), (req, res) => {
  try {
    const info = db.prepare('DELETE FROM emergency_contacts WHERE id = ?').run(req.params.id);
    if (info.changes === 0) return res.status(404).json({ status: 'error', message: 'Contact not found.' });
    res.json({ status: 'success', message: 'Contact deleted.' });
  } catch (err) {
    console.error('DELETE /contacts/:id error:', err);
    res.status(500).json({ status: 'error', message: 'Failed to delete contact.' });
  }
});

module.exports = router;
