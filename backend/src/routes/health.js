const express = require('express');

function createHealthRouter({ db, version, schemaVersion, config }) {
  const router = express.Router();
  router.get('/', (req, res) => {
    try {
      db.prepare('SELECT 1 AS ok').get();
      res.json({ status: 'ok', database: 'connected', version });
    } catch (error) {
      res.status(503).json({ status: 'error', database: 'unavailable', version });
    }
  });
  router.get('/live', (req, res) => res.json({ status: 'ok', version }));
  router.get('/ready', (req, res) => {
    try {
      db.prepare('SELECT 1 AS ok').get();
      const actual = db.prepare('SELECT MAX(version) version FROM schema_migrations').get().version;
      if (actual !== schemaVersion || !config.authSecret || !config.clientOrigin) throw new Error('not ready');
      res.json({ status: 'ready', version, schema_version: actual, configuration: 'valid' });
    } catch (error) { res.status(503).json({ status: 'not_ready', version }); }
  });
  return router;
}

module.exports = { createHealthRouter };
