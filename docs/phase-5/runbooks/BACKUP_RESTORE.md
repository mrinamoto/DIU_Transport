# Backup Restore
## Trigger or symptom
Approved recovery test or restoration after verified data loss/corruption.
## Immediate containment
Keep the active path untouched and restrict access to backup material.
## Verification commands
Run `npm run db:backup:verify` and validate integrity, schema version, required tables, and safe row counts.
## Safe diagnostic information
Use relative backup reference, timestamps, versions, and aggregate counts.
## Recovery steps
Copy the verified backup to a new isolated path, configure a test process to it, and never overwrite active data.
## Validation after recovery
Run check/tests and critical login, schedule, identity, audit, outbox, and readiness flows.
## Escalation point
Escalate any parity/integrity mismatch or uncertainty about backup provenance.
## Actions that must not be performed
Do not restore over active SQLite files or expose/commit backup contents.
## Evidence to retain
Backup fingerprint/reference, integrity result, parity counts, commands, timestamps, and approval.
