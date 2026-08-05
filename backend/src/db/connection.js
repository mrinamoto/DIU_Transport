const fs = require('fs');
const path = require('path');
const Database = require('better-sqlite3');

const MIGRATION_VERSION = 1;
const MIGRATION_NAME = 'phase2_baseline';
const migrationPath = path.join(__dirname, 'migrations', '001_phase2_baseline.sql');

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

function hasMigration(db) {
  const table = db.prepare(`
    SELECT 1 FROM sqlite_schema WHERE type = 'table' AND name = 'schema_migrations'
  `).get();
  if (!table) return false;
  return Boolean(db.prepare('SELECT 1 FROM schema_migrations WHERE version = ?').get(MIGRATION_VERSION));
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

    if (!hasMigration(db)) {
      const migrationSql = fs.readFileSync(migrationPath, 'utf8');
      const applyMigration = db.transaction(() => {
        db.exec(migrationSql);
        db.prepare('INSERT INTO schema_migrations (version, name) VALUES (?, ?)')
          .run(MIGRATION_VERSION, MIGRATION_NAME);
        db.pragma('optimize');
      });
      applyMigration();
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
  if (!hasMigration(db)) {
    db.close();
    throw new Error('Web database schema is missing or unsupported. Run npm run db:init safely.');
  }
  return db;
}

module.exports = { initializeDatabase, openDatabase, MIGRATION_VERSION };
