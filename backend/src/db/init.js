const { createConfig } = require('../config');
const { initializeDatabase, MIGRATION_VERSION } = require('./connection');

function main() {
  const config = createConfig({ requireAuthSecret: false });
  const db = initializeDatabase(config.databasePath);
  db.close();
  console.log(`Web database initialized at schema version ${MIGRATION_VERSION}.`);
}

if (require.main === module) {
  try {
    main();
  } catch (error) {
    console.error(`Database initialization failed: ${error.message}`);
    process.exitCode = 1;
  }
}

module.exports = { main };
