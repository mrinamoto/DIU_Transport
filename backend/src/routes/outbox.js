const express = require('express');
const { authorize } = require('../middleware/authorize');
function createOutboxRouter({ authenticate, outboxService }) {
  const router = express.Router(); router.use(authenticate, authorize('ADMIN'));
  router.get('/', (req, res, next) => { try { res.json({ status: 'success', ...outboxService.list(req.query) }); } catch (error) { next(error); } });
  router.post('/:id/retry', (req, res, next) => { try { res.json({ status: 'success', data: outboxService.retry(Number(req.params.id), { actorUserId: req.user.id, requestId: req.id }) }); } catch (error) { next(error); } });
  router.post('/:id/cancel', (req, res, next) => { try { res.json({ status: 'success', data: outboxService.cancel(Number(req.params.id), { actorUserId: req.user.id, requestId: req.id }) }); } catch (error) { next(error); } });
  return router;
}
module.exports = { createOutboxRouter };
