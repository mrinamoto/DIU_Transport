const express = require('express');
const { authorize } = require('../middleware/authorize');

function createAdminUsersRouter({ authenticate, identityService }) {
  const router = express.Router();
  router.use(authenticate, authorize('ADMIN'));
  router.get('/', (req, res, next) => { try { res.json({ status: 'success', ...identityService.list(req.query) }); } catch (error) { next(error); } });
  router.get('/:id', (req, res, next) => { try { res.json({ status: 'success', data: identityService.get(Number(req.params.id)) }); } catch (error) { next(error); } });
  router.patch('/:id', (req, res, next) => { try { res.json({ status: 'success', data: identityService.change(Number(req.params.id), req.body || {}, { actorUserId: req.user.id, requestId: req.id }) }); } catch (error) { next(error); } });
  router.post('/:id/revoke', (req, res, next) => { try { res.json({ status: 'success', data: identityService.revoke(Number(req.params.id), { actorUserId: req.user.id, requestId: req.id }) }); } catch (error) { next(error); } });
  router.post('/:id/recovery', (req, res, next) => { try { res.status(201).json({ status: 'success', data: identityService.initiateRecovery(Number(req.params.id), { actorUserId: req.user.id, requestId: req.id }) }); } catch (error) { next(error); } });
  router.get('/:id/export', (req, res, next) => { try { res.json({ status: 'success', data: identityService.exportUser(Number(req.params.id), { actorUserId: req.user.id, requestId: req.id }) }); } catch (error) { next(error); } });
  return router;
}

module.exports = { createAdminUsersRouter };
