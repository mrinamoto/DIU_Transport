const { AppError } = require('../middleware/errors');
const { validateEmployeeInput, validateFields } = require('../utils/validation');

const EMPLOYEE_FIELDS = new Set(['employee_code','full_name','employee_role','phone','email','shift','status','notes']);

function createEmployeeService(db, auditService) {
  const find = db.prepare('SELECT * FROM employees WHERE id=?');

  function validate(payload) {
    const result = validateEmployeeInput(payload);
    if (!result.valid) throw new AppError(400, result.errors[0], result.errors);
    return result.value;
  }

  function map(row, isAdmin) {
    if (isAdmin) return row;
    return {
      id: row.id, employee_code: row.employee_code, full_name: row.full_name,
      employee_role: row.employee_role, shift: row.shift,
    };
  }

  function list({ isAdmin = false } = {}) {
    const rows = db.prepare(`SELECT * FROM employees ${isAdmin ? '' : "WHERE status='ACTIVE'"} ORDER BY employee_code COLLATE NOCASE`).all();
    return rows.map((row) => map(row, isAdmin));
  }

  function getById(id, { isAdmin = false } = {}) {
    const row = find.get(id);
    if (!row || (!isAdmin && row.status !== 'ACTIVE')) throw new AppError(404, 'Employee not found.');
    return map(row, isAdmin);
  }

  function ensureUnique(code, excludeId, context, action) {
    if (!db.prepare('SELECT id FROM employees WHERE employee_code=? COLLATE NOCASE AND id!=?').get(code, excludeId)) return;
    auditService.record({ actorUserId: context.actorUserId, action, entityType:'EMPLOYEE', entityId:excludeId || null, outcome:'CONFLICT', requestId:context.requestId, metadata:{ reason:'normalized_duplicate' } });
    throw new AppError(409, 'An employee with this normalized code already exists.');
  }

  function create(payload, context) {
    const value = validate(payload); ensureUnique(value.employee_code, 0, context, 'EMPLOYEE_CREATE');
    return db.transaction(() => {
      const info = db.prepare(`INSERT INTO employees (employee_code,full_name,employee_role,phone,email,shift,status,notes)
        VALUES (@employee_code,@full_name,@employee_role,@phone,@email,@shift,@status,@notes)`).run(value);
      const id = Number(info.lastInsertRowid);
      auditService.record({ actorUserId:context.actorUserId, action:'EMPLOYEE_CREATE', entityType:'EMPLOYEE', entityId:id, outcome:'SUCCESS', requestId:context.requestId, metadata:{ changed_fields:Object.keys(value) } });
      return getById(id, { isAdmin:true });
    })();
  }

  function update(id, payload, context) {
    const existing = find.get(id); if (!existing) throw new AppError(404, 'Employee not found.');
    const fieldErrors=validateFields(payload,EMPLOYEE_FIELDS);if(fieldErrors.length)throw new AppError(400,fieldErrors[0],fieldErrors);
    const candidate = Object.fromEntries([...EMPLOYEE_FIELDS]
      .map((field) => [field, Object.prototype.hasOwnProperty.call(payload, field) ? payload[field] : existing[field]]));
    const value = validate(candidate);
    ensureUnique(value.employee_code, id, context, 'EMPLOYEE_UPDATE');
    const action = existing.status !== 'ACTIVE' && value.status === 'ACTIVE' ? 'EMPLOYEE_REACTIVATE'
      : existing.status === 'ACTIVE' && value.status !== 'ACTIVE' ? 'EMPLOYEE_DEACTIVATE' : 'EMPLOYEE_UPDATE';
    return db.transaction(() => {
      db.prepare(`UPDATE employees SET employee_code=@employee_code,full_name=@full_name,employee_role=@employee_role,
        phone=@phone,email=@email,shift=@shift,status=@status,notes=@notes,updated_at=CURRENT_TIMESTAMP WHERE id=@id`).run({ ...value, id });
      auditService.record({ actorUserId:context.actorUserId, action, entityType:'EMPLOYEE', entityId:id, outcome:'SUCCESS', requestId:context.requestId,
        metadata:{ changed_fields:Object.keys(payload), previous_status:existing.status, new_status:value.status } });
      return getById(id, { isAdmin:true });
    })();
  }

  return { list, getById, create, update, deactivate:(id,context)=>update(id,{status:'INACTIVE'},context), reactivate:(id,context)=>update(id,{status:'ACTIVE'},context) };
}

module.exports = { createEmployeeService };
