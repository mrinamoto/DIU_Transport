const path = require('node:path');
const { createConfig } = require('../config');
const { openDatabase } = require('../db/connection');
const { createBackup, verifyBackup } = require('../db/operations');
const { createDataGovernanceService } = require('../services/dataGovernanceService');

async function main(argv = process.argv.slice(2)) {
  const config = createConfig({ requireAuthSecret: false });
  const apply = argv.includes('--apply');
  if (!apply) {
    const db = openDatabase(config.databasePath);
    try { console.log(JSON.stringify(createDataGovernanceService(db).plan(), null, 2)); } finally { db.close(); }
    return;
  }
  const relativeToApprovedRoot = config.retentionAllowedDatabaseRoot ? path.relative(config.retentionAllowedDatabaseRoot, config.databasePath) : '..';
  const databaseIsApproved = relativeToApprovedRoot && !relativeToApprovedRoot.startsWith('..') && !path.isAbsolute(relativeToApprovedRoot);
  if (!argv.includes('--confirm-retention') || config.nodeEnv !== 'development' || config.retentionApplyGuard !== 'ALLOW_SYNTHETIC_RETENTION' || !databaseIsApproved) {
    throw new Error('Retention apply requires --confirm-retention, NODE_ENV=development, RETENTION_APPLY_GUARD=ALLOW_SYNTHETIC_RETENTION, and a database inside RETENTION_ALLOWED_DATABASE_ROOT.');
  }
  const backup = await createBackup(config.databasePath, config.backupDirectory);
  verifyBackup(backup, { sourcePath: config.databasePath });
  const db = openDatabase(config.databasePath);
  try { console.log(JSON.stringify(createDataGovernanceService(db).apply({ backupReference: path.basename(backup) }), null, 2)); } finally { db.close(); }
}
if (require.main === module) main().catch((error) => { console.error(`Retention failed: ${error.message}`); process.exitCode = 1; });
module.exports = { main };
