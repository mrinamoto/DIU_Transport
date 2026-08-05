const express = require('express');

function createCatalogRouter({ db, authenticate }) {
  const router = express.Router();
  router.use(authenticate);

  router.get('/', (req, res, next) => {
    try {
      const routes = db.prepare(`
        SELECT id, route_name, origin, destination FROM routes WHERE status='ACTIVE' ORDER BY route_name
      `).all();
      const buses = db.prepare(`
        SELECT id, bus_number, capacity FROM buses WHERE status='ACTIVE' ORDER BY bus_number
      `).all();
      const drivers = db.prepare(`
        SELECT id, full_name FROM drivers WHERE status='ACTIVE' ORDER BY full_name
      `).all();
      res.json({ status: 'success', data: { routes, buses, drivers } });
    } catch (error) {
      next(error);
    }
  });

  return router;
}

module.exports = { createCatalogRouter };
