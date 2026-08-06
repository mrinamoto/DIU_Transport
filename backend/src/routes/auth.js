const express = require('express');
const bcrypt = require('bcryptjs');
const jwt = require('jsonwebtoken');
const {
  isNonEmptyString,
  normalizeEmail,
  normalizeRole,
  isValidEmail,
  validatePassword,
} = require('../utils/validation');

const BCRYPT_ROUNDS = 12;

function publicUser(row) {
  return {
    id: row.id,
    full_name: row.full_name,
    email: row.email,
    role: row.role,
    status: row.status,
    security_status: row.security_status,
    created_at: row.created_at,
    updated_at: row.updated_at,
  };
}

function signToken(user, config) {
  return jwt.sign({ ver: user.auth_version }, config.authSecret, {
    algorithm: 'HS256',
    subject: String(user.id),
    expiresIn: config.authExpiresIn,
    issuer: config.authIssuer,
    audience: config.authAudience,
  });
}

function createAuthRouter({ db, config, authenticate, auditService, identityService, metricsService }) {
  const router = express.Router();

  router.post('/register', async (req, res, next) => {
    try {
      const fullName = typeof req.body?.full_name === 'string' ? req.body.full_name.trim() : '';
      const email = normalizeEmail(req.body?.email);
      const role = normalizeRole(req.body?.role);
      const passwordErrors = validatePassword(req.body?.password);
      const errors = [];

      if (!isNonEmptyString(fullName) || fullName.length < 2 || fullName.length > 100) {
        errors.push('Full name must be between 2 and 100 characters.');
      }
      if (!isValidEmail(email)) errors.push('A valid email address is required.');
      if (!role) errors.push('Role is required.');
      else if (!config.publicRegistrationRoles.includes(role)) {
        errors.push(`Public registration allows only: ${config.publicRegistrationRoles.join(', ')}.`);
      }
      errors.push(...passwordErrors);
      if (errors.length) {
        auditService.record({ action: 'AUTH_REGISTRATION_FAILURE', entityType: 'AUTHENTICATION', outcome: 'DENIED', requestId: req.id, metadata: { reason: 'validation_failed' } });
        return res.status(400).json({ status: 'error', message: errors[0], errors });
      }

      if (db.prepare('SELECT id FROM users WHERE email = ?').get(email)) {
        return res.status(409).json({ status: 'error', message: 'An account with this email already exists.' });
      }

      const passwordHash = await bcrypt.hash(req.body.password, BCRYPT_ROUNDS);
      const info = db.prepare(`
        INSERT INTO users (full_name, email, password_hash, role)
        VALUES (?, ?, ?, ?)
      `).run(fullName, email, passwordHash, role);
      const user = db.prepare('SELECT * FROM users WHERE id = ?').get(info.lastInsertRowid);

      return res.status(201).json({
        status: 'success',
        token: signToken(user, config),
        user: publicUser(user),
      });
    } catch (error) {
      return next(error);
    }
  });

  router.post('/login', async (req, res, next) => {
    try {
      const email = normalizeEmail(req.body?.email);
      const password = req.body?.password;
      if (!isValidEmail(email) || typeof password !== 'string' || password.length === 0) {
        auditService.record({ action: 'AUTH_LOGIN_FAILURE', entityType: 'AUTHENTICATION', outcome: 'DENIED', requestId: req.id, metadata: { reason: 'invalid_input' } });
        return res.status(400).json({ status: 'error', message: 'Email and password are required.' });
      }

      let user = db.prepare('SELECT * FROM users WHERE email = ?').get(email);
      const invalid = (reason) => {
        metricsService?.increment('authentication_failures');
        auditService.record({ action: 'AUTH_LOGIN_FAILURE', entityType: 'AUTHENTICATION', outcome: 'DENIED', requestId: req.id, metadata: { reason } });
        return res.status(401).json({ status: 'error', message: 'Invalid email or password.' });
      };
      if (!user || user.status !== 'ACTIVE' || user.security_status === 'SUSPENDED') return invalid(user ? 'unavailable_account' : 'invalid_credentials');
      if (user.security_status === 'LOCKED' && user.locked_until && Date.parse(user.locked_until) > Date.now()) return invalid('locked_account');
      if (user.security_status === 'LOCKED') {
        db.prepare("UPDATE users SET security_status='ACTIVE',failed_login_count=0,locked_until=NULL,security_updated_at=CURRENT_TIMESTAMP WHERE id=?").run(user.id);
        user = db.prepare('SELECT * FROM users WHERE id=?').get(user.id);
      }
      if (!(await bcrypt.compare(password, user.password_hash))) {
        const count = user.failed_login_count + 1;
        if (count >= config.accountLockThreshold) {
          const lockedUntil = new Date(Date.now() + config.accountLockDurationMinutes * 60000).toISOString();
          db.prepare("UPDATE users SET failed_login_count=?,security_status='LOCKED',locked_until=?,auth_version=auth_version+1,security_updated_at=CURRENT_TIMESTAMP WHERE id=?").run(count, lockedUntil, user.id);
          auditService.record({ action: 'AUTH_ACCOUNT_LOCKED', entityType: 'USER', entityId: user.id, outcome: 'DENIED', requestId: req.id, metadata: { duration_minutes: config.accountLockDurationMinutes } });
        } else db.prepare('UPDATE users SET failed_login_count=? WHERE id=?').run(count, user.id);
        return invalid('invalid_credentials');
      }
      db.prepare('UPDATE users SET failed_login_count=0,locked_until=NULL,last_login_at=CURRENT_TIMESTAMP WHERE id=?').run(user.id);
      user = db.prepare('SELECT * FROM users WHERE id=?').get(user.id);

      return res.json({
        status: 'success',
        token: signToken(user, config),
        user: publicUser(user),
      });
    } catch (error) {
      return next(error);
    }
  });

  router.get('/me', authenticate, (req, res) => {
    res.json({ status: 'success', user: publicUser(req.user) });
  });

  router.post('/recovery/complete', async (req, res, next) => {
    try {
      await identityService.completeRecovery(req.body?.token, req.body?.password, req.id);
      res.json({ status: 'success', message: 'Password recovery completed. Sign in again.' });
    } catch (error) { next(error); }
  });

  return router;
}

module.exports = { createAuthRouter, publicUser, signToken, BCRYPT_ROUNDS };
