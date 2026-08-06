const jwt = require('jsonwebtoken');

function createAuthenticate({ db, config }) {
  const findUser = db.prepare(`
    SELECT id, full_name, email, role, status, security_status, auth_version
    FROM users WHERE id = ?
  `);

  return function authenticate(req, res, next) {
    const header = req.get('authorization') || '';
    const match = header.match(/^Bearer\s+(.+)$/i);
    if (!match) {
      return res.status(401).json({ status: 'error', message: 'Authentication is required.' });
    }

    let payload;
    try {
      payload = jwt.verify(match[1], config.authSecret, {
        algorithms: ['HS256'],
        issuer: config.authIssuer,
        audience: config.authAudience,
      });
      const userId = Number(payload.sub);
      if (!Number.isInteger(userId) || userId <= 0) throw new Error('Invalid subject');
    } catch (error) {
      return res.status(401).json({ status: 'error', message: 'Authentication is invalid or expired.' });
    }

    try {
      const userId = Number(payload.sub);
      const user = findUser.get(userId);
      if (!user || user.status !== 'ACTIVE' || user.security_status !== 'ACTIVE' || payload.ver !== user.auth_version) {
        return res.status(401).json({ status: 'error', message: 'Authentication is invalid or inactive.' });
      }
      req.user = user;
      return next();
    } catch (error) {
      return next(error);
    }
  };
}

module.exports = { createAuthenticate };
