// backend/middleware/authMiddleware.js
// Verifies a JWT sent by the client and attaches the decoded payload to req.user.
//
// Expected header: Authorization: Bearer <token>

const jwt = require('jsonwebtoken');
require('dotenv').config();

const JWT_SECRET = process.env.JWT_SECRET;

if (!JWT_SECRET) {
  console.warn('⚠️  WARNING: JWT_SECRET is not set in your .env file. Using an insecure fallback.');
}

function authenticateToken(req, res, next) {
  const authHeader = req.headers['authorization'] || '';
  const token = authHeader.startsWith('Bearer ') ? authHeader.slice(7).trim() : null;

  if (!token) {
    return res.status(401).json({
      status: 'error',
      message: 'Access denied. No authentication token was provided.',
    });
  }

  jwt.verify(token, JWT_SECRET || 'insecure_dev_fallback_secret', (err, decoded) => {
    if (err) {
      if (err.name === 'TokenExpiredError') {
        return res.status(401).json({
          status: 'error',
          message: 'Your session has expired. Please log in again.',
        });
      }
      return res.status(403).json({
        status: 'error',
        message: 'Invalid authentication token.',
      });
    }

    // decoded payload shape: { id, email, role, full_name }
    req.user = decoded;
    next();
  });
}

module.exports = authenticateToken;
