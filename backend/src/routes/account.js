const express = require('express');

function createAccountRouter({ authenticate, identityService }) {
  const router = express.Router();
  router.use(authenticate);
  router.get('/export', (req, res, next) => { try { res.json({ status: 'success', data: identityService.exportUser(req.user.id) }); } catch (error) { next(error); } });
  return router;
}

module.exports = { createAccountRouter };
