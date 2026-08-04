// backend/utils/validators.js
// Small, dependency-free validation helpers used by the auth routes.

const VALID_ROLES = ['student', 'teacher', 'staff', 'admin'];

// Simple, practical email check (not a full RFC 5322 parser, but catches real mistakes)
const EMAIL_REGEX = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;

function isNonEmptyString(value) {
  return typeof value === 'string' && value.trim().length > 0;
}

function validateEmail(email) {
  return isNonEmptyString(email) && EMAIL_REGEX.test(email.trim());
}

function validateRole(role) {
  return VALID_ROLES.includes(role);
}

/**
 * Validates the payload for POST /api/auth/register
 * Returns { valid: boolean, errors: string[] }
 */
function validateRegistration(body) {
  const errors = [];
  const { full_name, email, password, role, student_id } = body || {};

  if (!isNonEmptyString(full_name)) {
    errors.push('Full name is required.');
  }

  if (!isNonEmptyString(email)) {
    errors.push('Email is required.');
  } else if (!validateEmail(email)) {
    errors.push('A valid email address is required.');
  }

  if (!isNonEmptyString(password)) {
    errors.push('Password is required.');
  } else if (password.length < 8) {
    errors.push('Password must be at least 8 characters long.');
  }

  if (!isNonEmptyString(role)) {
    errors.push('Role is required.');
  } else if (!validateRole(role)) {
    errors.push(`Role must be one of: ${VALID_ROLES.join(', ')}.`);
  }

  // student_id is required only when role === 'student'
  if (role === 'student' && !isNonEmptyString(student_id)) {
    errors.push('Student ID is required for the student role.');
  }

  return { valid: errors.length === 0, errors };
}

/**
 * Validates the payload for POST /api/auth/login
 */
function validateLogin(body) {
  const errors = [];
  const { email, password } = body || {};

  if (!isNonEmptyString(email)) errors.push('Email is required.');
  if (!isNonEmptyString(password)) errors.push('Password is required.');

  return { valid: errors.length === 0, errors };
}

module.exports = {
  VALID_ROLES,
  isNonEmptyString,
  validateEmail,
  validateRole,
  validateRegistration,
  validateLogin,
};
