const fs = require('fs');
const path = require('path');
const Database = require('better-sqlite3');

const MIGRATIONS = [
  { version: 1, name: 'phase2_baseline', file: '001_phase2_baseline.sql' },
  { version: 2, name: 'phase3_catalog_audit', file: '002_phase3_catalog_audit.sql' },
  { version: 3, name: 'phase4_operations_support', file: '003_phase4_operations_support.sql' },
];
const MIGRATION_VERSION = MIGRATIONS.at(-1).version;

function configureConnection(db) {
  db.pragma('foreign_keys = ON');
  db.pragma('busy_timeout = 5000');
  if (db.name !== ':memory:') db.pragma('journal_mode = WAL');
  return db;
}

function listApplicationTables(db) {
  return db.prepare(`
    SELECT name FROM sqlite_schema
    WHERE type = 'table' AND name NOT LIKE 'sqlite_%'
    ORDER BY name
  `).all().map((row) => row.name);
}

function appliedVersions(db) {
  const table = db.prepare(`
    SELECT 1 FROM sqlite_schema WHERE type = 'table' AND name = 'schema_migrations'
  `).get();
  if (!table) return new Set();
  return new Set(db.prepare('SELECT version FROM schema_migrations').all().map((row) => row.version));
}

function initializeDatabase(databasePath) {
  fs.mkdirSync(path.dirname(databasePath), { recursive: true });
  const existed = fs.existsSync(databasePath);
  const db = configureConnection(new Database(databasePath));

  try {
    const tables = listApplicationTables(db);
    if (existed && tables.length > 0 && !tables.includes('schema_migrations')) {
      throw new Error('Refusing to initialize an existing database with an unknown schema.');
    }

    const applied = appliedVersions(db);
    for (const migration of MIGRATIONS) {
      if (applied.has(migration.version)) continue;
      if (migration.version > 1 && !applied.has(migration.version - 1)) {
        throw new Error(`Cannot apply migration ${migration.version} before migration ${migration.version - 1}.`);
      }
      const migrationSql = fs.readFileSync(path.join(__dirname, 'migrations', migration.file), 'utf8');
      const applyMigration = db.transaction(() => {
        db.exec(migrationSql);
        db.prepare('INSERT INTO schema_migrations (version, name) VALUES (?, ?)')
          .run(migration.version, migration.name);
        db.pragma('optimize');
      });
      applyMigration();
      applied.add(migration.version);
    }

    return db;
  } catch (error) {
    db.close();
    throw error;
  }
}

function openDatabase(databasePath) {
  if (!fs.existsSync(databasePath)) {
    throw new Error('Web database is not initialized. Run npm run db:init first.');
  }

  const db = configureConnection(new Database(databasePath));
  if (!appliedVersions(db).has(MIGRATION_VERSION)) {
    db.close();
    throw new Error('Web database schema is missing or unsupported. Run npm run db:init safely.');
  }
  return db;
}

module.exports = { initializeDatabase, openDatabase, listApplicationTables, MIGRATION_VERSION, MIGRATIONS };
