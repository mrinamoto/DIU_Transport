// backend/routes/routesApi.js
// Bus route (path/stops) endpoints. Public users can browse all routes.
// Only admins can create/update/delete a route.

const express = require('express');
const db = require('../database');
const authenticateToken = require('../middleware/authMiddleware');
const requireRole = require('../middleware/roleMiddleware');
const { isNonEmptyString } = require('../utils/validators');

const router = express.Router();

function mapRouteRow(row) {
  let stops = [];
  try { stops = row.stops ? JSON.parse(row.stops) : []; } catch (e) { stops = []; }
  return { ...row, stops };
}

// ────────────────────────────────────────────────────────────
// GET /api/routes  (public)  — optional ?day_type=regular|friday  ?search=
// ────────────────────────────────────────────────────────────
router.get('/', (req, res) => {
  try {
    const { day_type, search } = req.query;
    const clauses = [];
    const params = {};

    if (day_type) { clauses.push('day_type = @day_type'); params.day_type = day_type; }
    if (search) {
      clauses.push('(route_name LIKE @search OR route_code LIKE @search OR start_point LIKE @search)');
      params.search = `%${search}%`;
    }

    const where = clauses.length ? `WHERE ${clauses.join(' AND ')}` : '';
    const rows = db.prepare(`SELECT * FROM routes ${where} ORDER BY route_code`).all(params);

    res.json({ status: 'success', count: rows.length, data: rows.map(mapRouteRow) });
  } catch (err) {
    console.error('GET /routes error:', err);
    res.status(500).json({ status: 'error', message: 'Failed to fetch routes.' });
  }
});

// ────────────────────────────────────────────────────────────
// GET /api/routes/:id  (public)
// ────────────────────────────────────────────────────────────
router.get('/:id', (req, res) => {
  try {
    const row = db.prepare('SELECT * FROM routes WHERE id = ?').get(req.params.id);
    if (!row) return res.status(404).json({ status: 'error', message: 'Route not found.' });
    res.json({ status: 'success', data: mapRouteRow(row) });
  } catch (err) {
    console.error('GET /routes/:id error:', err);
    res.status(500).json({ status: 'error', message: 'Failed to fetch route.' });
  }
});

// ────────────────────────────────────────────────────────────
// POST /api/routes  (admin only)
// ────────────────────────────────────────────────────────────
router.post('/', authenticateToken, requireRole('admin'), (req, res) => {
  try {
    const { route_code, route_name, start_point, end_point, stops, route_details, day_type, distance_km, estimated_duration } = req.body || {};

    const errors = [];
    if (!isNonEmptyString(route_code)) errors.push('Route code is required (e.g. R1).');
    if (!isNonEmptyString(route_name)) errors.push('Route name is required.');
    if (!isNonEmptyString(start_point)) errors.push('Start point is required.');

    if (route_code) {
      const dup = db.prepare('SELECT id FROM routes WHERE route_code = ?').get(route_code);
      if (dup) errors.push(`Route code "${route_code}" is already in use.`);
    }

    if (errors.length) return res.status(400).json({ status: 'error', message: errors[0], errors });

    const info = db.prepare(`
      INSERT INTO routes (route_code, route_name, start_point, end_point, stops, route_details, day_type, distance_km, estimated_duration)
      VALUES (@route_code, @route_name, @start_point, @end_point, @stops, @route_details, @day_type, @distance_km, @estimated_duration)
    `).run({
      route_code,
      route_name,
      start_point,
      end_point: end_point || 'Daffodil Smart City (DSC)',
      stops: JSON.stringify(Array.isArray(stops) ? stops : []),
      route_details: route_details || null,
      day_type: day_type || 'regular',
      distance_km: distance_km || null,
      estimated_duration: estimated_duration || null,
    });

    const row = db.prepare('SELECT * FROM routes WHERE id = ?').get(info.lastInsertRowid);
    res.status(201).json({ status: 'success', message: 'Route created.', data: mapRouteRow(row) });
  } catch (err) {
    console.error('POST /routes error:', err);
    res.status(500).json({ status: 'error', message: 'Failed to create route.' });
  }
});

// ────────────────────────────────────────────────────────────
// PUT /api/routes/:id  (admin only)
// ────────────────────────────────────────────────────────────
router.put('/:id', authenticateToken, requireRole('admin'), (req, res) => {
  try {
    const existing = db.prepare('SELECT * FROM routes WHERE id = ?').get(req.params.id);
    if (!existing) return res.status(404).json({ status: 'error', message: 'Route not found.' });

    if (req.body.route_code && req.body.route_code !== existing.route_code) {
      const dup = db.prepare('SELECT id FROM routes WHERE route_code = ? AND id != ?').get(req.body.route_code, req.params.id);
      if (dup) return res.status(409).json({ status: 'error', message: `Route code "${req.body.route_code}" is already in use.` });
    }

    const merged = { ...existing, ...req.body };
    const stopsJson = Array.isArray(req.body.stops) ? JSON.stringify(req.body.stops) : existing.stops;

    db.prepare(`
      UPDATE routes SET
        route_code = @route_code, route_name = @route_name, start_point = @start_point,
        end_point = @end_point, stops = @stops, route_details = @route_details,
        day_type = @day_type, distance_km = @distance_km, estimated_duration = @estimated_duration
      WHERE id = @id
    `).run({
      id: req.params.id,
      route_code: merged.route_code,
      route_name: merged.route_name,
      start_point: merged.start_point,
      end_point: merged.end_point,
      stops: stopsJson,
      route_details: merged.route_details,
      day_type: merged.day_type,
      distance_km: merged.distance_km,
      estimated_duration: merged.estimated_duration,
    });

    const row = db.prepare('SELECT * FROM routes WHERE id = ?').get(req.params.id);
    res.json({ status: 'success', message: 'Route updated.', data: mapRouteRow(row) });
  } catch (err) {
    console.error('PUT /routes/:id error:', err);
    res.status(500).json({ status: 'error', message: 'Failed to update route.' });
  }
});

// ────────────────────────────────────────────────────────────
// DELETE /api/routes/:id  (admin only)
// Blocks deletion if schedules still reference this route (protects data integrity)
// ────────────────────────────────────────────────────────────
router.delete('/:id', authenticateToken, requireRole('admin'), (req, res) => {
  try {
    const inUse = db.prepare('SELECT COUNT(*) AS c FROM schedules WHERE route_id = ?').get(req.params.id);
    if (inUse.c > 0) {
      return res.status(409).json({
        status: 'error',
        message: `Cannot delete this route — ${inUse.c} schedule(s) still reference it. Delete those schedules first.`,
      });
    }

    const info = db.prepare('DELETE FROM routes WHERE id = ?').run(req.params.id);
    if (info.changes === 0) return res.status(404).json({ status: 'error', message: 'Route not found.' });
    res.json({ status: 'success', message: 'Route deleted.' });
  } catch (err) {
    console.error('DELETE /routes/:id error:', err);
    res.status(500).json({ status: 'error', message: 'Failed to delete route.' });
  }
});

module.exports = router;
