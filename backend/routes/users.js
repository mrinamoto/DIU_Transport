// backend/routes/users.js
// Admin-only user management (list, view, update role/status, delete).
// Creating a user goes through /api/auth/register — this file manages
// existing accounts.

const express = require('express');
const bcrypt = require('bcryptjs');
const db = require('../database');
const authenticateToken = require('../middleware/authMiddleware');
const requireRole = require('../middleware/roleMiddleware');
const { validateEmail, validateRole } = require('../utils/validators');
const { getPagination, buildPaginatedResponse } = require('../utils/pagination');

const router = express.Router();

function toPublicUser(row) {
  if (!row) return null;
  const { password_hash, ...safe } = row;
  return safe;
}

// ────────────────────────────────────────────────────────────
// GET /api/users  (admin only) — ?role=  ?search=  ?page=  ?limit=
// ────────────────────────────────────────────────────────────
router.get('/', authenticateToken, requireRole('admin'), (req, res) => {
  try {
    const { role, search } = req.query;
    const { page, limit, offset } = getPagination(req.query);
    const clauses = [];
    const params = {};

    if (role) { clauses.push('role = @role'); params.role = role; }
    if (search) {
      clauses.push('(full_name LIKE @search OR email LIKE @search OR student_id LIKE @search)');
      params.search = `%${search}%`;
    }
    const where = clauses.length ? `WHERE ${clauses.join(' AND ')}` : '';

    const total = db.prepare(`SELECT COUNT(*) AS c FROM users ${where}`).get(params).c;
    const rows = db.prepare(`SELECT * FROM users ${where} ORDER BY id DESC LIMIT @limit OFFSET @offset`)
      .all({ ...params, limit, offset });

    res.json({ status: 'success', ...buildPaginatedResponse(rows.map(toPublicUser), total, page, limit) });
  } catch (err) {
    console.error('GET /users error:', err);
    res.status(500).json({ status: 'error', message: 'Failed to fetch users.' });
  }
});

// ────────────────────────────────────────────────────────────
// GET /api/users/:id  (admin only)
// ────────────────────────────────────────────────────────────
router.get('/:id', authenticateToken, requireRole('admin'), (req, res) => {
  try {
    const row = db.prepare('SELECT * FROM users WHERE id = ?').get(req.params.id);
    if (!row) return res.status(404).json({ status: 'error', message: 'User not found.' });
    res.json({ status: 'success', data: toPublicUser(row) });
  } catch (err) {
    console.error('GET /users/:id error:', err);
    res.status(500).json({ status: 'error', message: 'Failed to fetch user.' });
  }
});

// ────────────────────────────────────────────────────────────
// PUT /api/users/:id  (admin only) — update profile fields, role, active status
// Admins cannot deactivate their own account (safety guard).
// ────────────────────────────────────────────────────────────
router.put('/:id', authenticateToken, requireRole('admin'), (req, res) => {
  try {
    const existing = db.prepare('SELECT * FROM users WHERE id = ?').get(req.params.id);
    if (!existing) return res.status(404).json({ status: 'error', message: 'User not found.' });

    const merged = { ...existing, ...req.body };

    if (req.body.email && !validateEmail(req.body.email)) {
      return res.status(400).json({ status: 'error', message: 'A valid email address is required.' });
    }
    if (req.body.role && !validateRole(req.body.role)) {
      return res.status(400).json({ status: 'error', message: 'Invalid role.' });
    }
    if (req.body.is_active === 0 && Number(req.params.id) === req.user.id) {
      return res.status(400).json({ status: 'error', message: 'You cannot deactivate your own admin account.' });
    }
    if (req.body.email && req.body.email !== existing.email) {
      const dup = db.prepare('SELECT id FROM users WHERE email = ? AND id != ?').get(req.body.email, req.params.id);
      if (dup) return res.status(409).json({ status: 'error', message: 'That email is already used by another account.' });
    }

    db.prepare(`
      UPDATE users SET full_name=@full_name, email=@email, role=@role, student_id=@student_id,
        phone=@phone, department=@department, semester=@semester, is_active=@is_active
      WHERE id=@id
    `).run({
      id: req.params.id,
      full_name: merged.full_name,
      email: merged.email,
      role: merged.role,
      student_id: merged.student_id,
      phone: merged.phone,
      department: merged.department,
      semester: merged.semester,
      is_active: merged.is_active === undefined ? existing.is_active : (merged.is_active ? 1 : 0),
    });

    const row = db.prepare('SELECT * FROM users WHERE id = ?').get(req.params.id);
    res.json({ status: 'success', message: 'User updated.', data: toPublicUser(row) });
  } catch (err) {
    console.error('PUT /users/:id error:', err);
    res.status(500).json({ status: 'error', message: 'Failed to update user.' });
  }
});

// ────────────────────────────────────────────────────────────
// PUT /api/users/:id/password  (admin only) — reset a user's password
// ────────────────────────────────────────────────────────────
router.put('/:id/password', authenticateToken, requireRole('admin'), (req, res) => {
  try {
    const { new_password } = req.body || {};
    if (!new_password || new_password.length < 8) {
      return res.status(400).json({ status: 'error', message: 'New password must be at least 8 characters long.' });
    }
    const existing = db.prepare('SELECT id FROM users WHERE id = ?').get(req.params.id);
    if (!existing) return res.status(404).json({ status: 'error', message: 'User not found.' });

    const password_hash = bcrypt.hashSync(new_password, 10);
    db.prepare('UPDATE users SET password_hash = ? WHERE id = ?').run(password_hash, req.params.id);

    res.json({ status: 'success', message: 'Password reset successfully.' });
  } catch (err) {
    console.error('PUT /users/:id/password error:', err);
    res.status(500).json({ status: 'error', message: 'Failed to reset password.' });
  }
});

// ────────────────────────────────────────────────────────────
// DELETE /api/users/:id  (admin only) — cannot delete your own account
// ────────────────────────────────────────────────────────────
router.delete('/:id', authenticateToken, requireRole('admin'), (req, res) => {
  try {
    if (Number(req.params.id) === req.user.id) {
      return res.status(400).json({ status: 'error', message: 'You cannot delete your own admin account.' });
    }
    const info = db.prepare('DELETE FROM users WHERE id = ?').run(req.params.id);
    if (info.changes === 0) return res.status(404).json({ status: 'error', message: 'User not found.' });
    res.json({ status: 'success', message: 'User deleted.' });
  } catch (err) {
    console.error('DELETE /users/:id error:', err);
    res.status(500).json({ status: 'error', message: 'Failed to delete user.' });
  }
});

module.exports = router;
