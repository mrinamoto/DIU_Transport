# Retention and Privacy Controls

`npm run data:retention:plan` records and prints aggregate candidate counts only. The current allowlist is recovery records expired for more than 30 days and notifications expired/cancelled for more than 365 days. Core users, transport history, feedback, and audit records are not deleted.

Apply is intentionally difficult: `--apply --confirm-retention`, `NODE_ENV=development`, `RETENTION_APPLY_GUARD=ALLOW_SYNTHETIC_RETENTION`, and containment within `RETENTION_ALLOWED_DATABASE_ROOT` are all required. The CLI creates and verifies a SQLite backup before opening a write transaction, then records a `RETENTION_APPLY` lifecycle run. Phase 5 verification exercised the service only against an isolated synthetic temporary database; apply was not run against the normal development database.

`GET /api/account/export` returns the authenticated user's allowlisted account record. `GET /api/admin/users/:id/export` is administrator-only and audited. Password/recovery hashes, lock counters, tokens, audit metadata, feedback, and other users are excluded.

Anonymization exists only as a service-level dry-run returning counts and proposed fields. Synthetic tests prove it does not mutate data. A legal basis, retention schedule, historical-integrity mapping, approval workflow, and rollback procedure are required before an apply capability may be exposed.
