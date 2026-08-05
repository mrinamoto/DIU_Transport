class AppError extends Error {
  constructor(statusCode, message, details) {
    super(message);
    this.statusCode = statusCode;
    this.details = details;
  }
}

function notFound(req, res) {
  res.status(404).json({ status: 'error', message: 'API endpoint not found.' });
}

function errorHandler(error, req, res, next) {
  if (res.headersSent) return next(error);
  const statusCode = error.statusCode || 500;
  if (statusCode >= 500) console.error('Unhandled application error:', error);
  const payload = {
    status: 'error',
    message: statusCode >= 500 ? 'Internal server error.' : error.message,
  };
  if (statusCode < 500 && error.details) payload.errors = error.details;
  return res.status(statusCode).json(payload);
}

module.exports = { AppError, notFound, errorHandler };
