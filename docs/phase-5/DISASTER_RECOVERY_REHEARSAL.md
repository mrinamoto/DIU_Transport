# Disaster Recovery Rehearsal

The isolated Phase 5 test created a schema-v4 SQLite database, used the existing SQLite backup API, restored to a separate temporary path, ran `integrity_check`, verified schema version and all required tables, and compared safe row counts. Identity, recovery, outbox, and lifecycle tables were included. The active database was never overwritten.

Non-binding planning targets: RPO 24 hours, RTO 4 hours, daily encrypted backups, 30-day rolling retention plus approved longer audit retention, and monthly restore verification. These are recommendations, not implemented production guarantees.

Restore must follow `runbooks/BACKUP_RESTORE.md`: isolate the suspect database, validate the candidate backup, restore to a new path, verify integrity/schema/counts and critical authentication/schedule workflows, then obtain approval before switching configuration. Preserve original files and evidence.
