function createMetricsService(db, { enabled = true } = {}) {
  const counters = { requests: 0, errors: 0, authentication_failures: 0, rate_limits: 0 };
  return {
    increment(name) { if (enabled && Object.hasOwn(counters, name)) counters[name] += 1; },
    snapshot() {
      const outbox = db.prepare(`SELECT
        SUM(CASE WHEN status IN ('PENDING','PROCESSING') THEN 1 ELSE 0 END) pending,
        SUM(CASE WHEN status='FAILED' THEN 1 ELSE 0 END) failed FROM notification_outbox`).get();
      const schema = db.prepare('SELECT MAX(version) version FROM schema_migrations').get();
      return {
        enabled,
        counters: { ...counters },
        gauges: {
          schema_version: schema.version,
          outbox_pending: outbox.pending || 0,
          outbox_failed: outbox.failed || 0,
          process_uptime_seconds: Math.floor(process.uptime()),
        },
      };
    },
  };
}

module.exports = { createMetricsService };
