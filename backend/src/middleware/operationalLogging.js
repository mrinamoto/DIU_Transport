function createOperationalLogging({ config, metricsService, sink = console.log }) {
  return function operationalLogging(req, res, next) {
    const started = process.hrtime.bigint();
    metricsService.increment('requests');
    res.once('finish', () => {
      if (res.statusCode >= 500) metricsService.increment('errors');
      if (!config.operationalLogEnabled) return;
      const durationMs = Number(process.hrtime.bigint() - started) / 1e6;
      sink(JSON.stringify({
        timestamp: new Date().toISOString(),
        severity: res.statusCode >= 500 ? 'error' : res.statusCode >= 400 ? 'warn' : 'info',
        event: 'http_request_completed',
        request_id: req.id,
        method: req.method,
        route: req.route?.path || req.path,
        outcome: res.statusCode < 400 ? 'success' : 'failure',
        status_code: res.statusCode,
        duration_ms: Math.round(durationMs * 100) / 100,
        environment: config.nodeEnv,
      }));
    });
    next();
  };
}

module.exports = { createOperationalLogging };
