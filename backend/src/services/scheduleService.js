const { AppError } = require('../middleware/errors');
const { validateScheduleInput, validateScheduleFields } = require('../utils/validation');

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

function createScheduleService(db, auditService, notificationService = null) {
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
    const where = includeCancelled ? '' : `WHERE s.status = 'ACTIVE'
      AND NOT EXISTS (
        SELECT 1 FROM special_trips st
        WHERE st.schedule_id=s.id AND st.approval_status!='APPROVED'
      )`;
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

  const createTransaction = db.transaction((payload, createdBy, context) => {
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
    const result = mapSchedule(findSchedule.get(info.lastInsertRowid));
    auditService.record({ actorUserId: createdBy, action: 'SCHEDULE_CREATE', entityType: 'SCHEDULE', entityId: result.id, outcome: 'SUCCESS', requestId: context.requestId, metadata: { changed_fields: Object.keys(validation.value) } });
    return result;
  });

  const updateTransaction = db.transaction((id, payload, actorUserId, context) => {
    const fieldErrors = validateScheduleFields(payload);
    if (fieldErrors.length) throw new AppError(400, fieldErrors[0], fieldErrors);

    const existing = findRawSchedule.get(id);
    if (!existing) throw new AppError(404, 'Schedule not found.');
    const merged = { ...existing, ...payload };
    delete merged.id;
    if (!Object.prototype.hasOwnProperty.call(payload, 'trip_type')) merged.trip_type = existing.trip_type;
    if (!Object.prototype.hasOwnProperty.call(payload, 'status')) merged.status = existing.status;
    const validation = validateScheduleInput(merged, { checkFields: false });
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
    const result = mapSchedule(findSchedule.get(id));
    auditService.record({ actorUserId, action: 'SCHEDULE_UPDATE', entityType: 'SCHEDULE', entityId: id, outcome: 'SUCCESS', requestId: context.requestId, metadata: { changed_fields: Object.keys(payload) } });
    if (notificationService && !context.suppressAutomaticNotification && existing.status === 'ACTIVE' && result.status === 'ACTIVE') {
      notificationService.createAutomatic({
        title: 'Schedule updated', message: `Schedule #${id} has an important update.`,
        notificationType: 'SCHEDULE_UPDATE', relatedEntityType: 'SCHEDULE', relatedEntityId: id,
        dedupeKey: `schedule:${id}:update:${context.requestId}`, createdBy: actorUserId, requestId: context.requestId,
      });
    }
    return result;
  });

  const deactivateTransaction = db.transaction((id, actorUserId, context) => {
    const existing = findRawSchedule.get(id);
    if (!existing) throw new AppError(404, 'Schedule not found.');
    db.prepare("UPDATE schedules SET status='CANCELLED', updated_at=CURRENT_TIMESTAMP WHERE id=?").run(id);
    const result = mapSchedule(findSchedule.get(id));
    auditService.record({ actorUserId, action: 'SCHEDULE_DEACTIVATE', entityType: 'SCHEDULE', entityId: id, outcome: 'SUCCESS', requestId: context.requestId, metadata: { previous_status: existing.status, new_status: 'CANCELLED' } });
    if (notificationService && !context.suppressAutomaticNotification && existing.status !== 'CANCELLED') {
      notificationService.createAutomatic({
        title: 'Schedule cancelled', message: `Schedule #${id} has been cancelled.`,
        notificationType: 'SCHEDULE_CANCELLATION', relatedEntityType: 'SCHEDULE', relatedEntityId: id,
        dedupeKey: `schedule:${id}:cancel:${context.requestId}`, createdBy: actorUserId, requestId: context.requestId,
      });
    }
    return result;
  });

  function withConflictAudit(operation, { actorUserId, requestId, action, entityId = null }) {
    try { return operation(); } catch (error) {
      if (error.statusCode === 409) {
        auditService.record({ actorUserId, action, entityType: 'SCHEDULE', entityId, outcome: 'CONFLICT', requestId, metadata: { conflict_type: /bus/i.test(error.message) ? 'bus_overlap' : 'driver_overlap' } });
      }
      throw error;
    }
  }

  return {
    list,
    getById,
    create: (payload, actorUserId, context) => withConflictAudit(
      () => createTransaction(payload, actorUserId, context),
      { actorUserId, requestId: context.requestId, action: 'SCHEDULE_CREATE' },
    ),
    update: (id, payload, actorUserId, context) => withConflictAudit(
      () => updateTransaction(id, payload, actorUserId, context),
      { actorUserId, requestId: context.requestId, action: 'SCHEDULE_UPDATE', entityId: id },
    ),
    deactivate: deactivateTransaction,
  };
}

module.exports = { createScheduleService };
