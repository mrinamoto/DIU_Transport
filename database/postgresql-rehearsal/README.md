# Synthetic PostgreSQL rehearsal boundary

This area is non-production and contains no credentials or real data. The Phase 5 rehearsal is intentionally gated by `POSTGRES_REHEARSAL_SYNTHETIC=true`, an explicitly approved synthetic URL, and a project-local driver/runtime. No PostgreSQL service or dependency is installed by this repository.

Planned sequence: create an empty disposable database, apply `schema.sql`, import generated synthetic fixtures, compare allowlisted row counts and constraints, exercise read-only parity queries, then run `rollback.sql` against only that disposable database. Never point the workflow at an unknown or shared host.

SQLite remains the canonical development database.
