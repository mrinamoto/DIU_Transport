const express = require('express');
const { authorize } = require('../middleware/authorize');
function createOperationsRouter({ authenticate, metricsService }) {
  const router = express.Router(); router.use(authenticate, authorize('ADMIN'));
  router.get('/metrics', (req, res) => res.json({ status: 'success', data: metricsService.snapshot() }));
  return router;
}
module.exports = { createOperationsRouter };
