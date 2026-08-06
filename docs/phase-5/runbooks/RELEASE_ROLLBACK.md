# Release Rollback
## Trigger or symptom
Failed readiness, critical regression, migration incompatibility, or approved incident rollback.
## Immediate containment
Stop promotion and new traffic; preserve logs, database, and release artifacts.
## Verification commands
Record Git/release identity, schema version, live/ready, test result, and backup verification.
## Safe diagnostic information
Use hashes, versions, aggregate counts, request IDs, and redacted errors.
## Recovery steps
Deploy the previously approved application artifact; if data rollback is approved, restore a verified backup to a new path. Append a forward fix rather than editing applied migrations.
## Validation after recovery
Readiness, regressions, authentication, schedules, audit, outbox, and backup verification pass.
## Escalation point
Escalate when schema/data compatibility prevents safe application rollback.
## Actions that must not be performed
Do not force-push, rewrite history, edit applied migrations, or overwrite active data without approval.
## Evidence to retain
Release identifiers, approvals, commands/results, backup reference, timeline, and validation evidence.
