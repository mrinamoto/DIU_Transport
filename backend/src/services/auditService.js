const ALLOWED_METADATA_KEYS = new Set([
  'changed_fields', 'conflict_type', 'reason', 'previous_status', 'new_status',
  'source', 'record_count', 'schema_version',
]);
const SENSITIVE_KEY_PATTERN = /(password|hash|token|cookie|authorization|secret|header|sql)/i;

function sanitizeMetadata(metadata = {}) {
  const clean = {};
  for (const [key, rawValue] of Object.entries(metadata || {})) {
    if (!ALLOWED_METADATA_KEYS.has(key) || SENSITIVE_KEY_PATTERN.test(key)) continue;
    if (Array.isArray(rawValue)) {
      clean[key] = rawValue.slice(0, 20).map((item) => String(item).slice(0, 80));
    } else if (['string', 'number', 'boolean'].includes(typeof rawValue) || rawValue === null) {
      clean[key] = typeof rawValue === 'string' ? rawValue.slice(0, 120) : rawValue;
    }
  }
  return clean;
}

function createAuditService(db) {
  const insert = db.prepare(`
    INSERT INTO audit_logs (
      actor_user_id, action, entity_type, entity_id, outcome, request_id, metadata_json
    ) VALUES (
      @actor_user_id, @action, @entity_type, @entity_id, @outcome, @request_id, @metadata_json
    )
  `);

  function record({ actorUserId = null, action, entityType, entityId = null, outcome, requestId, metadata }) {
    insert.run({
      actor_user_id: actorUserId,
      action: String(action).slice(0, 80),
      entity_type: String(entityType).slice(0, 50),
      entity_id: entityId,
      outcome,
      request_id: String(requestId || 'system-operation').slice(0, 128),
      metadata_json: JSON.stringify(sanitizeMetadata(metadata)),
    });
  }

  return { record, sanitizeMetadata };
}

module.exports = { createAuditService, sanitizeMetadata, ALLOWED_METADATA_KEYS };
