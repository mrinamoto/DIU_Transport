const { AppError } = require('../middleware/errors');
const { validateBusInput, validateDriverInput, validateRouteInput } = require('../utils/validation');

const DEFINITIONS = {
  buses: {
    entity: 'BUS', table: 'buses', validator: validateBusInput,
    fields: ['bus_number', 'capacity', 'status'], uniqueField: 'bus_number', scheduleField: 'bus_id', inactiveStatus: 'INACTIVE',
  },
  drivers: {
    entity: 'DRIVER', table: 'drivers', validator: validateDriverInput,
    fields: ['full_name', 'phone', 'status'], scheduleField: 'driver_id', inactiveStatus: 'INACTIVE',
  },
  routes: {
    entity: 'ROUTE', table: 'routes', validator: validateRouteInput,
    fields: ['route_name', 'origin', 'destination', 'status', 'stops'], uniqueField: 'route_name', scheduleField: 'route_id', inactiveStatus: 'INACTIVE',
  },
};

function createCatalogService(db, auditService, { today = () => new Date().toISOString().slice(0, 10) } = {}) {
  const stopsForRoute = db.prepare('SELECT id, stop_name, stop_order FROM route_stops WHERE route_id=? ORDER BY stop_order');

  function definition(type) {
    const result = DEFINITIONS[type];
    if (!result) throw new Error(`Unknown catalog type: ${type}`);
    return result;
  }

  function mapRow(type, row, isAdmin) {
    if (type === 'drivers' && !isAdmin) return { id: row.id, full_name: row.full_name };
    const result = { ...row };
    if (type === 'routes') result.stops = stopsForRoute.all(row.id);
    return result;
  }

  function list(type, { isAdmin = false } = {}) {
    const def = definition(type);
    const where = isAdmin ? '' : "WHERE status='ACTIVE'";
    const order = type === 'buses' ? 'bus_number' : type === 'drivers' ? 'full_name' : 'route_name';
    const fields = isAdmin
      ? '*'
      : type === 'buses' ? 'id, bus_number, capacity' : type === 'drivers' ? 'id, full_name' : 'id, route_name, origin, destination';
    return db.prepare(`SELECT ${fields} FROM ${def.table} ${where} ORDER BY ${order} COLLATE NOCASE`)
      .all().map((row) => mapRow(type, row, isAdmin));
  }

  function getById(type, id, { isAdmin = false } = {}) {
    const def = definition(type);
    const row = db.prepare(`SELECT * FROM ${def.table} WHERE id=?`).get(id);
    if (!row || (!isAdmin && row.status !== 'ACTIVE')) throw new AppError(404, `${def.entity.toLowerCase()} not found.`);
    return mapRow(type, row, isAdmin);
  }

  function ensurePayloadFields(def, payload) {
    for (const key of Object.keys(payload || {})) {
      if (['id', 'created_at', 'updated_at'].includes(key)) throw new AppError(400, `${key} is controlled by the server.`);
      if (!def.fields.includes(key)) throw new AppError(400, `Unknown field: ${key}.`);
    }
  }

  function validate(def, payload) {
    const validation = def.validator(payload);
    if (!validation.valid) throw new AppError(400, validation.errors[0], validation.errors);
    return validation.value;
  }

  function ensureUnique(def, value, excludeId = 0, context) {
    if (!def.uniqueField) return;
    const duplicate = db.prepare(`SELECT id FROM ${def.table} WHERE ${def.uniqueField} = ? COLLATE NOCASE AND id != ?`)
      .get(value[def.uniqueField], excludeId);
    if (!duplicate) return;
    auditService.record({
      actorUserId: context.actorUserId, action: `${def.entity}_${excludeId ? 'UPDATE' : 'CREATE'}`,
      entityType: def.entity, entityId: excludeId || null, outcome: 'CONFLICT', requestId: context.requestId,
      metadata: { reason: 'normalized_duplicate' },
    });
    throw new AppError(409, `${def.entity.toLowerCase()} already exists with that normalized name.`);
  }

  function replaceStops(routeId, stops) {
    db.prepare('DELETE FROM route_stops WHERE route_id=?').run(routeId);
    const insert = db.prepare('INSERT INTO route_stops (route_id, stop_name, stop_order) VALUES (?, ?, ?)');
    stops.forEach((stop, index) => insert.run(routeId, stop, index + 1));
  }

  function writeValues(def, value) {
    return Object.fromEntries(def.fields.filter((field) => field !== 'stops').map((field) => [field, value[field]]));
  }

  function create(type, payload, context) {
    const def = definition(type);
    ensurePayloadFields(def, payload);
    const value = validate(def, payload);
    ensureUnique(def, value, 0, context);
    return db.transaction(() => {
      const values = writeValues(def, value);
      const columns = Object.keys(values);
      const info = db.prepare(`INSERT INTO ${def.table} (${columns.join(', ')}) VALUES (${columns.map((field) => `@${field}`).join(', ')})`)
        .run(values);
      const id = Number(info.lastInsertRowid);
      if (type === 'routes') replaceStops(id, value.stops);
      auditService.record({
        actorUserId: context.actorUserId, action: `${def.entity}_CREATE`, entityType: def.entity,
        entityId: id, outcome: 'SUCCESS', requestId: context.requestId, metadata: { changed_fields: columns },
      });
      return getById(type, id, { isAdmin: true });
    })();
  }

  function futureAssignment(def, id) {
    return db.prepare(`
      SELECT id FROM schedules
      WHERE ${def.scheduleField}=? AND status='ACTIVE' AND service_date>=?
      ORDER BY service_date LIMIT 1
    `).get(id, today());
  }

  function update(type, id, payload, context) {
    const def = definition(type);
    ensurePayloadFields(def, payload);
    const existing = db.prepare(`SELECT * FROM ${def.table} WHERE id=?`).get(id);
    if (!existing) throw new AppError(404, `${def.entity.toLowerCase()} not found.`);
    const candidate = Object.fromEntries(def.fields.map((field) => [field, field === 'stops'
      ? (Object.prototype.hasOwnProperty.call(payload, field) ? payload[field] : stopsForRoute.all(id).map((stop) => stop.stop_name))
      : (Object.prototype.hasOwnProperty.call(payload, field) ? payload[field] : existing[field])]));
    const value = validate(def, candidate);
    ensureUnique(def, value, id, context);

    if (existing.status === 'ACTIVE' && value.status !== 'ACTIVE' && futureAssignment(def, id)) {
      auditService.record({
        actorUserId: context.actorUserId, action: `${def.entity}_DEACTIVATE`, entityType: def.entity,
        entityId: id, outcome: 'CONFLICT', requestId: context.requestId, metadata: { reason: 'future_active_schedule' },
      });
      throw new AppError(409, `Cannot deactivate this ${def.entity.toLowerCase()} while a future active schedule uses it.`);
    }

    const action = existing.status !== 'ACTIVE' && value.status === 'ACTIVE'
      ? `${def.entity}_REACTIVATE`
      : existing.status === 'ACTIVE' && value.status !== 'ACTIVE' ? `${def.entity}_DEACTIVATE` : `${def.entity}_UPDATE`;
    const changedFields = def.fields.filter((field) => Object.prototype.hasOwnProperty.call(payload, field));

    return db.transaction(() => {
      const values = writeValues(def, value);
      const assignments = Object.keys(values).map((field) => `${field}=@${field}`).join(', ');
      db.prepare(`UPDATE ${def.table} SET ${assignments}, updated_at=CURRENT_TIMESTAMP WHERE id=@id`).run({ ...values, id });
      if (type === 'routes' && Object.prototype.hasOwnProperty.call(payload, 'stops')) replaceStops(id, value.stops);
      auditService.record({
        actorUserId: context.actorUserId, action, entityType: def.entity, entityId: id,
        outcome: 'SUCCESS', requestId: context.requestId,
        metadata: { changed_fields: changedFields, previous_status: existing.status, new_status: value.status },
      });
      return getById(type, id, { isAdmin: true });
    })();
  }

  function deactivate(type, id, context) {
    return update(type, id, { status: definition(type).inactiveStatus }, context);
  }

  return { list, getById, create, update, deactivate };
}

module.exports = { createCatalogService, DEFINITIONS };
