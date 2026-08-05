// Compatibility entry point for the safe, idempotent Phase 2 demonstration seed.
const { main } = require('./src/db/seed');

if (require.main === module) main();

module.exports = { main };
