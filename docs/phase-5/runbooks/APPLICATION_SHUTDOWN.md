# Application Shutdown
## Trigger or symptom
Maintenance, release rollback, or host shutdown.
## Immediate containment
Stop new traffic and let active requests drain.
## Verification commands
Observe process/port state and record outbox pending/processing counts through protected metrics.
## Safe diagnostic information
Record aggregate counts and process identifier only.
## Recovery steps
Send SIGTERM/CTRL+C, wait for server close and SQLite close, then confirm the port is free.
## Validation after recovery
No listener remains and no outbox row is stranded PROCESSING beyond policy.
## Escalation point
Escalate if graceful shutdown times out or database integrity changes.
## Actions that must not be performed
Do not kill the database mid-write, delete lock/WAL files, or expose process environment.
## Evidence to retain
Shutdown timestamp, aggregate outbox state, and port/process verification.
