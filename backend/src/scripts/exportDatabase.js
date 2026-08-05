const path = require('node:path');
const { createConfig } = require('../config');
const { createSanitizedExport } = require('../db/operations');

function main() {
  const config = createConfig({ requireAuthSecret: false });
  const result = createSanitizedExport(config.databasePath, config.exportDirectory);
  console.log(`Sanitized migration rehearsal created: ${path.relative(config.projectRoot, result.destination)}`);
}

if (require.main === module) {
  try { main(); } catch (error) { console.error(`Database export failed: ${error.message}`); process.exitCode = 1; }
}

module.exports = { main };
