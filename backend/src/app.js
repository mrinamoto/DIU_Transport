const path = require('path');
const express = require('express');
const cors = require('cors');
const helmet = require('helmet');
const packageJson = require('../../package.json');
const { createAuthenticate } = require('./middleware/authenticate');
const { notFound, errorHandler, AppError } = require('./middleware/errors');
const { createAuthRouter } = require('./routes/auth');
const { createCatalogRouter } = require('./routes/catalog');
const { createHealthRouter } = require('./routes/health');
const { createScheduleRouter } = require('./routes/schedules');
const { createScheduleService } = require('./services/scheduleService');
const { createAuditService } = require('./services/auditService');
const { createCatalogService } = require('./services/catalogService');
const { createCatalogResourceRouter } = require('./routes/catalogResources');
const { createAuditRouter } = require('./routes/audit');
const { requestId } = require('./middleware/requestId');
const { createRateLimits } = require('./middleware/rateLimits');

function createApp({ db, config }) {
  const app = express();
  const frontendDirectory = path.join(config.projectRoot, 'frontend');
  const auditService = createAuditService(db);
  const authenticate = createAuthenticate({ db, config });
  const scheduleService = createScheduleService(db, auditService);
  const catalogService = createCatalogService(db, auditService);
  const { apiLimiter, authLimiter } = createRateLimits(config);

  app.disable('x-powered-by');
  app.set('trust proxy', config.trustProxy);
  app.use(requestId);
  app.use(helmet());
  app.use(cors({
    origin(origin, callback) {
      if (!origin || origin === config.clientOrigin) return callback(null, true);
      return callback(new AppError(403, 'Origin is not allowed.'));
    },
  }));
  app.use(express.json({ limit: '100kb' }));
  app.use(express.urlencoded({ extended: false, limit: '100kb' }));
  app.use((req, res, next) => { req.auditService = auditService; next(); });
  app.use('/api', apiLimiter);
  app.use('/api/auth/login', authLimiter);
  app.use('/api/auth/register', authLimiter);

  app.use('/api/health', createHealthRouter({ db, version: packageJson.version }));
  app.use('/api/auth', createAuthRouter({ db, config, authenticate, auditService }));
  app.use('/api/catalog', createCatalogRouter({ db, authenticate }));
  app.use('/api/schedules', createScheduleRouter({ authenticate, scheduleService }));
  for (const type of ['buses', 'drivers', 'routes']) {
    app.use(`/api/${type}`, createCatalogResourceRouter({ type, authenticate, catalogService }));
  }
  app.use('/api/audit-logs', createAuditRouter({ db, authenticate, pageSizeMax: config.auditPageSizeMax }));
  app.use('/api', notFound);

  app.use(express.static(frontendDirectory));
  app.get('*', (req, res) => res.sendFile(path.join(frontendDirectory, 'index.html')));
  app.use(errorHandler);

  return app;
}

module.exports = { createApp };
