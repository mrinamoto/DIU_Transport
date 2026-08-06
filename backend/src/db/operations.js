const fs = require('node:fs');
const os = require('node:os');
const path = require('node:path');
const { randomUUID } = require('node:crypto');
const Database = require('better-sqlite3');
const { MIGRATION_VERSION } = require('./connection');

const REQUIRED_TABLES = [
  'audit_logs', 'buses', 'drivers', 'employees', 'emergency_contacts', 'feedback',
  'notification_reads', 'notifications', 'route_stops', 'routes', 'schedules',
  'schema_migrations', 'special_trips', 'users',
  'password_recovery_tokens', 'notification_outbox', 'data_lifecycle_runs',
];

function timestampedName(prefix, extension) {
  const stamp = new Date().toISOString().replace(/[:.]/g, '-');
  return `${prefix}-${stamp}-${randomUUID().slice(0, 8)}.${extension}`;
}

function integrityCheck(db) {
  const rows = db.pragma('integrity_check');
  if (rows.length !== 1 || rows[0].integrity_check !== 'ok') throw new Error('SQLite integrity check failed.');
}

function openExistingDatabase(databasePath) {
  if (!fs.existsSync(databasePath)) throw new Error('Source web database does not exist.');
  return new Database(databasePath, { readonly: true, fileMustExist: true });
}

async function createBackup(sourcePath, backupDirectory) {
  fs.mkdirSync(backupDirectory, { recursive: true });
  const destination = path.join(backupDirectory, timestampedName('diu-transport-backup', 'db'));
  if (fs.existsSync(destination)) throw new Error('Refusing to overwrite an existing backup.');
  const source = openExistingDatabase(sourcePath);
  try {
    integrityCheck(source);
    await source.backup(destination);
  } finally { source.close(); }
  const backup = openExistingDatabase(destination);
  try { integrityCheck(backup); } finally { backup.close(); }
  return destination;
}

function databaseSummary(db) {
  integrityCheck(db);
  db.pragma('foreign_keys = ON');
  const schemaVersion = db.prepare('SELECT MAX(version) AS version FROM schema_migrations').get()?.version;
  if (schemaVersion !== MIGRATION_VERSION) throw new Error(`Backup schema version ${schemaVersion} is unsupported.`);
  const tables = db.prepare("SELECT name FROM sqlite_schema WHERE type='table' AND name NOT LIKE 'sqlite_%'").all().map((row) => row.name);
  for (const required of REQUIRED_TABLES) if (!tables.includes(required)) throw new Error(`Backup is missing required table: ${required}.`);
  const rowCounts = Object.fromEntries(REQUIRED_TABLES.filter((table) => table !== 'schema_migrations')
    .map((table) => [table, db.prepare(`SELECT COUNT(*) AS count FROM ${table}`).get().count]));
  return { schemaVersion, tables: tables.sort(), rowCounts };
}

function verifyBackup(backupPath, { sourcePath } = {}) {
  if (!fs.existsSync(backupPath)) throw new Error('Backup file does not exist.');
  const temporaryDirectory = fs.mkdtempSync(path.join(os.tmpdir(), 'diu-transport-restore-verify-'));
  const verificationPath = path.join(temporaryDirectory, 'verification.db');
  try {
    fs.copyFileSync(backupPath, verificationPath, fs.constants.COPYFILE_EXCL);
    const verification = openExistingDatabase(verificationPath);
    let summary;
    try { summary = databaseSummary(verification); } finally { verification.close(); }
    if (sourcePath) {
      const source = openExistingDatabase(sourcePath);
      try {
        const sourceSummary = databaseSummary(source);
        if (JSON.stringify(sourceSummary.rowCounts) !== JSON.stringify(summary.rowCounts)) {
          throw new Error('Backup safe row counts do not match the source database.');
        }
      } finally { source.close(); }
    }
    return summary;
  } finally {
    fs.rmSync(temporaryDirectory, { recursive: true, force: true });
  }
}

function createSanitizedExport(sourcePath, exportDirectory) {
  fs.mkdirSync(exportDirectory, { recursive: true });
  const destination = path.join(exportDirectory, timestampedName('diu-transport-sanitized-export', 'json'));
  const db = openExistingDatabase(sourcePath);
  try {
    const summary = databaseSummary(db);
    const data = {
      users: db.prepare('SELECT id, role, status, security_status, auth_version, created_at, updated_at FROM users ORDER BY id').all(),
      buses: db.prepare('SELECT id, bus_number, capacity, status, created_at, updated_at FROM buses ORDER BY id').all(),
      drivers: db.prepare('SELECT id, status, created_at, updated_at FROM drivers ORDER BY id').all(),
      routes: db.prepare('SELECT id, route_name, origin, destination, status, created_at, updated_at FROM routes ORDER BY id').all(),
      route_stops: db.prepare('SELECT id, route_id, stop_name, stop_order, created_at, updated_at FROM route_stops ORDER BY route_id, stop_order').all(),
      schedules: db.prepare('SELECT id, route_id, bus_id, driver_id, service_date, departure_time, arrival_time, trip_type, status, notes, created_by, created_at, updated_at FROM schedules ORDER BY id').all(),
      audit_logs: db.prepare('SELECT id, actor_user_id, action, entity_type, entity_id, outcome, request_id, metadata_json, created_at FROM audit_logs ORDER BY id').all(),
      employees: db.prepare('SELECT id, employee_code, employee_role, shift, status, created_at, updated_at FROM employees ORDER BY id').all(),
      special_trips: db.prepare('SELECT id, schedule_id, category, approval_status, requested_by, approved_by, approved_at, created_at, updated_at FROM special_trips ORDER BY id').all(),
      notifications: db.prepare('SELECT id, notification_type, audience_role, related_entity_type, related_entity_id, status, publish_at, expires_at, created_by, dedupe_key, created_at, updated_at FROM notifications ORDER BY id').all(),
      notification_reads: db.prepare('SELECT notification_id, user_id, read_at FROM notification_reads ORDER BY notification_id, user_id').all(),
      emergency_contacts: db.prepare('SELECT id, contact_role, availability, display_order, status, created_at, updated_at FROM emergency_contacts ORDER BY id').all(),
      feedback: db.prepare('SELECT id, submitted_by, category, status, assigned_to, resolved_at, created_at, updated_at FROM feedback ORDER BY id').all(),
      password_recovery_tokens: db.prepare('SELECT id, user_id, expires_at, used_at, revoked_at, created_by, created_at FROM password_recovery_tokens ORDER BY id').all(),
      notification_outbox: db.prepare('SELECT id, notification_id, channel, recipient_reference, status, attempt_count, available_at, last_error_code, idempotency_key, created_at, updated_at FROM notification_outbox ORDER BY id').all(),
      data_lifecycle_runs: db.prepare('SELECT id, operation, mode, candidate_counts_json, outcome, created_at FROM data_lifecycle_runs ORDER BY id').all(),
    };
    const payload = {
      manifest: {
        format: 'phase4-sanitized-json-rehearsal',
        schema_version: summary.schemaVersion,
        generated_at: new Date().toISOString(),
        tables: Object.entries(data).map(([table, rows]) => ({ table, row_count: rows.length })),
        excluded_fields: [
          'users.full_name', 'users.email', 'users.password_hash', 'users.failed_login_count', 'users.locked_until', 'users.last_login_at', 'drivers.full_name', 'drivers.phone',
          'password_recovery_tokens.token_hash',
          'employees.full_name', 'employees.phone', 'employees.email', 'employees.notes',
          'special_trips.title', 'special_trips.description', 'special_trips.organizer',
          'notifications.title', 'notifications.message',
          'emergency_contacts.contact_name', 'emergency_contacts.phone', 'emergency_contacts.email',
          'feedback.subject', 'feedback.message', 'feedback.admin_response',
        ],
        warning: 'Sanitized rehearsal only; not a PostgreSQL import artifact.',
      },
      data,
    };
    fs.writeFileSync(destination, `${JSON.stringify(payload, null, 2)}\n`, { encoding: 'utf8', flag: 'wx' });
    return { destination, manifest: payload.manifest };
  } finally { db.close(); }
}

function latestBackup(backupDirectory) {
  if (!fs.existsSync(backupDirectory)) throw new Error('Backup directory does not exist.');
  const files = fs.readdirSync(backupDirectory).filter((file) => file.endsWith('.db')).sort().reverse();
  if (!files.length) throw new Error('No backup file is available for verification.');
  return path.join(backupDirectory, files[0]);
}

module.exports = { createBackup, verifyBackup, createSanitizedExport, latestBackup, databaseSummary, REQUIRED_TABLES };
