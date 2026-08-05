// Compatibility entry point. The canonical Phase 2 server lives under backend/src.
const { startServer } = require('./src/server');

if (require.main === module) startServer();

module.exports = { startServer };
