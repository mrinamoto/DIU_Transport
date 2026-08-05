CREATE TABLE employees (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  employee_code TEXT NOT NULL COLLATE NOCASE UNIQUE,
  full_name TEXT NOT NULL CHECK(length(trim(full_name)) BETWEEN 2 AND 100),
  employee_role TEXT NOT NULL CHECK(employee_role IN ('TRANSPORT_OFFICER', 'HELPER', 'MAINTENANCE', 'OTHER')),
  phone TEXT NOT NULL,
  email TEXT,
  shift TEXT NOT NULL CHECK(shift IN ('MORNING', 'EVENING', 'NIGHT', 'FLEXIBLE')),
  status TEXT NOT NULL DEFAULT 'ACTIVE' CHECK(status IN ('ACTIVE', 'INACTIVE')),
  notes TEXT CHECK(notes IS NULL OR length(notes) <= 500),
  created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE special_trips (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  schedule_id INTEGER NOT NULL UNIQUE REFERENCES schedules(id) ON DELETE RESTRICT,
  category TEXT NOT NULL CHECK(category IN ('EXAM', 'CLUB_EVENT', 'INDUSTRIAL_VISIT', 'OTHER')),
  title TEXT NOT NULL CHECK(length(trim(title)) BETWEEN 2 AND 150),
  description TEXT CHECK(description IS NULL OR length(description) <= 1000),
  organizer TEXT NOT NULL CHECK(length(trim(organizer)) BETWEEN 2 AND 150),
  approval_status TEXT NOT NULL DEFAULT 'DRAFT' CHECK(approval_status IN ('DRAFT', 'APPROVED', 'CANCELLED', 'COMPLETED')),
  requested_by INTEGER REFERENCES users(id) ON DELETE SET NULL,
  approved_by INTEGER REFERENCES users(id) ON DELETE SET NULL,
  approved_at TEXT,
  created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE notifications (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  title TEXT NOT NULL CHECK(length(trim(title)) BETWEEN 2 AND 150),
  message TEXT NOT NULL CHECK(length(trim(message)) BETWEEN 2 AND 1000),
  notification_type TEXT NOT NULL CHECK(notification_type IN ('SCHEDULE_UPDATE', 'SCHEDULE_CANCELLATION', 'NEW_ROUTE', 'SPECIAL_TRIP', 'EMERGENCY', 'GENERAL')),
  audience_role TEXT CHECK(audience_role IS NULL OR audience_role IN ('STUDENT', 'TEACHER', 'STAFF', 'ADMIN')),
  related_entity_type TEXT,
  related_entity_id INTEGER,
  status TEXT NOT NULL DEFAULT 'DRAFT' CHECK(status IN ('DRAFT', 'PUBLISHED', 'EXPIRED', 'CANCELLED')),
  publish_at TEXT,
  expires_at TEXT,
  created_by INTEGER REFERENCES users(id) ON DELETE SET NULL,
  dedupe_key TEXT UNIQUE,
  created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CHECK((related_entity_type IS NULL) = (related_entity_id IS NULL))
);

CREATE TABLE notification_reads (
  notification_id INTEGER NOT NULL REFERENCES notifications(id) ON DELETE CASCADE,
  user_id INTEGER NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  read_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY(notification_id, user_id)
);

CREATE TABLE emergency_contacts (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  contact_name TEXT NOT NULL CHECK(length(trim(contact_name)) BETWEEN 2 AND 100),
  contact_role TEXT NOT NULL CHECK(contact_role IN ('TRANSPORT_MANAGER', 'TRANSPORT_OFFICER', 'EMERGENCY_HOTLINE', 'OTHER')),
  phone TEXT NOT NULL,
  email TEXT,
  availability TEXT NOT NULL CHECK(length(trim(availability)) BETWEEN 2 AND 120),
  display_order INTEGER NOT NULL DEFAULT 0 CHECK(display_order >= 0),
  status TEXT NOT NULL DEFAULT 'ACTIVE' CHECK(status IN ('ACTIVE', 'INACTIVE')),
  created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE feedback (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  submitted_by INTEGER NOT NULL REFERENCES users(id) ON DELETE RESTRICT,
  category TEXT NOT NULL CHECK(category IN ('SCHEDULE', 'ROUTE', 'BUS_SERVICE', 'DRIVER_BEHAVIOR', 'SAFETY', 'APPLICATION', 'OTHER')),
  subject TEXT NOT NULL CHECK(length(trim(subject)) BETWEEN 2 AND 150),
  message TEXT NOT NULL CHECK(length(trim(message)) BETWEEN 2 AND 2000),
  status TEXT NOT NULL DEFAULT 'NEW' CHECK(status IN ('NEW', 'IN_REVIEW', 'RESOLVED', 'CLOSED')),
  admin_response TEXT CHECK(admin_response IS NULL OR length(admin_response) <= 2000),
  assigned_to INTEGER REFERENCES employees(id) ON DELETE SET NULL,
  resolved_at TEXT,
  created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_employees_status_role ON employees(status, employee_role);
CREATE INDEX idx_special_trips_status_created ON special_trips(approval_status, created_at DESC);
CREATE INDEX idx_notifications_active_audience ON notifications(status, audience_role, publish_at, expires_at);
CREATE INDEX idx_notification_reads_user ON notification_reads(user_id, read_at);
CREATE INDEX idx_contacts_status_order ON emergency_contacts(status, display_order, id);
CREATE INDEX idx_feedback_submitter_created ON feedback(submitted_by, created_at DESC);
CREATE INDEX idx_feedback_status_created ON feedback(status, created_at DESC);
