// backend/routes/lostfound.js
// Any authenticated user can report a lost item and see their own reports.
// Admins can see and manage all reports.

const express = require('express');
const db = require('../database');
const authenticateToken = require('../middleware/authMiddleware');
const requireRole = require('../middleware/roleMiddleware');
const { isNonEmptyString } = require('../utils/validators');
const { getPagination, buildPaginatedResponse } = require('../utils/pagination');

const router = express.Router();
const VALID_STATUSES = ['reported', 'found', 'claimed', 'returned'];

const LF_SELECT = `
  SELECT lf.*, u.full_name AS reporter_name, u.email AS reporter_email
  FROM lost_found lf
  LEFT JOIN users u ON lf.reported_by = u.id
`;

// ────────────────────────────────────────────────────────────
// GET /api/lostfound/my  (any authenticated user) — own reports
// ────────────────────────────────────────────────────────────
router.get('/my', authenticateToken, (req, res) => {
  try {
    const rows = db.prepare(`${LF_SELECT} WHERE lf.reported_by = ? ORDER BY lf.created_at DESC`).all(req.user.id);
    res.json({ status: 'success', count: rows.length, data: rows });
  } catch (err) {
    console.error('GET /lostfound/my error:', err);
    res.status(500).json({ status: 'error', message: 'Failed to fetch your reports.' });
  }
});

// ────────────────────────────────────────────────────────────
// GET /api/lostfound  (admin only) — all reports, filterable
// ────────────────────────────────────────────────────────────
router.get('/', authenticateToken, requireRole('admin'), (req, res) => {
  try {
    const { status, bus_number, search } = req.query;
    const { page, limit, offset } = getPagination(req.query);
    const clauses = [];
    const params = {};

    if (status) { clauses.push('lf.status = @status'); params.status = status; }
    if (bus_number) { clauses.push('lf.bus_number = @bus_number'); params.bus_number = bus_number; }
    if (search) { clauses.push('(lf.item_name LIKE @search OR lf.description LIKE @search)'); params.search = `%${search}%`; }
    const where = clauses.length ? `WHERE ${clauses.join(' AND ')}` : '';

    const total = db.prepare(`SELECT COUNT(*) AS c FROM lost_found lf ${where}`).get(params).c;
    const rows = db.prepare(`${LF_SELECT} ${where} ORDER BY lf.created_at DESC LIMIT @limit OFFSET @offset`)
      .all({ ...params, limit, offset });

    res.json({ status: 'success', ...buildPaginatedResponse(rows, total, page, limit) });
  } catch (err) {
    console.error('GET /lostfound error:', err);
    res.status(500).json({ status: 'error', message: 'Failed to fetch lost & found reports.' });
  }
});

// ────────────────────────────────────────────────────────────
// POST /api/lostfound  (any authenticated user) — report a lost item
// ────────────────────────────────────────────────────────────
router.post('/', authenticateToken, (req, res) => {
  try {
    const { item_name, description, bus_number, route_name, date_lost, contact_info } = req.body || {};
    const errors = [];

    if (!isNonEmptyString(item_name)) errors.push('Item name is required.');
    if (!isNonEmptyString(description)) errors.push('A short description is required.');

    if (errors.length) return res.status(400).json({ status: 'error', message: errors[0], errors });

    const info = db.prepare(`
      INSERT INTO lost_found (reported_by, item_name, description, bus_number, route_name, date_lost, status, contact_info, remarks)
      VALUES (@reported_by, @item_name, @description, @bus_number, @route_name, @date_lost, 'reported', @contact_info, @remarks)
    `).run({
      reported_by: req.user.id,
      item_name,
      description,
      bus_number: bus_number || null,
      route_name: route_name || null,
      date_lost: date_lost || null,
      contact_info: contact_info || null,
      remarks: 'Submitted via system. Under review by the transport office.',
    });

    const row = db.prepare(`${LF_SELECT} WHERE lf.id = ?`).get(info.lastInsertRowid);
    res.status(201).json({ status: 'success', message: 'Lost item reported successfully.', data: row });
  } catch (err) {
    console.error('POST /lostfound error:', err);
    res.status(500).json({ status: 'error', message: 'Failed to submit your report.' });
  }
});

// ────────────────────────────────────────────────────────────
// PUT /api/lostfound/:id  (admin only) — update status / remarks
// ────────────────────────────────────────────────────────────
router.put('/:id', authenticateToken, requireRole('admin'), (req, res) => {
  try {
    const existing = db.prepare('SELECT * FROM lost_found WHERE id = ?').get(req.params.id);
    if (!existing) return res.status(404).json({ status: 'error', message: 'Report not found.' });

    const merged = { ...existing, ...req.body };
    if (merged.status && !VALID_STATUSES.includes(merged.status)) {
      return res.status(400).json({ status: 'error', message: `Status must be one of: ${VALID_STATUSES.join(', ')}.` });
    }

    db.prepare(`
      UPDATE lost_found SET status = @status, remarks = @remarks
      WHERE id = @id
    `).run({ id: req.params.id, status: merged.status, remarks: merged.remarks });

    const row = db.prepare(`${LF_SELECT} WHERE lf.id = ?`).get(req.params.id);
    res.json({ status: 'success', message: 'Report updated.', data: row });
  } catch (err) {
    console.error('PUT /lostfound/:id error:', err);
    res.status(500).json({ status: 'error', message: 'Failed to update report.' });
  }
});

// ────────────────────────────────────────────────────────────
// DELETE /api/lostfound/:id  (admin only)
// ────────────────────────────────────────────────────────────
router.delete('/:id', authenticateToken, requireRole('admin'), (req, res) => {
  try {
    const info = db.prepare('DELETE FROM lost_found WHERE id = ?').run(req.params.id);
    if (info.changes === 0) return res.status(404).json({ status: 'error', message: 'Report not found.' });
    res.json({ status: 'success', message: 'Report deleted.' });
  } catch (err) {
    console.error('DELETE /lostfound/:id error:', err);
    res.status(500).json({ status: 'error', message: 'Failed to delete report.' });
  }
});

module.exports = router;
