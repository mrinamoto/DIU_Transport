// Compatibility module for older unmounted routes. It uses only the isolated Phase 2 web DB.
const { createConfig } = require('./src/config');
const { openDatabase } = require('./src/db/connection');

const config = createConfig();
module.exports = openDatabase(config.databasePath);
