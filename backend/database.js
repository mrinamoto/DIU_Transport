// backend/database.js
// Sets up the SQLite connection using better-sqlite3 and ensures the schema exists.

const path = require('path');
const fs = require('fs');
const Database = require('better-sqlite3');
require('dotenv').config();

const DB_PATH = process.env.DB_PATH || path.join(__dirname, '..', 'diu_transport.db');

const db = new Database(DB_PATH);
db.pragma('journal_mode = WAL');
db.pragma('foreign_keys = ON');

function initializeSchema() {
  const schemaPath = path.join(__dirname, 'models', 'db-schema.sql');
  const schemaSql = fs.readFileSync(schemaPath, 'utf8');
  db.exec(schemaSql);
  console.log('✅ Database schema ensured at:', DB_PATH);
}

initializeSchema();

module.exports = db;
