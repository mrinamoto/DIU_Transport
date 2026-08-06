const { AppError } = require('../middleware/errors');

function createOutboxService(db, auditService, { maxAttempts = 3 } = {}) {
  const find = db.prepare('SELECT * FROM notification_outbox WHERE id=?');
  function enqueue(notification) {
    const recipient = notification.audience_role ? `ROLE:${notification.audience_role}` : 'AUDIENCE:ALL';
    const idempotencyKey = `notification:${notification.id}:NOOP`;
    db.prepare(`INSERT OR IGNORE INTO notification_outbox
      (notification_id,channel,recipient_reference,idempotency_key) VALUES(?,'NOOP',?,?)`)
      .run(notification.id, recipient, idempotencyKey);
    return db.prepare('SELECT * FROM notification_outbox WHERE idempotency_key=?').get(idempotencyKey);
  }
  function list(query = {}) {
    const page = Number.parseInt(query.page || '1', 10); const limit = Number.parseInt(query.limit || '25', 10);
    if (!Number.isInteger(page) || page < 1 || !Number.isInteger(limit) || limit < 1 || limit > 100) throw new AppError(400, 'Invalid outbox pagination.');
    const params = {}; let where = '';
    if (query.status) { params.status = String(query.status).toUpperCase(); where = 'WHERE status=@status'; }
    const total = db.prepare(`SELECT COUNT(*) count FROM notification_outbox ${where}`).get(params).count;
    const data = db.prepare(`SELECT * FROM notification_outbox ${where} ORDER BY created_at DESC,id DESC LIMIT @limit OFFSET @offset`).all({ ...params, limit, offset: (page - 1) * limit });
    return { data, pagination: { page, limit, total, total_pages: Math.max(1, Math.ceil(total / limit)) } };
  }
  function processOne(id = null) {
    return db.transaction(() => {
      const row = id ? find.get(id) : db.prepare("SELECT * FROM notification_outbox WHERE status='PENDING' AND datetime(available_at)<=CURRENT_TIMESTAMP ORDER BY id LIMIT 1").get();
      if (!row) return null;
      if (row.status !== 'PENDING') throw new AppError(409, 'Outbox job is not pending.');
      if (row.attempt_count >= maxAttempts) throw new AppError(409, 'Outbox retry limit reached.');
      db.prepare("UPDATE notification_outbox SET status='PROCESSING',locked_at=CURRENT_TIMESTAMP,attempt_count=attempt_count+1,updated_at=CURRENT_TIMESTAMP WHERE id=?").run(row.id);
      // The only Phase 5 provider is deliberately local and performs no network I/O.
      db.prepare("UPDATE notification_outbox SET status='SUCCEEDED',locked_at=NULL,last_error_code=NULL,updated_at=CURRENT_TIMESTAMP WHERE id=?").run(row.id);
      return find.get(row.id);
    })();
  }
  function retry(id, context) {
    const row = find.get(id); if (!row) throw new AppError(404, 'Outbox job not found.');
    if (row.status !== 'FAILED' || row.attempt_count >= maxAttempts) throw new AppError(409, 'Outbox job is not eligible for retry.');
    db.prepare("UPDATE notification_outbox SET status='PENDING',available_at=CURRENT_TIMESTAMP,locked_at=NULL,last_error_code=NULL,updated_at=CURRENT_TIMESTAMP WHERE id=?").run(id);
    auditService.record({ actorUserId: context.actorUserId, action: 'OUTBOX_RETRY', entityType: 'NOTIFICATION_OUTBOX', entityId: id, outcome: 'SUCCESS', requestId: context.requestId, metadata: { channel: 'NOOP' } });
    return find.get(id);
  }
  function cancel(id, context) {
    const row = find.get(id); if (!row) throw new AppError(404, 'Outbox job not found.');
    if (!['PENDING', 'FAILED'].includes(row.status)) throw new AppError(409, 'Outbox job cannot be cancelled.');
    db.prepare("UPDATE notification_outbox SET status='CANCELLED',locked_at=NULL,updated_at=CURRENT_TIMESTAMP WHERE id=?").run(id);
    auditService.record({ actorUserId: context.actorUserId, action: 'OUTBOX_CANCEL', entityType: 'NOTIFICATION_OUTBOX', entityId: id, outcome: 'SUCCESS', requestId: context.requestId, metadata: { channel: 'NOOP' } });
    return find.get(id);
  }
  return { enqueue, list, processOne, retry, cancel };
}

module.exports = { createOutboxService };
