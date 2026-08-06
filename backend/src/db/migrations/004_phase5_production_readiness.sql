ALTER TABLE users ADD COLUMN security_status TEXT NOT NULL DEFAULT 'ACTIVE'
  CHECK(security_status IN ('ACTIVE', 'LOCKED', 'SUSPENDED'));
ALTER TABLE users ADD COLUMN auth_version INTEGER NOT NULL DEFAULT 1 CHECK(auth_version >= 1);
ALTER TABLE users ADD COLUMN failed_login_count INTEGER NOT NULL DEFAULT 0 CHECK(failed_login_count >= 0);
ALTER TABLE users ADD COLUMN locked_until TEXT;
ALTER TABLE users ADD COLUMN last_login_at TEXT;
ALTER TABLE users ADD COLUMN security_updated_at TEXT;

CREATE TABLE password_recovery_tokens (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  user_id INTEGER NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  token_hash TEXT NOT NULL UNIQUE,
  expires_at TEXT NOT NULL,
  used_at TEXT,
  revoked_at TEXT,
  created_by INTEGER REFERENCES users(id) ON DELETE SET NULL,
  created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_recovery_user_state
ON password_recovery_tokens(user_id, expires_at, used_at, revoked_at);

CREATE TABLE notification_outbox (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  notification_id INTEGER NOT NULL REFERENCES notifications(id) ON DELETE CASCADE,
  channel TEXT NOT NULL DEFAULT 'NOOP' CHECK(channel IN ('NOOP')),
  recipient_reference TEXT NOT NULL,
  status TEXT NOT NULL DEFAULT 'PENDING'
    CHECK(status IN ('PENDING', 'PROCESSING', 'SUCCEEDED', 'FAILED', 'CANCELLED')),
  attempt_count INTEGER NOT NULL DEFAULT 0 CHECK(attempt_count >= 0),
  available_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
  locked_at TEXT,
  last_error_code TEXT,
  idempotency_key TEXT NOT NULL UNIQUE,
  created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_outbox_dispatch
ON notification_outbox(status, available_at, id);

CREATE TABLE data_lifecycle_runs (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  operation TEXT NOT NULL CHECK(operation IN ('RETENTION_PLAN', 'RETENTION_APPLY', 'ANONYMIZATION_DRY_RUN')),
  mode TEXT NOT NULL CHECK(mode IN ('DRY_RUN', 'APPLY')),
  candidate_counts_json TEXT NOT NULL,
  outcome TEXT NOT NULL CHECK(outcome IN ('PLANNED', 'SUCCEEDED', 'FAILED')),
  backup_reference TEXT,
  created_by INTEGER REFERENCES users(id) ON DELETE SET NULL,
  created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_lifecycle_runs_created ON data_lifecycle_runs(created_at DESC);
