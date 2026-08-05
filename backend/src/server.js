const { createConfig } = require('./config');
const { openDatabase } = require('./db/connection');
const { createApp } = require('./app');

function startServer() {
  const config = createConfig();
  const db = openDatabase(config.databasePath);
  const app = createApp({ db, config });
  const server = app.listen(config.port, () => {
    console.log(`DIU Transport web application listening on http://localhost:${config.port}`);
  });

  function shutdown() {
    server.close(() => {
      db.close();
      process.exit(0);
    });
  }
  process.once('SIGINT', shutdown);
  process.once('SIGTERM', shutdown);
  return { app, server, db, config };
}

if (require.main === module) {
  try {
    startServer();
  } catch (error) {
    console.error(`Startup failed: ${error.message}`);
    process.exitCode = 1;
  }
}

module.exports = { startServer };
