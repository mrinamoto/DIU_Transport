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
const { createEmployeeService } = require('./services/employeeService');
const { createNotificationService } = require('./services/notificationService');
const { createSpecialTripService } = require('./services/specialTripService');
const { createContactService } = require('./services/contactService');
const { createFeedbackService } = require('./services/feedbackService');
const { createEmployeeRouter } = require('./routes/employees');
const { createNotificationRouter } = require('./routes/notifications');
const { createSpecialTripRouter } = require('./routes/specialTrips');
const { createContactRouter } = require('./routes/contacts');
const { createFeedbackRouter, createAdminFeedbackRouter } = require('./routes/feedback');

function createApp({ db, config }) {
  const app = express();
  const frontendDirectory = path.join(config.projectRoot, 'frontend');
  const auditService = createAuditService(db);
  const authenticate = createAuthenticate({ db, config });
  const notificationService = createNotificationService(db, auditService, { pageSizeMax: config.notificationPageSizeMax });
  const scheduleService = createScheduleService(db, auditService, notificationService);
  const catalogService = createCatalogService(db, auditService);
  const employeeService = createEmployeeService(db, auditService);
  const contactService = createContactService(db, auditService);
  const feedbackService = createFeedbackService(db, auditService, { pageSizeMax: config.feedbackPageSizeMax });
  const specialTripService = createSpecialTripService(db, auditService, scheduleService, notificationService, { pageSizeMax: config.specialTripPageSizeMax });
  const { apiLimiter, authLimiter, feedbackLimiter } = createRateLimits(config);

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
  app.use('/api/employees', createEmployeeRouter({ authenticate, employeeService }));
  app.use('/api/special-trips', createSpecialTripRouter({ authenticate, specialTripService }));
  app.use('/api/notifications', createNotificationRouter({ authenticate, notificationService }));
  app.use('/api/contacts', createContactRouter({ authenticate, contactService }));
  app.use('/api/feedback', createFeedbackRouter({ authenticate, feedbackService, feedbackLimiter }));
  app.use('/api/admin/feedback', createAdminFeedbackRouter({ authenticate, feedbackService }));
  app.use('/api', notFound);

  app.use(express.static(frontendDirectory));
  app.get('*', (req, res) => res.sendFile(path.join(frontendDirectory, 'index.html')));
  app.use(errorHandler);

  return app;
}

module.exports = { createApp };
