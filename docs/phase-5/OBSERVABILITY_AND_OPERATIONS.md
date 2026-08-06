# Observability and Operations

Each completed HTTP request can emit one JSON line with timestamp, severity, event, request ID, method, route/path, outcome, status, duration, and environment. Bodies, query values, authorization/cookies, passwords, hashes, tokens, feedback, email, database paths, and stack traces are excluded. Audit logging remains separate and answers actor/business-change questions.

- `GET /api/health/live`: process liveness only.
- `GET /api/health/ready`: database query, exact migration version, and required configuration presence; no secret/path values.
- `GET /api/admin/operations/metrics`: administrator-only aggregate request/error/authentication/rate-limit counters, schema version, outbox counts, and process uptime.

Metrics are process-local and reset on restart. They are suitable for a safe development baseline, not billing, compliance, or an SLA. Backup age is not exposed because no authoritative backup registry exists. Runbooks under `runbooks/` define safe response procedures.
