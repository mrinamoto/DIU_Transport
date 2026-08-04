// backend/routes/auth.js
// Authentication routes: register, login, and "who am I".

const express = require('express');
const bcrypt = require('bcryptjs');
const jwt = require('jsonwebtoken');
require('dotenv').config();

const db = require('../database');
const authenticateToken = require('../middleware/authMiddleware');
const { validateRegistration, validateLogin } = require('../utils/validators');

const router = express.Router();

const JWT_SECRET = process.env.JWT_SECRET || 'insecure_dev_fallback_secret';
const JWT_EXPIRES_IN = process.env.JWT_EXPIRES_IN || '7d';
const SALT_ROUNDS = 10;

// Where the frontend should send the user after a successful login,
// based on role. The frontend reads `redirect` from the login response.
function getRedirectForRole(role) {
  return role === 'admin' ? '/admin/admin-dashboard.html' : '/dashboard.html';
}

function signToken(user) {
  return jwt.sign(
    { id: user.id, email: user.email, role: user.role, full_name: user.full_name },
    JWT_SECRET,
    { expiresIn: JWT_EXPIRES_IN }
  );
}

// Shape a DB user row into a safe object (never send password_hash to the client)
function toPublicUser(row) {
  if (!row) return null;
  const { password_hash, ...safe } = row;
  return safe;
}

// ────────────────────────────────────────────────────────────
// POST /api/auth/register
// ────────────────────────────────────────────────────────────
router.post('/register', (req, res) => {
  try {
    const { full_name, email, password, role, student_id, phone, department, semester } = req.body || {};

    const { valid, errors } = validateRegistration(req.body);
    if (!valid) {
      return res.status(400).json({ status: 'error', message: errors[0], errors });
    }

    const normalizedEmail = email.trim().toLowerCase();

    const existing = db.prepare('SELECT id FROM users WHERE email = ?').get(normalizedEmail);
    if (existing) {
      return res.status(409).json({
        status: 'error',
        message: 'An account with this email already exists. Please log in instead.',
      });
    }

    const password_hash = bcrypt.hashSync(password, SALT_ROUNDS);

    const insert = db.prepare(`
      INSERT INTO users (full_name, email, password_hash, role, student_id, phone, department, semester)
      VALUES (@full_name, @email, @password_hash, @role, @student_id, @phone, @department, @semester)
    `);

    const info = insert.run({
      full_name: full_name.trim(),
      email: normalizedEmail,
      password_hash,
      role,
      student_id: role === 'student' ? student_id.trim() : (student_id || null),
      phone: phone || null,
      department: department || null,
      semester: semester || null,
    });

    const newUser = db.prepare('SELECT * FROM users WHERE id = ?').get(info.lastInsertRowid);
    const token = signToken(newUser);

    return res.status(201).json({
      status: 'success',
      message: 'Registration successful! You are now logged in.',
      token,
      user: toPublicUser(newUser),
      redirect: getRedirectForRole(newUser.role),
    });
  } catch (err) {
    console.error('Register error:', err);
    return res.status(500).json({ status: 'error', message: 'Something went wrong while creating your account.' });
  }
});

// ────────────────────────────────────────────────────────────
// POST /api/auth/login
// ────────────────────────────────────────────────────────────
router.post('/login', (req, res) => {
  try {
    const { valid, errors } = validateLogin(req.body);
    if (!valid) {
      return res.status(400).json({ status: 'error', message: errors[0], errors });
    }

    const { email, password } = req.body;
    const normalizedEmail = email.trim().toLowerCase();

    const user = db.prepare('SELECT * FROM users WHERE email = ?').get(normalizedEmail);

    // Use the same generic message whether the email doesn't exist or the
    // password is wrong — this avoids leaking which emails are registered.
    const invalidCredsResponse = () =>
      res.status(401).json({ status: 'error', message: 'Invalid email or password.' });

    if (!user) return invalidCredsResponse();

    if (!user.is_active) {
      return res.status(403).json({
        status: 'error',
        message: 'This account has been deactivated. Please contact the transport office.',
      });
    }

    const passwordMatches = bcrypt.compareSync(password, user.password_hash);
    if (!passwordMatches) return invalidCredsResponse();

    const token = signToken(user);

    return res.json({
      status: 'success',
      message: `Welcome back, ${user.full_name}!`,
      token,
      user: toPublicUser(user),
      redirect: getRedirectForRole(user.role),
    });
  } catch (err) {
    console.error('Login error:', err);
    return res.status(500).json({ status: 'error', message: 'Something went wrong while logging you in.' });
  }
});

// ────────────────────────────────────────────────────────────
// GET /api/auth/me  (requires a valid Bearer token)
// ────────────────────────────────────────────────────────────
router.get('/me', authenticateToken, (req, res) => {
  try {
    const user = db.prepare('SELECT * FROM users WHERE id = ?').get(req.user.id);

    if (!user) {
      return res.status(404).json({ status: 'error', message: 'User account no longer exists.' });
    }

    return res.json({ status: 'success', user: toPublicUser(user) });
  } catch (err) {
    console.error('Me error:', err);
    return res.status(500).json({ status: 'error', message: 'Something went wrong while fetching your profile.' });
  }
});

module.exports = router;
