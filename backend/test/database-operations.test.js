const fs = require('node:fs');
const os = require('node:os');
const path = require('node:path');
const { createHash } = require('node:crypto');
const Database = require('better-sqlite3');
const { before, after, test } = require('node:test');
const assert = require('node:assert/strict');
const { initializeDatabase, MIGRATION_VERSION } = require('../src/db/connection');
const { seedDatabase } = require('../src/db/seed');
const { createBackup, verifyBackup, createSanitizedExport } = require('../src/db/operations');

let temporaryDirectory; let databasePath; let backupDirectory; let exportDirectory; let backupPath;
const hashFile = (file) => createHash('sha256').update(fs.readFileSync(file)).digest('hex');

before(() => {
  temporaryDirectory=fs.mkdtempSync(path.join(os.tmpdir(),'diu-transport-phase3-operations-'));
  databasePath=path.join(temporaryDirectory,'source.db');backupDirectory=path.join(temporaryDirectory,'backups');exportDirectory=path.join(temporaryDirectory,'exports');
  const db=initializeDatabase(databasePath);seedDatabase(db);db.close();
});
after(()=>{if(temporaryDirectory?.startsWith(os.tmpdir()))fs.rmSync(temporaryDirectory,{recursive:true,force:true});});

test('fresh initialization applies every migration at schema version 2',()=>{
  const db=new Database(databasePath,{readonly:true});
  try{assert.equal(db.prepare('SELECT MAX(version) AS version FROM schema_migrations').get().version,MIGRATION_VERSION);assert.ok(db.prepare("SELECT 1 FROM sqlite_schema WHERE type='table' AND name='audit_logs'").get());assert.ok(db.prepare("SELECT 1 FROM sqlite_schema WHERE type='table' AND name='route_stops'").get());}
  finally{db.close();}
});

test('a Phase 2 database upgrades non-destructively to Phase 3',()=>{
  const upgradePath=path.join(temporaryDirectory,'upgrade.db');const legacy=new Database(upgradePath);
  legacy.exec(fs.readFileSync(path.join(__dirname,'../src/db/migrations/001_phase2_baseline.sql'),'utf8'));
  legacy.prepare("INSERT INTO schema_migrations(version,name) VALUES(1,'phase2_baseline')").run();
  legacy.prepare("INSERT INTO buses(bus_number,capacity) VALUES('UPGRADE-PRESERVED',20)").run();legacy.close();
  const upgraded=initializeDatabase(upgradePath);try{assert.equal(upgraded.prepare('SELECT MAX(version) AS version FROM schema_migrations').get().version,2);assert.ok(upgraded.prepare("SELECT id FROM buses WHERE bus_number='UPGRADE-PRESERVED'").get());assert.ok(upgraded.prepare("SELECT name FROM sqlite_schema WHERE name='audit_logs'").get());}finally{upgraded.close();}
});

test('SQLite-safe backup and temporary restore verification preserve the active database',async()=>{
  const beforeHash=hashFile(databasePath);backupPath=await createBackup(databasePath,backupDirectory);assert.ok(fs.existsSync(backupPath));assert.notEqual(path.resolve(backupPath),path.resolve(databasePath));
  const summary=verifyBackup(backupPath,{sourcePath:databasePath});assert.equal(summary.schemaVersion,2);assert.ok(summary.tables.includes('audit_logs'));assert.equal(hashFile(databasePath),beforeHash);assert.ok(fs.existsSync(backupPath));
});

test('sanitized export manifest preserves structure without credential fields',()=>{
  const beforeHash=hashFile(databasePath);const result=createSanitizedExport(databasePath,exportDirectory);assert.ok(fs.existsSync(result.destination));const payload=JSON.parse(fs.readFileSync(result.destination,'utf8'));
  assert.equal(payload.manifest.schema_version,2);assert.ok(payload.manifest.tables.some((item)=>item.table==='users'));assert.ok(payload.data.users.every((row)=>!('password_hash'in row)&&!('email'in row)&&!('full_name'in row)));assert.ok(payload.data.drivers.every((row)=>!('phone'in row)&&!('full_name'in row)));assert.equal(hashFile(databasePath),beforeHash);
});
