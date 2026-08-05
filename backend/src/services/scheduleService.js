const { AppError } = require('../middleware/errors');
const { validateScheduleInput } = require('../utils/validation');

const SCHEDULE_SELECT = `
  SELECT s.id, s.route_id, s.bus_id, s.driver_id, s.service_date,
         s.departure_time, s.arrival_time, s.trip_type, s.status, s.notes,
         s.created_by, s.created_at, s.updated_at,
         r.route_name, r.origin, r.destination,
         b.bus_number, b.capacity AS bus_capacity,
         d.full_name AS driver_name
  FROM schedules s
  JOIN routes r ON r.id = s.route_id
  JOIN buses b ON b.id = s.bus_id
  JOIN drivers d ON d.id = s.driver_id
`;

function mapSchedule(row) {
  return {
    id: row.id,
    service_date: row.service_date,
    departure_time: row.departure_time,
    arrival_time: row.arrival_time,
    trip_type: row.trip_type,
    status: row.status,
    notes: row.notes,
    route: {
      id: row.route_id,
      name: row.route_name,
      origin: row.origin,
      destination: row.destination,
    },
    bus: { id: row.bus_id, number: row.bus_number, capacity: row.bus_capacity },
    driver: { id: row.driver_id, name: row.driver_name },
    created_by: row.created_by,
    created_at: row.created_at,
    updated_at: row.updated_at,
  };
}

function createScheduleService(db) {
  const findRoute = db.prepare('SELECT id, status FROM routes WHERE id = ?');
  const findBus = db.prepare('SELECT id, status FROM buses WHERE id = ?');
  const findDriver = db.prepare('SELECT id, status FROM drivers WHERE id = ?');
  const findRawSchedule = db.prepare('SELECT * FROM schedules WHERE id = ?');
  const findSchedule = db.prepare(`${SCHEDULE_SELECT} WHERE s.id = ?`);
  const findConflict = db.prepare(`
    SELECT id, bus_id, driver_id
    FROM schedules
    WHERE service_date = @service_date
      AND status = 'ACTIVE'
      AND id != @exclude_id
      AND (@departure_time < arrival_time AND @arrival_time > departure_time)
      AND (bus_id = @bus_id OR driver_id = @driver_id)
    LIMIT 1
  `);

  function ensureAssignments(schedule) {
    const route = findRoute.get(schedule.route_id);
    if (!route) throw new AppError(400, 'Selected route does not exist.');
    if (route.status !== 'ACTIVE') throw new AppError(400, 'Selected route is not active.');

    const bus = findBus.get(schedule.bus_id);
    if (!bus) throw new AppError(400, 'Selected bus does not exist.');
    if (bus.status !== 'ACTIVE') throw new AppError(400, 'Selected bus is not active.');

    const driver = findDriver.get(schedule.driver_id);
    if (!driver) throw new AppError(400, 'Selected driver does not exist.');
    if (driver.status !== 'ACTIVE') throw new AppError(400, 'Selected driver is not active.');
  }

  function ensureNoConflict(schedule, excludeId = 0) {
    if (schedule.status !== 'ACTIVE') return;
    const conflict = findConflict.get({ ...schedule, exclude_id: excludeId });
    if (!conflict) return;
    if (conflict.bus_id === schedule.bus_id) {
      throw new AppError(409, 'The selected bus already has an overlapping schedule.');
    }
    throw new AppError(409, 'The selected driver already has an overlapping schedule.');
  }

  function list({ includeCancelled = false } = {}) {
    const where = includeCancelled ? '' : "WHERE s.status = 'ACTIVE'";
    return db.prepare(`${SCHEDULE_SELECT} ${where} ORDER BY s.service_date, s.departure_time, s.id`)
      .all().map(mapSchedule);
  }

  function getById(id, { includeCancelled = false } = {}) {
    const row = findSchedule.get(id);
    if (!row || (!includeCancelled && row.status !== 'ACTIVE')) {
      throw new AppError(404, 'Schedule not found.');
    }
    return mapSchedule(row);
  }

  const createTransaction = db.transaction((payload, createdBy) => {
    const validation = validateScheduleInput(payload);
    if (!validation.valid) throw new AppError(400, validation.errors[0], validation.errors);
    ensureAssignments(validation.value);
    ensureNoConflict(validation.value);
    const value = validation.value;
    const info = db.prepare(`
      INSERT INTO schedules (
        route_id, bus_id, driver_id, service_date, departure_time, arrival_time,
        trip_type, status, notes, created_by
      ) VALUES (
        @route_id, @bus_id, @driver_id, @service_date, @departure_time, @arrival_time,
        @trip_type, @status, @notes, @created_by
      )
    `).run({ ...value, created_by: createdBy });
    return mapSchedule(findSchedule.get(info.lastInsertRowid));
  });

  const updateTransaction = db.transaction((id, payload) => {
    if (Object.prototype.hasOwnProperty.call(payload, 'id')) {
      throw new AppError(400, 'Schedule IDs are assigned by the server.');
    }

    const existing = findRawSchedule.get(id);
    if (!existing) throw new AppError(404, 'Schedule not found.');
    const merged = { ...existing, ...payload };
    delete merged.id;
    if (!Object.prototype.hasOwnProperty.call(payload, 'trip_type')) merged.trip_type = existing.trip_type;
    if (!Object.prototype.hasOwnProperty.call(payload, 'status')) merged.status = existing.status;
    const validation = validateScheduleInput(merged);
    if (!validation.valid) throw new AppError(400, validation.errors[0], validation.errors);
    ensureAssignments(validation.value);
    ensureNoConflict(validation.value, id);
    db.prepare(`
      UPDATE schedules SET
        route_id=@route_id, bus_id=@bus_id, driver_id=@driver_id,
        service_date=@service_date, departure_time=@departure_time, arrival_time=@arrival_time,
        trip_type=@trip_type, status=@status, notes=@notes, updated_at=CURRENT_TIMESTAMP
      WHERE id=@id
    `).run({ ...validation.value, id });
    return mapSchedule(findSchedule.get(id));
  });

  const deactivateTransaction = db.transaction((id) => {
    const existing = findRawSchedule.get(id);
    if (!existing) throw new AppError(404, 'Schedule not found.');
    db.prepare("UPDATE schedules SET status='CANCELLED', updated_at=CURRENT_TIMESTAMP WHERE id=?").run(id);
    return mapSchedule(findSchedule.get(id));
  });

  return {
    list,
    getById,
    create: createTransaction,
    update: updateTransaction,
    deactivate: deactivateTransaction,
  };
}

module.exports = { createScheduleService };
