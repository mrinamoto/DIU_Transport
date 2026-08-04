// backend/utils/pagination.js
// Small helper so every admin list endpoint supports ?page=&limit= consistently.

function getPagination(query, defaultLimit = 10, maxLimit = 100) {
  let page = parseInt(query.page, 10);
  let limit = parseInt(query.limit, 10);

  if (!Number.isFinite(page) || page < 1) page = 1;
  if (!Number.isFinite(limit) || limit < 1) limit = defaultLimit;
  if (limit > maxLimit) limit = maxLimit;

  const offset = (page - 1) * limit;
  return { page, limit, offset };
}

function buildPaginatedResponse(rows, totalCount, page, limit) {
  return {
    data: rows,
    pagination: {
      page,
      limit,
      total: totalCount,
      totalPages: Math.max(1, Math.ceil(totalCount / limit)),
    },
  };
}

module.exports = { getPagination, buildPaginatedResponse };
