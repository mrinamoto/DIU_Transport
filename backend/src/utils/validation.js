const EMAIL_PATTERN = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
const DATE_PATTERN = /^\d{4}-\d{2}-\d{2}$/;
const TIME_PATTERN = /^(?:[01]\d|2[0-3]):[0-5]\d$/;
const SCHEDULE_STATUSES = new Set(['ACTIVE', 'CANCELLED']);
const TRIP_TYPES = new Set(['REGULAR', 'SPECIAL']);
const BUS_STATUSES = new Set(['ACTIVE', 'INACTIVE', 'MAINTENANCE']);
const STANDARD_STATUSES = new Set(['ACTIVE', 'INACTIVE']);
const PHONE_PATTERN = /^[+0-9][0-9 ()-]{6,24}$/;
const PROTECTED_FIELDS = new Set(['id', 'created_at', 'updated_at']);

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

const SCHEDULE_FIELDS = new Set([
  'route_id', 'bus_id', 'driver_id', 'service_date', 'departure_time', 'arrival_time',
  'trip_type', 'status', 'notes',
]);

function validateScheduleFields(input) {
  const errors = [];
  for (const key of Object.keys(input || {})) {
    if (key === 'id') errors.push('Schedule IDs are assigned by the server.');
    else if (['created_at', 'updated_at', 'created_by'].includes(key)) errors.push(`${key} is controlled by the server.`);
    else if (!SCHEDULE_FIELDS.has(key)) errors.push(`Unknown field: ${key}.`);
  }
  return errors;
}

function validateScheduleInput(input, { checkFields = true } = {}) {
  const errors = [];
  if (checkFields) errors.push(...validateScheduleFields(input));

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

function normalizeText(value) {
  return typeof value === 'string' ? value.trim().replace(/\s+/g, ' ') : '';
}

function validateFields(input, allowed) {
  const errors = [];
  for (const key of Object.keys(input || {})) {
    if (PROTECTED_FIELDS.has(key)) errors.push(`${key} is controlled by the server.`);
    else if (!allowed.has(key)) errors.push(`Unknown field: ${key}.`);
  }
  return errors;
}

function validateBusInput(input) {
  const errors = validateFields(input, new Set(['bus_number', 'capacity', 'status']));
  const value = {
    bus_number: normalizeText(input?.bus_number).toUpperCase(),
    capacity: Number(input?.capacity),
    status: normalizeRole(input?.status || 'ACTIVE'),
  };
  if (!value.bus_number || value.bus_number.length > 30) errors.push('Bus number must be 1-30 characters.');
  if (!Number.isInteger(value.capacity) || value.capacity < 1 || value.capacity > 100) {
    errors.push('Capacity must be an integer between 1 and 100.');
  }
  if (!BUS_STATUSES.has(value.status)) errors.push('Bus status must be ACTIVE, INACTIVE, or MAINTENANCE.');
  return { valid: errors.length === 0, errors, value };
}

function validateDriverInput(input) {
  const errors = validateFields(input, new Set(['full_name', 'phone', 'status']));
  const value = {
    full_name: normalizeText(input?.full_name),
    phone: normalizeText(input?.phone),
    status: normalizeRole(input?.status || 'ACTIVE'),
  };
  if (value.full_name.length < 2 || value.full_name.length > 100) errors.push('Driver name must be 2-100 characters.');
  if (!PHONE_PATTERN.test(value.phone)) errors.push('Phone must contain 7-25 practical dialing characters.');
  if (!STANDARD_STATUSES.has(value.status)) errors.push('Driver status must be ACTIVE or INACTIVE.');
  return { valid: errors.length === 0, errors, value };
}

function validateRouteInput(input) {
  const errors = validateFields(input, new Set(['route_name', 'origin', 'destination', 'status', 'stops']));
  const value = {
    route_name: normalizeText(input?.route_name),
    origin: normalizeText(input?.origin),
    destination: normalizeText(input?.destination),
    status: normalizeRole(input?.status || 'ACTIVE'),
    stops: input?.stops === undefined ? [] : input.stops,
  };
  if (!value.route_name || value.route_name.length > 100) errors.push('Route name must be 1-100 characters.');
  if (!value.origin || value.origin.length > 100) errors.push('Origin must be 1-100 characters.');
  if (!value.destination || value.destination.length > 100) errors.push('Destination must be 1-100 characters.');
  if (value.origin && value.destination && value.origin.toLowerCase() === value.destination.toLowerCase()) {
    errors.push('Origin and destination must be different.');
  }
  if (!STANDARD_STATUSES.has(value.status)) errors.push('Route status must be ACTIVE or INACTIVE.');
  if (!Array.isArray(value.stops) || value.stops.length > 50) {
    errors.push('Stops must be an array containing at most 50 names.');
    value.stops = [];
  } else {
    value.stops = value.stops.map(normalizeText);
    if (value.stops.some((stop) => !stop || stop.length > 100)) errors.push('Each stop must be 1-100 characters.');
  }
  return { valid: errors.length === 0, errors, value };
}

const EMPLOYEE_ROLES = new Set(['TRANSPORT_OFFICER', 'HELPER', 'MAINTENANCE', 'OTHER']);
const EMPLOYEE_SHIFTS = new Set(['MORNING', 'EVENING', 'NIGHT', 'FLEXIBLE']);
const SPECIAL_TRIP_CATEGORIES = new Set(['EXAM', 'CLUB_EVENT', 'INDUSTRIAL_VISIT', 'OTHER']);
const SPECIAL_TRIP_STATUSES = new Set(['DRAFT', 'APPROVED', 'CANCELLED', 'COMPLETED']);
const NOTIFICATION_TYPES = new Set(['SCHEDULE_UPDATE', 'SCHEDULE_CANCELLATION', 'NEW_ROUTE', 'SPECIAL_TRIP', 'EMERGENCY', 'GENERAL']);
const NOTIFICATION_STATUSES = new Set(['DRAFT', 'PUBLISHED', 'EXPIRED', 'CANCELLED']);
const AUDIENCE_ROLES = new Set(['STUDENT', 'TEACHER', 'STAFF', 'ADMIN']);
const CONTACT_ROLES = new Set(['TRANSPORT_MANAGER', 'TRANSPORT_OFFICER', 'EMERGENCY_HOTLINE', 'OTHER']);
const FEEDBACK_CATEGORIES = new Set(['SCHEDULE', 'ROUTE', 'BUS_SERVICE', 'DRIVER_BEHAVIOR', 'SAFETY', 'APPLICATION', 'OTHER']);
const FEEDBACK_STATUSES = new Set(['NEW', 'IN_REVIEW', 'RESOLVED', 'CLOSED']);

function optionalText(value, maximum) {
  const normalized = normalizeText(value);
  return normalized ? normalized.slice(0, maximum + 1) : null;
}

function validateEmployeeInput(input) {
  const errors = validateFields(input, new Set(['employee_code', 'full_name', 'employee_role', 'phone', 'email', 'shift', 'status', 'notes']));
  const value = {
    employee_code: normalizeText(input?.employee_code).toUpperCase(),
    full_name: normalizeText(input?.full_name),
    employee_role: normalizeRole(input?.employee_role),
    phone: normalizeText(input?.phone),
    email: optionalText(input?.email, 150)?.toLowerCase() || null,
    shift: normalizeRole(input?.shift),
    status: normalizeRole(input?.status || 'ACTIVE'),
    notes: optionalText(input?.notes, 500),
  };
  if (!value.employee_code || value.employee_code.length > 30) errors.push('Employee code must be 1-30 characters.');
  if (value.full_name.length < 2 || value.full_name.length > 100) errors.push('Employee name must be 2-100 characters.');
  if (!EMPLOYEE_ROLES.has(value.employee_role)) errors.push('Employee role is invalid.');
  if (!PHONE_PATTERN.test(value.phone)) errors.push('Phone must contain 7-25 practical dialing characters.');
  if (value.email && (!isValidEmail(value.email) || value.email.length > 150)) errors.push('Employee email must be valid when provided.');
  if (!EMPLOYEE_SHIFTS.has(value.shift)) errors.push('Employee shift is invalid.');
  if (!STANDARD_STATUSES.has(value.status)) errors.push('Employee status must be ACTIVE or INACTIVE.');
  if (value.notes && value.notes.length > 500) errors.push('Employee notes cannot exceed 500 characters.');
  return { valid: errors.length === 0, errors, value };
}

function validateSpecialTripInput(input) {
  const errors = validateFields(input, new Set(['category', 'title', 'description', 'organizer', 'approval_status']));
  const value = {
    category: normalizeRole(input?.category),
    title: normalizeText(input?.title),
    description: optionalText(input?.description, 1000),
    organizer: normalizeText(input?.organizer),
    approval_status: normalizeRole(input?.approval_status || 'DRAFT'),
  };
  if (!SPECIAL_TRIP_CATEGORIES.has(value.category)) errors.push('Special-trip category is invalid.');
  if (value.title.length < 2 || value.title.length > 150) errors.push('Special-trip title must be 2-150 characters.');
  if (value.description && value.description.length > 1000) errors.push('Special-trip description cannot exceed 1000 characters.');
  if (value.organizer.length < 2 || value.organizer.length > 150) errors.push('Organizer must be 2-150 characters.');
  if (!SPECIAL_TRIP_STATUSES.has(value.approval_status)) errors.push('Special-trip status is invalid.');
  return { valid: errors.length === 0, errors, value };
}

function validOptionalDateTime(value) {
  return value === null || value === undefined || value === '' || (typeof value === 'string' && !Number.isNaN(Date.parse(value)));
}

function validateNotificationInput(input) {
  const errors = validateFields(input, new Set(['title', 'message', 'notification_type', 'audience_role', 'related_entity_type', 'related_entity_id', 'status', 'publish_at', 'expires_at']));
  const value = {
    title: normalizeText(input?.title),
    message: normalizeText(input?.message),
    notification_type: normalizeRole(input?.notification_type || 'GENERAL'),
    audience_role: input?.audience_role ? normalizeRole(input.audience_role) : null,
    related_entity_type: input?.related_entity_type ? normalizeRole(input.related_entity_type) : null,
    related_entity_id: input?.related_entity_id === null || input?.related_entity_id === undefined || input?.related_entity_id === '' ? null : parsePositiveId(input.related_entity_id),
    status: normalizeRole(input?.status || (normalizeRole(input?.notification_type) === 'EMERGENCY' ? 'PUBLISHED' : 'DRAFT')),
    publish_at: input?.publish_at || null,
    expires_at: input?.expires_at || null,
  };
  if (value.title.length < 2 || value.title.length > 150) errors.push('Notification title must be 2-150 characters.');
  if (value.message.length < 2 || value.message.length > 1000) errors.push('Notification message must be 2-1000 characters.');
  if (!NOTIFICATION_TYPES.has(value.notification_type)) errors.push('Notification type is invalid.');
  if (value.audience_role && !AUDIENCE_ROLES.has(value.audience_role)) errors.push('Notification audience role is invalid.');
  if ((value.related_entity_type === null) !== (value.related_entity_id === null)) errors.push('Related entity type and ID must be supplied together.');
  if (!NOTIFICATION_STATUSES.has(value.status)) errors.push('Notification status is invalid.');
  if (!validOptionalDateTime(value.publish_at)) errors.push('Publish time must be a valid date/time.');
  if (!validOptionalDateTime(value.expires_at)) errors.push('Expiry time must be a valid date/time.');
  if (value.publish_at && value.expires_at && Date.parse(value.expires_at) <= Date.parse(value.publish_at)) errors.push('Expiry time must be after publish time.');
  return { valid: errors.length === 0, errors, value };
}

function validateContactInput(input) {
  const errors = validateFields(input, new Set(['contact_name', 'contact_role', 'phone', 'email', 'availability', 'display_order', 'status']));
  const value = {
    contact_name: normalizeText(input?.contact_name),
    contact_role: normalizeRole(input?.contact_role),
    phone: normalizeText(input?.phone),
    email: optionalText(input?.email, 150)?.toLowerCase() || null,
    availability: normalizeText(input?.availability),
    display_order: Number(input?.display_order ?? 0),
    status: normalizeRole(input?.status || 'ACTIVE'),
  };
  if (value.contact_name.length < 2 || value.contact_name.length > 100) errors.push('Contact name must be 2-100 characters.');
  if (!CONTACT_ROLES.has(value.contact_role)) errors.push('Contact role is invalid.');
  if (!PHONE_PATTERN.test(value.phone)) errors.push('Phone must contain 7-25 practical dialing characters.');
  if (value.email && (!isValidEmail(value.email) || value.email.length > 150)) errors.push('Contact email must be valid when provided.');
  if (value.availability.length < 2 || value.availability.length > 120) errors.push('Availability must be 2-120 characters.');
  if (!Number.isInteger(value.display_order) || value.display_order < 0 || value.display_order > 10000) errors.push('Display order must be an integer between 0 and 10000.');
  if (!STANDARD_STATUSES.has(value.status)) errors.push('Contact status must be ACTIVE or INACTIVE.');
  return { valid: errors.length === 0, errors, value };
}

const CREDENTIAL_CONTENT_PATTERN = /(-----BEGIN [A-Z ]*PRIVATE KEY-----|\bBearer\s+[A-Za-z0-9._~-]{20,}|\bpassword\s*[:=]\s*\S{6,}|\beyJ[A-Za-z0-9_-]{15,}\.[A-Za-z0-9_-]{10,}\.)/i;

function validateFeedbackInput(input) {
  const errors = validateFields(input, new Set(['category', 'subject', 'message']));
  const value = {
    category: normalizeRole(input?.category),
    subject: normalizeText(input?.subject),
    message: normalizeText(input?.message),
  };
  if (!FEEDBACK_CATEGORIES.has(value.category)) errors.push('Feedback category is invalid.');
  if (value.subject.length < 2 || value.subject.length > 150) errors.push('Feedback subject must be 2-150 characters.');
  if (value.message.length < 2 || value.message.length > 2000) errors.push('Feedback message must be 2-2000 characters.');
  if (CREDENTIAL_CONTENT_PATTERN.test(`${value.subject}\n${value.message}`)) errors.push('Feedback must not contain credentials or authentication data.');
  return { valid: errors.length === 0, errors, value };
}

function validateFeedbackAdminInput(input) {
  const errors = validateFields(input, new Set(['status', 'admin_response', 'assigned_to']));
  const value = {
    status: input?.status ? normalizeRole(input.status) : undefined,
    admin_response: input?.admin_response === undefined ? undefined : optionalText(input.admin_response, 2000),
    assigned_to: input?.assigned_to === undefined ? undefined : (input.assigned_to === null || input.assigned_to === '' ? null : parsePositiveId(input.assigned_to)),
  };
  if (value.status !== undefined && !FEEDBACK_STATUSES.has(value.status)) errors.push('Feedback status is invalid.');
  if (value.admin_response && value.admin_response.length > 2000) errors.push('Administrator response cannot exceed 2000 characters.');
  if (value.admin_response && CREDENTIAL_CONTENT_PATTERN.test(value.admin_response)) errors.push('Administrator response must not contain credentials or authentication data.');
  if (input?.assigned_to !== undefined && input.assigned_to !== null && input.assigned_to !== '' && !value.assigned_to) errors.push('Assigned employee ID must be valid.');
  return { valid: errors.length === 0, errors, value };
}

module.exports = {
  isNonEmptyString,
  normalizeEmail,
  normalizeRole,
  isValidEmail,
  validatePassword,
  validateScheduleInput,
  validateScheduleFields,
  validateBusInput,
  validateDriverInput,
  validateRouteInput,
  normalizeText,
  validateFields,
  parsePositiveId,
  validateEmployeeInput,
  validateSpecialTripInput,
  validateNotificationInput,
  validateContactInput,
  validateFeedbackInput,
  validateFeedbackAdminInput,
  SPECIAL_TRIP_STATUSES,
};
