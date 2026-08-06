# Application Startup
## Trigger or symptom
Planned startup or readiness failure.
## Immediate containment
Keep traffic disabled until readiness succeeds.
## Verification commands
Run `npm ci`, `npm run check`, `npm run db:init`, then `npm start`; query `/api/health/live` and `/api/health/ready`.
## Safe diagnostic information
Record versions, request IDs, status codes, and schema version; redact environment values and paths.
## Recovery steps
Correct validated configuration, verify the database backup, apply append-only migrations, and restart once.
## Validation after recovery
Require live/ready success, protected metrics access, login, and schedule read.
## Escalation point
Escalate on migration, integrity, or repeated readiness failure.
## Actions that must not be performed
Do not print secrets, bypass readiness, edit historical migrations, or point at an unknown database.
## Evidence to retain
Redacted timestamps, versions, request IDs, check output, and approval record.
