# PostgreSQL Rehearsal

Status: DEFERRED, not passed.

Read-only environment inspection found no `docker` or `psql` command, no project-local container workflow, no approved synthetic rehearsal URL, and no locally installed PostgreSQL driver. Per the safety precondition, nothing was installed, started, or contacted.

`database/postgresql-rehearsal/` contains a synthetic-only manifest scaffold and rollback boundary. For a later approved run: provision a disposable local database; set `POSTGRES_REHEARSAL_SYNTHETIC=true` and the approved URL; add a project-local pinned driver; complete schema-v4 type/constraint mappings; generate fictional fixtures; import; compare allowlisted table/row/constraint parity; exercise critical queries; run rollback only against that database; record versions and destroy the disposable environment. SQLite remains canonical throughout.
