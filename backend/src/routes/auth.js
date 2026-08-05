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
    created_at: row.created_at,
    updated_at: row.updated_at,
  };
}

function signToken(user, config) {
  return jwt.sign({}, config.authSecret, {
    algorithm: 'HS256',
    subject: String(user.id),
    expiresIn: config.authExpiresIn,
    issuer: config.authIssuer,
    audience: config.authAudience,
  });
}

function createAuthRouter({ db, config, authenticate }) {
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
      if (errors.length) return res.status(400).json({ status: 'error', message: errors[0], errors });

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
        return res.status(400).json({ status: 'error', message: 'Email and password are required.' });
      }

      const user = db.prepare('SELECT * FROM users WHERE email = ?').get(email);
      const invalid = () => res.status(401).json({ status: 'error', message: 'Invalid email or password.' });
      if (!user || user.status !== 'ACTIVE') return invalid();
      if (!(await bcrypt.compare(password, user.password_hash))) return invalid();

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

  return router;
}

module.exports = { createAuthRouter, publicUser, signToken, BCRYPT_ROUNDS };
