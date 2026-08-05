const express = require('express');
const { authorize } = require('../middleware/authorize');

function createScheduleRouter({ authenticate, scheduleService }) {
  const router = express.Router();
  router.use(authenticate);

  router.get('/', (req, res, next) => {
    try {
      const data = scheduleService.list({ includeCancelled: req.user.role === 'ADMIN' });
      res.json({ status: 'success', count: data.length, data });
    } catch (error) {
      next(error);
    }
  });

  router.get('/:id', (req, res, next) => {
    try {
      const data = scheduleService.getById(Number(req.params.id), {
        includeCancelled: req.user.role === 'ADMIN',
      });
      res.json({ status: 'success', data });
    } catch (error) {
      next(error);
    }
  });

  router.post('/', authorize('ADMIN'), (req, res, next) => {
    try {
      const data = scheduleService.create(req.body || {}, req.user.id);
      res.status(201).json({ status: 'success', data });
    } catch (error) {
      next(error);
    }
  });

  router.patch('/:id', authorize('ADMIN'), (req, res, next) => {
    try {
      const data = scheduleService.update(Number(req.params.id), req.body || {});
      res.json({ status: 'success', data });
    } catch (error) {
      next(error);
    }
  });

  router.delete('/:id', authorize('ADMIN'), (req, res, next) => {
    try {
      const data = scheduleService.deactivate(Number(req.params.id));
      res.json({ status: 'success', message: 'Schedule cancelled.', data });
    } catch (error) {
      next(error);
    }
  });

  return router;
}

module.exports = { createScheduleRouter };
