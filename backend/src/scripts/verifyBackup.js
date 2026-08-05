const path = require('node:path');
const { createConfig } = require('../config');
const { verifyBackup, latestBackup } = require('../db/operations');

function main() {
  const config = createConfig({ requireAuthSecret: false });
  const backupPath = process.env.BACKUP_FILE
    ? (path.isAbsolute(process.env.BACKUP_FILE) ? process.env.BACKUP_FILE : path.resolve(config.projectRoot, process.env.BACKUP_FILE))
    : latestBackup(config.backupDirectory);
  const summary = verifyBackup(backupPath, { sourcePath: config.databasePath });
  console.log(`Backup restore verification passed at schema version ${summary.schemaVersion}; temporary verification copy removed.`);
}

if (require.main === module) {
  try { main(); } catch (error) { console.error(`Backup verification failed: ${error.message}`); process.exitCode = 1; }
}

module.exports = { main };
