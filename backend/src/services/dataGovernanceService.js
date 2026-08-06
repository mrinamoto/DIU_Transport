function createDataGovernanceService(db) {
  function candidateCounts(now = new Date()) {
    const instant = now.toISOString();
    return {
      expired_recovery_tokens: db.prepare(`SELECT COUNT(*) count FROM password_recovery_tokens
        WHERE datetime(expires_at)<datetime(?) AND created_at<datetime(?,'-30 days')`).get(instant, instant).count,
      expired_notifications: db.prepare(`SELECT COUNT(*) count FROM notifications
        WHERE status IN ('EXPIRED','CANCELLED') AND updated_at<datetime(?,'-365 days')`).get(instant).count,
    };
  }
  function plan({ actorUserId = null } = {}) {
    const counts = candidateCounts();
    const info = db.prepare(`INSERT INTO data_lifecycle_runs(operation,mode,candidate_counts_json,outcome,created_by)
      VALUES('RETENTION_PLAN','DRY_RUN',?,'PLANNED',?)`).run(JSON.stringify(counts), actorUserId);
    return { run_id: Number(info.lastInsertRowid), mode: 'DRY_RUN', candidate_counts: counts };
  }
  function apply({ actorUserId = null, backupReference }) {
    const counts = candidateCounts();
    return db.transaction(() => {
      db.prepare(`DELETE FROM password_recovery_tokens WHERE datetime(expires_at)<CURRENT_TIMESTAMP AND created_at<datetime('now','-30 days')`).run();
      db.prepare(`DELETE FROM notifications WHERE status IN ('EXPIRED','CANCELLED') AND updated_at<datetime('now','-365 days')`).run();
      const info = db.prepare(`INSERT INTO data_lifecycle_runs(operation,mode,candidate_counts_json,outcome,backup_reference,created_by)
        VALUES('RETENTION_APPLY','APPLY',?,'SUCCEEDED',?,?)`).run(JSON.stringify(counts), backupReference, actorUserId);
      return { run_id: Number(info.lastInsertRowid), mode: 'APPLY', deleted_counts: counts, backup_reference: backupReference };
    })();
  }
  function anonymizationDryRun(userId) {
    const exists = db.prepare('SELECT id FROM users WHERE id=?').get(userId);
    return { mode: 'DRY_RUN', eligible: Boolean(exists), affected_user_count: exists ? 1 : 0, fields: ['full_name', 'email'] };
  }
  return { candidateCounts, plan, apply, anonymizationDryRun };
}
module.exports = { createDataGovernanceService };
