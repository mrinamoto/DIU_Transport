const path = require('path');
const dotenv = require('dotenv');

const projectRoot = path.resolve(__dirname, '..', '..', '..');
dotenv.config({ path: path.join(projectRoot, '.env') });

const SAFE_PUBLIC_ROLES = new Set(['STUDENT', 'TEACHER']);

function parsePort(value) {
  const port = Number.parseInt(value, 10);
  if (!Number.isInteger(port) || port < 0 || port > 65535) {
    throw new Error('PORT must be an integer between 0 and 65535.');
  }
  return port;
}

function parsePublicRoles(value) {
  const roles = String(value || 'STUDENT,TEACHER')
    .split(',')
    .map((role) => role.trim().toUpperCase())
    .filter(Boolean);

  if (roles.length === 0 || roles.some((role) => !SAFE_PUBLIC_ROLES.has(role))) {
    throw new Error('PUBLIC_REGISTRATION_ROLES may contain only STUDENT and TEACHER.');
  }

  return [...new Set(roles)];
}

function resolveDatabasePath(value) {
  const configured = value || 'backend/data/diu_transport_web.db';
  return path.isAbsolute(configured) ? configured : path.resolve(projectRoot, configured);
}

function parsePositiveInteger(value, fallback, name, { min = 1, max = Number.MAX_SAFE_INTEGER } = {}) {
  const parsed = Number.parseInt(value ?? fallback, 10);
  if (!Number.isInteger(parsed) || parsed < min || parsed > max) {
    throw new Error(`${name} must be an integer between ${min} and ${max}.`);
  }
  return parsed;
}

function parseTrustProxy(value) {
  const normalized = String(value ?? 'false').trim().toLowerCase();
  if (normalized === 'false') return false;
  if (normalized === 'true') return true;
  const hops = Number.parseInt(normalized, 10);
  if (Number.isInteger(hops) && hops >= 0 && hops <= 10) return hops;
  throw new Error('TRUST_PROXY must be false, true, or a hop count from 0 to 10.');
}

function resolveProjectPath(value, fallback) {
  const configured = value || fallback;
  return path.isAbsolute(configured) ? configured : path.resolve(projectRoot, configured);
}

function createConfig({ env = process.env, requireAuthSecret = true } = {}) {
  const authSecret = env.AUTH_SECRET || '';
  if (requireAuthSecret && authSecret.length < 32) {
    throw new Error('AUTH_SECRET must be configured with at least 32 characters.');
  }

  return Object.freeze({
    projectRoot,
    nodeEnv: env.NODE_ENV || 'development',
    port: parsePort(env.PORT || '5000'),
    clientOrigin: env.CLIENT_ORIGIN || 'http://localhost:5000',
    databasePath: resolveDatabasePath(env.WEB_DATABASE_PATH),
    authSecret,
    authExpiresIn: env.AUTH_EXPIRES_IN || '1h',
    authIssuer: 'diu-transport-web',
    authAudience: 'diu-transport-frontend',
    publicRegistrationRoles: parsePublicRoles(env.PUBLIC_REGISTRATION_ROLES),
    authRateLimitWindowMs: parsePositiveInteger(env.AUTH_RATE_LIMIT_WINDOW_MS, '900000', 'AUTH_RATE_LIMIT_WINDOW_MS'),
    authRateLimitMax: parsePositiveInteger(env.AUTH_RATE_LIMIT_MAX, '20', 'AUTH_RATE_LIMIT_MAX'),
    apiRateLimitWindowMs: parsePositiveInteger(env.API_RATE_LIMIT_WINDOW_MS, '60000', 'API_RATE_LIMIT_WINDOW_MS'),
    apiRateLimitMax: parsePositiveInteger(env.API_RATE_LIMIT_MAX, '300', 'API_RATE_LIMIT_MAX'),
    trustProxy: parseTrustProxy(env.TRUST_PROXY),
    auditPageSizeMax: parsePositiveInteger(env.AUDIT_PAGE_SIZE_MAX, '100', 'AUDIT_PAGE_SIZE_MAX', { max: 250 }),
    backupDirectory: resolveProjectPath(env.BACKUP_DIRECTORY, 'backend/backups'),
    exportDirectory: resolveProjectPath(env.EXPORT_DIRECTORY, 'backend/exports'),
    feedbackRateLimitWindowMs: parsePositiveInteger(env.FEEDBACK_RATE_LIMIT_WINDOW_MS, '600000', 'FEEDBACK_RATE_LIMIT_WINDOW_MS'),
    feedbackRateLimitMax: parsePositiveInteger(env.FEEDBACK_RATE_LIMIT_MAX, '10', 'FEEDBACK_RATE_LIMIT_MAX'),
    notificationPageSizeMax: parsePositiveInteger(env.NOTIFICATION_PAGE_SIZE_MAX, '100', 'NOTIFICATION_PAGE_SIZE_MAX', { max: 250 }),
    feedbackPageSizeMax: parsePositiveInteger(env.FEEDBACK_PAGE_SIZE_MAX, '100', 'FEEDBACK_PAGE_SIZE_MAX', { max: 250 }),
    specialTripPageSizeMax: parsePositiveInteger(env.SPECIAL_TRIP_PAGE_SIZE_MAX, '100', 'SPECIAL_TRIP_PAGE_SIZE_MAX', { max: 250 }),
  });
}

module.exports = { createConfig, projectRoot };
