const express = require('express');

function createHealthRouter({ db, version }) {
  const router = express.Router();
  router.get('/', (req, res) => {
    try {
      db.prepare('SELECT 1 AS ok').get();
      res.json({ status: 'ok', database: 'connected', version });
    } catch (error) {
      res.status(503).json({ status: 'error', database: 'unavailable', version });
    }
  });
  return router;
}

module.exports = { createHealthRouter };
