const express = require('express');
const { authorize } = require('../middleware/authorize');

function createCatalogResourceRouter({ type, authenticate, catalogService }) {
  const router = express.Router();
  router.use(authenticate);

  router.get('/', (req, res, next) => {
    try {
      const data = catalogService.list(type, { isAdmin: req.user.role === 'ADMIN' });
      res.json({ status: 'success', count: data.length, data });
    } catch (error) { next(error); }
  });

  router.get('/:id', (req, res, next) => {
    try {
      const data = catalogService.getById(type, Number(req.params.id), { isAdmin: req.user.role === 'ADMIN' });
      res.json({ status: 'success', data });
    } catch (error) { next(error); }
  });

  router.post('/', authorize('ADMIN'), (req, res, next) => {
    try {
      const data = catalogService.create(type, req.body || {}, { actorUserId: req.user.id, requestId: req.id });
      res.status(201).json({ status: 'success', data });
    } catch (error) { next(error); }
  });

  router.patch('/:id', authorize('ADMIN'), (req, res, next) => {
    try {
      const data = catalogService.update(type, Number(req.params.id), req.body || {}, { actorUserId: req.user.id, requestId: req.id });
      res.json({ status: 'success', data });
    } catch (error) { next(error); }
  });

  router.delete('/:id', authorize('ADMIN'), (req, res, next) => {
    try {
      const data = catalogService.deactivate(type, Number(req.params.id), { actorUserId: req.user.id, requestId: req.id });
      res.json({ status: 'success', message: 'Catalog record deactivated.', data });
    } catch (error) { next(error); }
  });

  return router;
}

module.exports = { createCatalogResourceRouter };
