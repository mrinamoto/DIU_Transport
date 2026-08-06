-- Synthetic rehearsal scaffold only. Complete and validate type/constraint parity
-- in an approved disposable PostgreSQL environment before execution.
BEGIN;
CREATE TABLE rehearsal_manifest (
  id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  schema_version INTEGER NOT NULL CHECK (schema_version = 4),
  fixture_set TEXT NOT NULL,
  created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);
COMMIT;
