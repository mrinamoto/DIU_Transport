# Database Corruption
## Trigger or symptom
SQLite integrity failure, malformed database, repeated I/O errors, or impossible row state.
## Immediate containment
Stop writes, isolate the application, and preserve the database plus WAL/SHM as evidence.
## Verification commands
Run read-only integrity checks and backup summary against copies.
## Safe diagnostic information
Record integrity result, schema version, file timestamps/sizes, and safe row counts—not records.
## Recovery steps
Select a verified backup and follow the separate restore runbook into a new path.
## Validation after recovery
Integrity, schema, foreign keys, safe counts, login, schedules, audit, and outbox checks pass.
## Escalation point
Escalate when no verified backup meets the recovery objective or evidence suggests storage failure.
## Actions that must not be performed
Do not overwrite the source, delete WAL files, run ad hoc repair SQL, or use production data in rehearsal.
## Evidence to retain
Original immutable copy, hashes, check outputs, backup identity, and decision log.
