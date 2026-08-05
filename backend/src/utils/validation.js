const EMAIL_PATTERN = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
const DATE_PATTERN = /^\d{4}-\d{2}-\d{2}$/;
const TIME_PATTERN = /^(?:[01]\d|2[0-3]):[0-5]\d$/;
const SCHEDULE_STATUSES = new Set(['ACTIVE', 'CANCELLED']);
const TRIP_TYPES = new Set(['REGULAR', 'SPECIAL']);

function isNonEmptyString(value) {
  return typeof value === 'string' && value.trim().length > 0;
}

function normalizeEmail(value) {
  return typeof value === 'string' ? value.trim().toLowerCase() : '';
}

function normalizeRole(value) {
  return typeof value === 'string' ? value.trim().toUpperCase() : '';
}

function isValidEmail(value) {
  return EMAIL_PATTERN.test(normalizeEmail(value));
}

function validatePassword(value) {
  const errors = [];
  if (typeof value !== 'string' || value.trim().length === 0) {
    return ['Password is required.'];
  }
  if (value.length < 12) errors.push('Password must be at least 12 characters.');
  if (!/[a-z]/.test(value)) errors.push('Password must include a lowercase letter.');
  if (!/[A-Z]/.test(value)) errors.push('Password must include an uppercase letter.');
  if (!/\d/.test(value)) errors.push('Password must include a number.');
  if (!/[^A-Za-z0-9]/.test(value)) errors.push('Password must include a special character.');
  return errors;
}

function parsePositiveId(value) {
  const parsed = Number(value);
  return Number.isInteger(parsed) && parsed > 0 ? parsed : null;
}

function isValidDate(value) {
  if (!DATE_PATTERN.test(value || '')) return false;
  const date = new Date(`${value}T00:00:00Z`);
  return !Number.isNaN(date.valueOf()) && date.toISOString().slice(0, 10) === value;
}

function validateScheduleInput(input) {
  const errors = [];
  if (Object.prototype.hasOwnProperty.call(input || {}, 'id')) {
    errors.push('Schedule IDs are assigned by the server.');
  }

  const normalized = {
    route_id: parsePositiveId(input?.route_id),
    bus_id: parsePositiveId(input?.bus_id),
    driver_id: parsePositiveId(input?.driver_id),
    service_date: typeof input?.service_date === 'string' ? input.service_date.trim() : '',
    departure_time: typeof input?.departure_time === 'string' ? input.departure_time.trim() : '',
    arrival_time: typeof input?.arrival_time === 'string' ? input.arrival_time.trim() : '',
    trip_type: typeof input?.trip_type === 'string' ? input.trip_type.trim().toUpperCase() : 'REGULAR',
    status: typeof input?.status === 'string' ? input.status.trim().toUpperCase() : 'ACTIVE',
    notes: typeof input?.notes === 'string' && input.notes.trim() ? input.notes.trim() : null,
  };

  if (!normalized.route_id) errors.push('A valid route is required.');
  if (!normalized.bus_id) errors.push('A valid bus is required.');
  if (!normalized.driver_id) errors.push('A valid driver is required.');
  if (!isValidDate(normalized.service_date)) errors.push('Service date must use YYYY-MM-DD.');
  if (!TIME_PATTERN.test(normalized.departure_time)) errors.push('Departure time must use 24-hour HH:MM.');
  if (!TIME_PATTERN.test(normalized.arrival_time)) errors.push('Arrival time must use 24-hour HH:MM.');
  if (TIME_PATTERN.test(normalized.departure_time) && TIME_PATTERN.test(normalized.arrival_time)
      && normalized.arrival_time <= normalized.departure_time) {
    errors.push('Arrival time must be later than departure time.');
  }
  if (!TRIP_TYPES.has(normalized.trip_type)) errors.push('Trip type must be REGULAR or SPECIAL.');
  if (!SCHEDULE_STATUSES.has(normalized.status)) errors.push('Status must be ACTIVE or CANCELLED.');
  if (normalized.notes && normalized.notes.length > 500) errors.push('Notes cannot exceed 500 characters.');

  return { valid: errors.length === 0, errors, value: normalized };
}

module.exports = {
  isNonEmptyString,
  normalizeEmail,
  normalizeRole,
  isValidEmail,
  validatePassword,
  validateScheduleInput,
};
