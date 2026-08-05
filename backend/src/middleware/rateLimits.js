const { rateLimit } = require('express-rate-limit');

function controlledHandler(req, res) {
  res.status(429).json({
    status: 'error',
    message: 'Too many requests. Please try again later.',
    request_id: req.id,
  });
}

function createRateLimits(config) {
  const shared = {
    standardHeaders: 'draft-8',
    legacyHeaders: false,
    handler: controlledHandler,
  };
  return {
    apiLimiter: rateLimit({ ...shared, windowMs: config.apiRateLimitWindowMs, limit: config.apiRateLimitMax }),
    authLimiter: rateLimit({ ...shared, windowMs: config.authRateLimitWindowMs, limit: config.authRateLimitMax }),
  };
}

module.exports = { createRateLimits };
