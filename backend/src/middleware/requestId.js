const { randomUUID } = require('node:crypto');

const SAFE_REQUEST_ID = /^[A-Za-z0-9][A-Za-z0-9._:-]{7,127}$/;

function requestId(req, res, next) {
  const supplied = req.get('x-request-id');
  req.id = typeof supplied === 'string' && SAFE_REQUEST_ID.test(supplied) ? supplied : randomUUID();
  res.set('X-Request-ID', req.id);
  next();
}

module.exports = { requestId, SAFE_REQUEST_ID };
