const express = require('express');
const { authorize } = require('../middleware/authorize');
const { AppError } = require('../middleware/errors');

const FILTER_PATTERN = /^[A-Z][A-Z0-9_]{0,79}$/;
const DATE_PATTERN = /^\d{4}-\d{2}-\d{2}$/;

function createAuditRouter({ db, authenticate, pageSizeMax }) {
  const router = express.Router();
  router.use(authenticate, authorize('ADMIN'));

  router.get('/', (req, res, next) => {
    try {
      const page = Number.parseInt(req.query.page || '1', 10);
      const limit = Number.parseInt(req.query.limit || '25', 10);
      if (!Number.isInteger(page) || page < 1) throw new AppError(400, 'Page must be a positive integer.');
      if (!Number.isInteger(limit) || limit < 1 || limit > pageSizeMax) {
        throw new AppError(400, `Limit must be between 1 and ${pageSizeMax}.`);
      }

      const clauses = [];
      const params = {};
      for (const [queryName, column] of [['action', 'a.action'], ['entity', 'a.entity_type'], ['outcome', 'a.outcome']]) {
        if (!req.query[queryName]) continue;
        const value = String(req.query[queryName]).trim().toUpperCase();
        if (!FILTER_PATTERN.test(value)) throw new AppError(400, `Invalid ${queryName} filter.`);
        clauses.push(`${column}=@${queryName}`);
        params[queryName] = value;
      }
      if (req.query.actor) {
        const actor = Number(req.query.actor);
        if (!Number.isInteger(actor) || actor <= 0) throw new AppError(400, 'Actor must be a positive user ID.');
        clauses.push('a.actor_user_id=@actor');
        params.actor = actor;
      }
      if (req.query.start) {
        if (!DATE_PATTERN.test(req.query.start)) throw new AppError(400, 'Start date must use YYYY-MM-DD.');
        clauses.push('a.created_at>=@start');
        params.start = `${req.query.start} 00:00:00`;
      }
      if (req.query.end) {
        if (!DATE_PATTERN.test(req.query.end)) throw new AppError(400, 'End date must use YYYY-MM-DD.');
        clauses.push('a.created_at<=@end');
        params.end = `${req.query.end} 23:59:59`;
      }

      const where = clauses.length ? `WHERE ${clauses.join(' AND ')}` : '';
      const total = db.prepare(`SELECT COUNT(*) AS count FROM audit_logs a ${where}`).get(params).count;
      const rows = db.prepare(`
        SELECT a.id, a.actor_user_id, u.full_name AS actor_name, a.action, a.entity_type,
               a.entity_id, a.outcome, a.request_id, a.metadata_json, a.created_at
        FROM audit_logs a
        LEFT JOIN users u ON u.id=a.actor_user_id
        ${where}
        ORDER BY a.created_at DESC, a.id DESC
        LIMIT @limit OFFSET @offset
      `).all({ ...params, limit, offset: (page - 1) * limit });
      const data = rows.map((row) => ({ ...row, metadata: JSON.parse(row.metadata_json), metadata_json: undefined }));
      res.json({
        status: 'success', data,
        pagination: { page, limit, total, total_pages: Math.max(1, Math.ceil(total / limit)) },
      });
    } catch (error) { next(error); }
  });

  return router;
}

module.exports = { createAuditRouter };
