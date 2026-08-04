// backend/routes/billing.js
// A user can view their own billing history. Admins can view/create/update
// billing records for anyone.

const express = require('express');
const db = require('../database');
const authenticateToken = require('../middleware/authMiddleware');
const requireRole = require('../middleware/roleMiddleware');
const { getPagination, buildPaginatedResponse } = require('../utils/pagination');

const router = express.Router();
const VALID_STATUSES = ['paid', 'due', 'pending', 'overdue'];

const BILLING_SELECT = `
  SELECT b.*, u.full_name AS user_name, u.email AS user_email
  FROM billing b
  LEFT JOIN users u ON b.user_id = u.id
`;

// ────────────────────────────────────────────────────────────
// GET /api/billing/my  (any authenticated user)
// ────────────────────────────────────────────────────────────
router.get('/my', authenticateToken, (req, res) => {
  try {
    const rows = db.prepare(`${BILLING_SELECT} WHERE b.user_id = ? ORDER BY b.due_date DESC`).all(req.user.id);
    res.json({ status: 'success', count: rows.length, data: rows });
  } catch (err) {
    console.error('GET /billing/my error:', err);
    res.status(500).json({ status: 'error', message: 'Failed to fetch your billing history.' });
  }
});

// ────────────────────────────────────────────────────────────
// GET /api/billing  (admin only)
// ────────────────────────────────────────────────────────────
router.get('/', authenticateToken, requireRole('admin'), (req, res) => {
  try {
    const { status, search } = req.query;
    const { page, limit, offset } = getPagination(req.query);
    const clauses = [];
    const params = {};

    if (status) { clauses.push('b.status = @status'); params.status = status; }
    if (search) { clauses.push('(u.full_name LIKE @search OR u.email LIKE @search)'); params.search = `%${search}%`; }
    const where = clauses.length ? `WHERE ${clauses.join(' AND ')}` : '';

    const total = db.prepare(`SELECT COUNT(*) AS c FROM billing b LEFT JOIN users u ON b.user_id = u.id ${where}`).get(params).c;
    const rows = db.prepare(`${BILLING_SELECT} ${where} ORDER BY b.id DESC LIMIT @limit OFFSET @offset`).all({ ...params, limit, offset });

    res.json({ status: 'success', ...buildPaginatedResponse(rows, total, page, limit) });
  } catch (err) {
    console.error('GET /billing error:', err);
    res.status(500).json({ status: 'error', message: 'Failed to fetch billing records.' });
  }
});

// ────────────────────────────────────────────────────────────
// POST /api/billing  (admin only) — create an invoice
// ────────────────────────────────────────────────────────────
router.post('/', authenticateToken, requireRole('admin'), (req, res) => {
  try {
    const { user_id, semester_label, amount, due_date, method, status, description } = req.body || {};
    const errors = [];

    if (!user_id) errors.push('A user must be selected.');
    else if (!db.prepare('SELECT id FROM users WHERE id = ?').get(user_id)) errors.push('Selected user does not exist.');
    if (!semester_label) errors.push('Semester label is required.');
    if (amount === undefined || amount === null || isNaN(Number(amount))) errors.push('A valid amount is required.');

    if (errors.length) return res.status(400).json({ status: 'error', message: errors[0], errors });

    const info = db.prepare(`
      INSERT INTO billing (user_id, semester_label, amount, due_date, paid_date, method, status, description)
      VALUES (@user_id, @semester_label, @amount, @due_date, NULL, @method, @status, @description)
    `).run({
      user_id, semester_label, amount: Number(amount),
      due_date: due_date || null,
      method: method || null,
      status: status || 'due',
      description: description || null,
    });

    const row = db.prepare(`${BILLING_SELECT} WHERE b.id = ?`).get(info.lastInsertRowid);
    res.status(201).json({ status: 'success', message: 'Billing record created.', data: row });
  } catch (err) {
    console.error('POST /billing error:', err);
    res.status(500).json({ status: 'error', message: 'Failed to create billing record.' });
  }
});

// ────────────────────────────────────────────────────────────
// PUT /api/billing/:id  (admin only) — e.g. mark as paid
// ────────────────────────────────────────────────────────────
router.put('/:id', authenticateToken, requireRole('admin'), (req, res) => {
  try {
    const existing = db.prepare('SELECT * FROM billing WHERE id = ?').get(req.params.id);
    if (!existing) return res.status(404).json({ status: 'error', message: 'Billing record not found.' });

    const merged = { ...existing, ...req.body };
    if (merged.status && !VALID_STATUSES.includes(merged.status)) {
      return res.status(400).json({ status: 'error', message: `Status must be one of: ${VALID_STATUSES.join(', ')}.` });
    }
    // If marking as paid and no paid_date was supplied, stamp it with today.
    if (merged.status === 'paid' && !req.body.paid_date && !existing.paid_date) {
      merged.paid_date = new Date().toISOString().split('T')[0];
    }

    db.prepare(`
      UPDATE billing SET semester_label=@semester_label, amount=@amount, due_date=@due_date,
        paid_date=@paid_date, method=@method, status=@status, description=@description
      WHERE id=@id
    `).run({
      id: req.params.id,
      semester_label: merged.semester_label,
      amount: merged.amount,
      due_date: merged.due_date,
      paid_date: merged.paid_date,
      method: merged.method,
      status: merged.status,
      description: merged.description,
    });

    const row = db.prepare(`${BILLING_SELECT} WHERE b.id = ?`).get(req.params.id);
    res.json({ status: 'success', message: 'Billing record updated.', data: row });
  } catch (err) {
    console.error('PUT /billing/:id error:', err);
    res.status(500).json({ status: 'error', message: 'Failed to update billing record.' });
  }
});

module.exports = router;
