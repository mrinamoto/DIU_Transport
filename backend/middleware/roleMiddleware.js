// backend/middleware/roleMiddleware.js
// Restricts a route to one or more roles. Must run AFTER authMiddleware,
// since it relies on req.user being already set.
//
// Usage:
//   router.get('/admin-only', authenticateToken, requireRole('admin'), handler)
//   router.get('/staff-area', authenticateToken, requireRole('admin', 'staff'), handler)

function requireRole(...allowedRoles) {
  return function (req, res, next) {
    if (!req.user) {
      return res.status(401).json({
        status: 'error',
        message: 'Access denied. Please log in first.',
      });
    }

    if (!allowedRoles.includes(req.user.role)) {
      return res.status(403).json({
        status: 'error',
        message: `Access denied. This action requires one of the following roles: ${allowedRoles.join(', ')}.`,
      });
    }

    next();
  };
}

module.exports = requireRole;
