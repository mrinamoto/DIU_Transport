// backend/routes/schedules.js
// Bus schedule endpoints. Public users can view schedules (this is the
// core public-facing feature). Only admins can create/update/delete.

const express = require('express');
const db = require('../database');
const authenticateToken = require('../middleware/authMiddleware');
const requireRole = require('../middleware/roleMiddleware');
const { isNonEmptyString } = require('../utils/validators');

const router = express.Router();

// Joins schedules with route/bus/driver info so the frontend never has to
// make multiple round trips just to render one schedule card.
const SCHEDULE_SELECT = `
  SELECT
    s.id, s.departure_time, s.departure_note, s.arrival_time, s.direction,
    s.day_type, s.status, s.semester_label,
    r.id AS route_id, r.route_code, r.route_name, r.start_point, r.end_point, r.stops, r.route_details,
    b.id AS bus_id, b.bus_number, b.capacity AS bus_capacity, b.status AS bus_status,
    d.id AS driver_id, d.full_name AS driver_name, d.phone AS driver_phone
  FROM schedules s
  LEFT JOIN routes r ON s.route_id = r.id
  LEFT JOIN buses b ON s.bus_id = b.id
  LEFT JOIN drivers d ON s.driver_id = d.id
`;

function mapScheduleRow(row) {
  let stops = [];
  try { stops = row.stops ? JSON.parse(row.stops) : []; } catch (e) { stops = []; }
  return {
    id: row.id,
    departure_time: row.departure_time,
    departure_note: row.departure_note,
    arrival_time: row.arrival_time,
    direction: row.direction,
    day_type: row.day_type,
    status: row.status,
    semester_label: row.semester_label,
    route: {
      id: row.route_id,
      code: row.route_code,
      name: row.route_name,
      start_point: row.start_point,
      end_point: row.end_point,
      stops,
      details: row.route_details,
    },
    bus: row.bus_id ? { id: row.bus_id, number: row.bus_number, capacity: row.bus_capacity, status: row.bus_status } : null,
    driver: row.driver_id ? { id: row.driver_id, name: row.driver_name, phone: row.driver_phone } : null,
  };
}

// ────────────────────────────────────────────────────────────
// GET /api/schedules  (public)
// Optional filters: ?day_type=weekday|friday|exam  ?direction=to_campus|from_campus
//                   ?route_code=R1  ?search=dhanmondi  ?status=active
// ────────────────────────────────────────────────────────────
router.get('/', (req, res) => {
  try {
    const { day_type, direction, route_code, search, status } = req.query;
    const clauses = [];
    const params = {};

    if (day_type) { clauses.push('s.day_type = @day_type'); params.day_type = day_type; }
    if (direction) { clauses.push('s.direction = @direction'); params.direction = direction; }
    if (status) { clauses.push('s.status = @status'); params.status = status; }
    if (route_code) { clauses.push('r.route_code = @route_code'); params.route_code = route_code; }
    if (search) {
      clauses.push('(r.route_name LIKE @search OR r.route_code LIKE @search OR r.start_point LIKE @search)');
      params.search = `%${search}%`;
    }

    const where = clauses.length ? `WHERE ${clauses.join(' AND ')}` : '';
    const rows = db.prepare(`${SCHEDULE_SELECT} ${where} ORDER BY r.route_code, s.direction, s.departure_time`).all(params);

    res.json({ status: 'success', count: rows.length, data: rows.map(mapScheduleRow) });
  } catch (err) {
    console.error('GET /schedules error:', err);
    res.status(500).json({ status: 'error', message: 'Failed to fetch schedules.' });
  }
});

// ────────────────────────────────────────────────────────────
// GET /api/schedules/:id  (public)
// ────────────────────────────────────────────────────────────
router.get('/:id', (req, res) => {
  try {
    const row = db.prepare(`${SCHEDULE_SELECT} WHERE s.id = ?`).get(req.params.id);
    if (!row) return res.status(404).json({ status: 'error', message: 'Schedule not found.' });
    res.json({ status: 'success', data: mapScheduleRow(row) });
  } catch (err) {
    console.error('GET /schedules/:id error:', err);
    res.status(500).json({ status: 'error', message: 'Failed to fetch schedule.' });
  }
});

// ────────────────────────────────────────────────────────────
// POST /api/schedules  (admin only)
// ────────────────────────────────────────────────────────────
router.post('/', authenticateToken, requireRole('admin'), (req, res) => {
  try {
    const { bus_id, route_id, driver_id, departure_time, departure_note, arrival_time, direction, day_type, status, semester_label } = req.body || {};

    const errors = [];
    if (!isNonEmptyString(departure_time)) errors.push('Departure time is required.');
    if (!isNonEmptyString(arrival_time)) errors.push('Arrival time is required.');
    if (!['to_campus', 'from_campus'].includes(direction)) errors.push("Direction must be 'to_campus' or 'from_campus'.");
    if (!route_id) errors.push('A route must be selected.');

    if (route_id) {
      const routeExists = db.prepare('SELECT id FROM routes WHERE id = ?').get(route_id);
      if (!routeExists) errors.push('The selected route does not exist.');
    }

    if (errors.length) return res.status(400).json({ status: 'error', message: errors[0], errors });

    const info = db.prepare(`
      INSERT INTO schedules (bus_id, route_id, driver_id, departure_time, departure_note, arrival_time, direction, day_type, status, semester_label)
      VALUES (@bus_id, @route_id, @driver_id, @departure_time, @departure_note, @arrival_time, @direction, @day_type, @status, @semester_label)
    `).run({
      bus_id: bus_id || null,
      route_id,
      driver_id: driver_id || null,
      departure_time,
      departure_note: departure_note || null,
      arrival_time,
      direction,
      day_type: day_type || 'weekday',
      status: status || 'active',
      semester_label: semester_label || 'Mid-Term Exam, Summer 2026',
    });

    const row = db.prepare(`${SCHEDULE_SELECT} WHERE s.id = ?`).get(info.lastInsertRowid);
    res.status(201).json({ status: 'success', message: 'Schedule created.', data: mapScheduleRow(row) });
  } catch (err) {
    console.error('POST /schedules error:', err);
    res.status(500).json({ status: 'error', message: 'Failed to create schedule.' });
  }
});

// ────────────────────────────────────────────────────────────
// PUT /api/schedules/:id  (admin only)
// ────────────────────────────────────────────────────────────
router.put('/:id', authenticateToken, requireRole('admin'), (req, res) => {
  try {
    const existing = db.prepare('SELECT * FROM schedules WHERE id = ?').get(req.params.id);
    if (!existing) return res.status(404).json({ status: 'error', message: 'Schedule not found.' });

    const merged = { ...existing, ...req.body };

    if (merged.direction && !['to_campus', 'from_campus'].includes(merged.direction)) {
      return res.status(400).json({ status: 'error', message: "Direction must be 'to_campus' or 'from_campus'." });
    }

    db.prepare(`
      UPDATE schedules SET
        bus_id = @bus_id, route_id = @route_id, driver_id = @driver_id,
        departure_time = @departure_time, departure_note = @departure_note,
        arrival_time = @arrival_time, direction = @direction, day_type = @day_type,
        status = @status, semester_label = @semester_label
      WHERE id = @id
    `).run({
      id: req.params.id,
      bus_id: merged.bus_id || null,
      route_id: merged.route_id,
      driver_id: merged.driver_id || null,
      departure_time: merged.departure_time,
      departure_note: merged.departure_note || null,
      arrival_time: merged.arrival_time,
      direction: merged.direction,
      day_type: merged.day_type,
      status: merged.status,
      semester_label: merged.semester_label,
    });

    const row = db.prepare(`${SCHEDULE_SELECT} WHERE s.id = ?`).get(req.params.id);
    res.json({ status: 'success', message: 'Schedule updated.', data: mapScheduleRow(row) });
  } catch (err) {
    console.error('PUT /schedules/:id error:', err);
    res.status(500).json({ status: 'error', message: 'Failed to update schedule.' });
  }
});

// ────────────────────────────────────────────────────────────
// DELETE /api/schedules/:id  (admin only)
// ────────────────────────────────────────────────────────────
router.delete('/:id', authenticateToken, requireRole('admin'), (req, res) => {
  try {
    const info = db.prepare('DELETE FROM schedules WHERE id = ?').run(req.params.id);
    if (info.changes === 0) return res.status(404).json({ status: 'error', message: 'Schedule not found.' });
    res.json({ status: 'success', message: 'Schedule deleted.' });
  } catch (err) {
    console.error('DELETE /schedules/:id error:', err);
    res.status(500).json({ status: 'error', message: 'Failed to delete schedule.' });
  }
});

module.exports = router;
