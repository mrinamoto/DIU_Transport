const { rateLimit } = require('express-rate-limit');

function controlledHandler(metricsService, req, res) {
  metricsService?.increment('rate_limits');
  res.status(429).json({
    status: 'error',
    message: 'Too many requests. Please try again later.',
    request_id: req.id,
  });
}

function createRateLimits(config, metricsService) {
  const shared = {
    standardHeaders: 'draft-8',
    legacyHeaders: false,
    handler: (req, res) => controlledHandler(metricsService, req, res),
  };
  return {
    apiLimiter: rateLimit({ ...shared, windowMs: config.apiRateLimitWindowMs, limit: config.apiRateLimitMax }),
    authLimiter: rateLimit({ ...shared, windowMs: config.authRateLimitWindowMs, limit: config.authRateLimitMax }),
    feedbackLimiter: rateLimit({ ...shared, windowMs: config.feedbackRateLimitWindowMs, limit: config.feedbackRateLimitMax }),
  };
}

module.exports = { createRateLimits };
