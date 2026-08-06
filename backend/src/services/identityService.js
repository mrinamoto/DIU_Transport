const crypto = require('node:crypto');
const bcrypt = require('bcryptjs');
const { AppError } = require('../middleware/errors');
const { validatePassword } = require('../utils/validation');
const { BCRYPT_ROUNDS } = require('../routes/auth');

const ROLES = new Set(['STUDENT', 'TEACHER', 'STAFF', 'ADMIN']);
const ACCOUNT_STATUSES = new Set(['ACTIVE', 'INACTIVE']);
const SECURITY_STATUSES = new Set(['ACTIVE', 'LOCKED', 'SUSPENDED']);
const SAFE_SELECT = `id, full_name, email, role, status, security_status,
  failed_login_count, locked_until, last_login_at, security_updated_at, created_at, updated_at`;

function createIdentityService(db, auditService, config) {
  const safeUser = db.prepare(`SELECT ${SAFE_SELECT} FROM users WHERE id=?`);
  const usableAdminCount = db.prepare(`SELECT COUNT(*) count FROM users
    WHERE role='ADMIN' AND status='ACTIVE' AND security_status='ACTIVE'`).pluck();

  function get(id) {
    const row = safeUser.get(id);
    if (!row) throw new AppError(404, 'User not found.');
    return row;
  }

  function list(query = {}) {
    const page = Number.parseInt(query.page || '1', 10);
    const limit = Number.parseInt(query.limit || '25', 10);
    if (!Number.isInteger(page) || page < 1) throw new AppError(400, 'Page must be a positive integer.');
    if (!Number.isInteger(limit) || limit < 1 || limit > config.identityPageSizeMax) throw new AppError(400, `Limit must be between 1 and ${config.identityPageSizeMax}.`);
    const clauses = []; const params = {};
    for (const [key, allowed] of [['role', ROLES], ['status', ACCOUNT_STATUSES], ['security_status', SECURITY_STATUSES]]) {
      if (!query[key]) continue;
      const value = String(query[key]).toUpperCase();
      if (!allowed.has(value)) throw new AppError(400, `Invalid ${key} filter.`);
      clauses.push(`${key}=@${key}`); params[key] = value;
    }
    const where = clauses.length ? `WHERE ${clauses.join(' AND ')}` : '';
    const total = db.prepare(`SELECT COUNT(*) count FROM users ${where}`).get(params).count;
    const data = db.prepare(`SELECT ${SAFE_SELECT} FROM users ${where} ORDER BY id LIMIT @limit OFFSET @offset`).all({ ...params, limit, offset: (page - 1) * limit });
    return { data, pagination: { page, limit, total, total_pages: Math.max(1, Math.ceil(total / limit)) } };
  }

  function assertNotFinalAdmin(existing, next) {
    const currentlyUsable = existing.role === 'ADMIN' && existing.status === 'ACTIVE' && existing.security_status === 'ACTIVE';
    const nextUsable = next.role === 'ADMIN' && next.status === 'ACTIVE' && next.security_status === 'ACTIVE';
    if (currentlyUsable && !nextUsable && usableAdminCount.get() <= 1) throw new AppError(409, 'The final active administrator cannot be demoted or disabled.');
  }

  function change(id, patch, context) {
    const allowed = new Set(['role', 'status', 'security_status']);
    const keys = Object.keys(patch);
    if (!keys.length || keys.some((key) => !allowed.has(key))) throw new AppError(400, 'Only role, status, and security_status may be changed.');
    const existing = get(id);
    const next = { ...existing };
    if (patch.role !== undefined) { next.role = String(patch.role).toUpperCase(); if (!ROLES.has(next.role)) throw new AppError(400, 'Invalid role.'); }
    if (patch.status !== undefined) { next.status = String(patch.status).toUpperCase(); if (!ACCOUNT_STATUSES.has(next.status)) throw new AppError(400, 'Invalid account status.'); }
    if (patch.security_status !== undefined) { next.security_status = String(patch.security_status).toUpperCase(); if (!SECURITY_STATUSES.has(next.security_status)) throw new AppError(400, 'Invalid security status.'); }
    return db.transaction(() => {
      assertNotFinalAdmin(existing, next);
      const changed = keys.filter((key) => next[key] !== existing[key]);
      if (changed.length) db.prepare(`UPDATE users SET role=?,status=?,security_status=?,
        failed_login_count=CASE WHEN ?='ACTIVE' THEN 0 ELSE failed_login_count END,
        locked_until=CASE WHEN ?='ACTIVE' THEN NULL ELSE locked_until END,
        auth_version=auth_version+1,security_updated_at=CURRENT_TIMESTAMP,updated_at=CURRENT_TIMESTAMP WHERE id=?`)
        .run(next.role, next.status, next.security_status, next.security_status, next.security_status, id);
      auditService.record({ actorUserId: context.actorUserId, action: 'USER_IDENTITY_CHANGE', entityType: 'USER', entityId: id, outcome: 'SUCCESS', requestId: context.requestId, metadata: { changed_fields: changed, previous_role: existing.role, new_role: next.role, previous_status: existing.status, new_status: next.status, previous_security_status: existing.security_status, new_security_status: next.security_status } });
      return get(id);
    })();
  }

  function revoke(id, context) {
    get(id);
    db.prepare('UPDATE users SET auth_version=auth_version+1,security_updated_at=CURRENT_TIMESTAMP WHERE id=?').run(id);
    auditService.record({ actorUserId: context.actorUserId, action: 'USER_AUTH_REVOKE', entityType: 'USER', entityId: id, outcome: 'SUCCESS', requestId: context.requestId, metadata: { scope: 'all_sessions' } });
    return get(id);
  }

  function initiateRecovery(id, context) {
    const user = get(id);
    if (user.status !== 'ACTIVE') throw new AppError(409, 'Recovery requires an active account.');
    const rawToken = crypto.randomBytes(32).toString('base64url');
    const tokenHash = crypto.createHash('sha256').update(rawToken).digest('hex');
    const expiresAt = new Date(Date.now() + config.recoveryTokenTtlMinutes * 60000).toISOString();
    db.transaction(() => {
      db.prepare('UPDATE password_recovery_tokens SET revoked_at=CURRENT_TIMESTAMP WHERE user_id=? AND used_at IS NULL AND revoked_at IS NULL').run(id);
      db.prepare('INSERT INTO password_recovery_tokens(user_id,token_hash,expires_at,created_by) VALUES(?,?,?,?)').run(id, tokenHash, expiresAt, context.actorUserId);
      db.prepare('UPDATE users SET auth_version=auth_version+1,security_updated_at=CURRENT_TIMESTAMP WHERE id=?').run(id);
      auditService.record({ actorUserId: context.actorUserId, action: 'USER_RECOVERY_INITIATE', entityType: 'USER', entityId: id, outcome: 'SUCCESS', requestId: context.requestId, metadata: { expires_in_minutes: config.recoveryTokenTtlMinutes } });
    })();
    return { recovery_token: rawToken, expires_at: expiresAt, display: 'once' };
  }

  async function completeRecovery(token, password, requestId) {
    if (typeof token !== 'string' || token.length < 32) throw new AppError(400, 'Recovery token and new password are required.');
    const errors = validatePassword(password);
    if (errors.length) throw new AppError(400, errors[0], errors);
    const hash = crypto.createHash('sha256').update(token).digest('hex');
    const record = db.prepare(`SELECT * FROM password_recovery_tokens WHERE token_hash=?`).get(hash);
    if (!record || record.used_at || record.revoked_at || Date.parse(record.expires_at) <= Date.now()) throw new AppError(400, 'Recovery token is invalid or expired.');
    const passwordHash = await bcrypt.hash(password, BCRYPT_ROUNDS);
    db.transaction(() => {
      const used = db.prepare(`UPDATE password_recovery_tokens SET used_at=CURRENT_TIMESTAMP
        WHERE id=? AND used_at IS NULL AND revoked_at IS NULL AND datetime(expires_at)>CURRENT_TIMESTAMP`).run(record.id);
      if (!used.changes) throw new AppError(400, 'Recovery token is invalid or expired.');
      db.prepare(`UPDATE users SET password_hash=?,auth_version=auth_version+1,failed_login_count=0,
        locked_until=NULL,security_status='ACTIVE',security_updated_at=CURRENT_TIMESTAMP,updated_at=CURRENT_TIMESTAMP WHERE id=?`).run(passwordHash, record.user_id);
      auditService.record({ action: 'USER_RECOVERY_COMPLETE', entityType: 'USER', entityId: record.user_id, outcome: 'SUCCESS', requestId, metadata: { sessions_revoked: true } });
    })();
  }

  function exportUser(id, context = {}) {
    const user = get(id);
    if (context.actorUserId && context.actorUserId !== id) auditService.record({ actorUserId: context.actorUserId, action: 'USER_DATA_EXPORT', entityType: 'USER', entityId: id, outcome: 'SUCCESS', requestId: context.requestId, metadata: { scope: 'approved_account_support' } });
    return { generated_at: new Date().toISOString(), account: { id: user.id, full_name: user.full_name, email: user.email, role: user.role, status: user.status, security_status: user.security_status, created_at: user.created_at, updated_at: user.updated_at } };
  }

  return { list, get, change, revoke, initiateRecovery, completeRecovery, exportUser };
}

module.exports = { createIdentityService, ROLES, ACCOUNT_STATUSES, SECURITY_STATUSES };
