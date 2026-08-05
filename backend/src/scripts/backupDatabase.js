const path = require('node:path');
const { createConfig } = require('../config');
const { createBackup } = require('../db/operations');

async function main() {
  const config = createConfig({ requireAuthSecret: false });
  const destination = await createBackup(config.databasePath, config.backupDirectory);
  console.log(`Verified SQLite backup created: ${path.relative(config.projectRoot, destination)}`);
}

if (require.main === module) main().catch((error) => { console.error(`Database backup failed: ${error.message}`); process.exitCode = 1; });

module.exports = { main };
