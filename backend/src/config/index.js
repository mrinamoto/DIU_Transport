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
  });
}

module.exports = { createConfig, projectRoot };
