// backend/routes/cards.js
// Transport card endpoints. A logged-in user can see their own card(s).
// Admins can view/issue/update any card.

const express = require('express');
const db = require('../database');
const authenticateToken = require('../middleware/authMiddleware');
const requireRole = require('../middleware/roleMiddleware');
const { getPagination, buildPaginatedResponse } = require('../utils/pagination');

const router = express.Router();

const CARD_SELECT = `
  SELECT tc.*, u.full_name AS user_name, u.email AS user_email, u.role AS user_role,
         r.route_code, r.route_name
  FROM transport_cards tc
  LEFT JOIN users u ON tc.user_id = u.id
  LEFT JOIN routes r ON tc.route_id = r.id
`;

// ────────────────────────────────────────────────────────────
// GET /api/cards/my  (any authenticated user)
// ────────────────────────────────────────────────────────────
router.get('/my', authenticateToken, (req, res) => {
  try {
    const rows = db.prepare(`${CARD_SELECT} WHERE tc.user_id = ? ORDER BY tc.id DESC`).all(req.user.id);
    res.json({ status: 'success', count: rows.length, data: rows });
  } catch (err) {
    console.error('GET /cards/my error:', err);
    res.status(500).json({ status: 'error', message: 'Failed to fetch your transport card.' });
  }
});

// ────────────────────────────────────────────────────────────
// GET /api/cards  (admin only) — ?status=  ?search=  ?page=  ?limit=
// ────────────────────────────────────────────────────────────
router.get('/', authenticateToken, requireRole('admin'), (req, res) => {
  try {
    const { status, search } = req.query;
    const { page, limit, offset } = getPagination(req.query);
    const clauses = [];
    const params = {};

    if (status) { clauses.push('tc.status = @status'); params.status = status; }
    if (search) {
      clauses.push('(u.full_name LIKE @search OR tc.card_number LIKE @search OR u.email LIKE @search)');
      params.search = `%${search}%`;
    }
    const where = clauses.length ? `WHERE ${clauses.join(' AND ')}` : '';

    const total = db.prepare(`SELECT COUNT(*) AS c FROM transport_cards tc LEFT JOIN users u ON tc.user_id = u.id ${where}`).get(params).c;
    const rows = db.prepare(`${CARD_SELECT} ${where} ORDER BY tc.id DESC LIMIT @limit OFFSET @offset`)
      .all({ ...params, limit, offset });

    res.json({ status: 'success', ...buildPaginatedResponse(rows, total, page, limit) });
  } catch (err) {
    console.error('GET /cards error:', err);
    res.status(500).json({ status: 'error', message: 'Failed to fetch transport cards.' });
  }
});

// ────────────────────────────────────────────────────────────
// POST /api/cards  (admin only) — issue a new card
// ────────────────────────────────────────────────────────────
router.post('/', authenticateToken, requireRole('admin'), (req, res) => {
  try {
    const { user_id, card_number, issue_date, expiry_date, status, route_id, balance } = req.body || {};
    const errors = [];

    if (!user_id) errors.push('A user must be selected.');
    else if (!db.prepare('SELECT id FROM users WHERE id = ?').get(user_id)) errors.push('Selected user does not exist.');

    if (!card_number) errors.push('Card number is required.');
    else if (db.prepare('SELECT id FROM transport_cards WHERE card_number = ?').get(card_number)) errors.push(`Card number "${card_number}" is already issued.`);

    if (!issue_date) errors.push('Issue date is required.');
    if (!expiry_date) errors.push('Expiry date is required.');

    if (errors.length) return res.status(400).json({ status: 'error', message: errors[0], errors });

    const info = db.prepare(`
      INSERT INTO transport_cards (user_id, card_number, issue_date, expiry_date, status, route_id, balance)
      VALUES (@user_id, @card_number, @issue_date, @expiry_date, @status, @route_id, @balance)
    `).run({
      user_id, card_number, issue_date, expiry_date,
      status: status || 'pending',
      route_id: route_id || null,
      balance: balance || 0,
    });

    const row = db.prepare(`${CARD_SELECT} WHERE tc.id = ?`).get(info.lastInsertRowid);
    res.status(201).json({ status: 'success', message: 'Transport card issued.', data: row });
  } catch (err) {
    console.error('POST /cards error:', err);
    res.status(500).json({ status: 'error', message: 'Failed to issue transport card.' });
  }
});

// ────────────────────────────────────────────────────────────
// PUT /api/cards/:id  (admin only) — update status / route / balance
// ────────────────────────────────────────────────────────────
router.put('/:id', authenticateToken, requireRole('admin'), (req, res) => {
  try {
    const existing = db.prepare('SELECT * FROM transport_cards WHERE id = ?').get(req.params.id);
    if (!existing) return res.status(404).json({ status: 'error', message: 'Transport card not found.' });

    const VALID_STATUSES = ['active', 'expired', 'pending', 'suspended'];
    const merged = { ...existing, ...req.body };
    if (!VALID_STATUSES.includes(merged.status)) {
      return res.status(400).json({ status: 'error', message: `Status must be one of: ${VALID_STATUSES.join(', ')}.` });
    }

    db.prepare(`
      UPDATE transport_cards SET
        user_id = @user_id, card_number = @card_number, issue_date = @issue_date,
        expiry_date = @expiry_date, status = @status, route_id = @route_id, balance = @balance
      WHERE id = @id
    `).run({
      id: req.params.id,
      user_id: merged.user_id,
      card_number: merged.card_number,
      issue_date: merged.issue_date,
      expiry_date: merged.expiry_date,
      status: merged.status,
      route_id: merged.route_id,
      balance: merged.balance,
    });

    const row = db.prepare(`${CARD_SELECT} WHERE tc.id = ?`).get(req.params.id);
    res.json({ status: 'success', message: 'Transport card updated.', data: row });
  } catch (err) {
    console.error('PUT /cards/:id error:', err);
    res.status(500).json({ status: 'error', message: 'Failed to update transport card.' });
  }
});

module.exports = router;
