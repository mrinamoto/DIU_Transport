function authorize(...allowedRoles) {
  const normalized = new Set(allowedRoles.map((role) => role.toUpperCase()));
  return function requireAuthorizedRole(req, res, next) {
    if (!req.user) {
      return res.status(401).json({ status: 'error', message: 'Authentication is required.' });
    }
    if (!normalized.has(req.user.role)) {
      if (req.auditService && !['GET', 'HEAD', 'OPTIONS'].includes(req.method)) {
        req.auditService.record({
          actorUserId: req.user.id,
          action: 'ADMIN_MUTATION_DENIED',
          entityType: 'AUTHORIZATION',
          outcome: 'DENIED',
          requestId: req.id,
          metadata: { reason: 'insufficient_role' },
        });
      }
      return res.status(403).json({ status: 'error', message: 'You are not authorized to perform this action.' });
    }
    return next();
  };
}

module.exports = { authorize };
