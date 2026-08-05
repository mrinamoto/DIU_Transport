# Phase 2 Database Baseline

## Isolation decision

The web database path defaults to `backend/data/diu_transport_web.db` through `WEB_DATABASE_PATH`. The Java database remains `diu_transport.db`; Phase 2 neither read, modified, deleted, nor migrated it. Runtime database files and `-wal`/`-shm` sidecars are ignored. Only `.gitkeep`, migration SQL, and seed source belong in Git.

## Explicit lifecycle

```powershell
npm run db:init
npm run db:seed
```

Initialization creates the parent directory and migration version 1 only when safe. A pre-existing database with tables but without the Phase 2 migration marker is refused as unknown; no automatic legacy migration or destructive reset occurs. The server opens an already initialized database and does not silently create schema.

SQLite enables `foreign_keys`, a busy timeout, and WAL mode. The migration creates only `users`, `buses`, `drivers`, `routes`, `schedules`, and `schema_migrations`, with foreign keys and indexes supporting identity, catalog lookup, and schedule conflict checks.

## Schema summary

- `users`: normalized unique email, bcrypt hash, canonical role/status, timestamps
- `routes`: name, origin, destination, active state, timestamps
- `buses`: unique bus number, capacity, active state, timestamps
- `drivers`: fictional-safe contact field, active state, timestamps
- `schedules`: route/bus/driver references, concrete service date and local HH:MM range, trip/status/notes, creator, timestamps

All application values are bound parameters. Schedule create/update/cancel and seed operations use transactions.

## Seed policy

`npm run db:seed` is additive and idempotent. It creates two fictional routes, buses, drivers, and schedules dated 2030-01-15. It does not delete data, print personal information, or create accounts/default credentials.

## Executed evidence

- `npm run db:init` — passed; schema version 1 initialized.
- `npm run db:seed` — passed; fictional demonstration data inserted safely.
- Runtime `GET /api/health` — returned status `ok` and database `connected` without an absolute path.
- Automated test — confirmed foreign keys enabled and the temporary test path differs from development.
- Tests create a unique OS-temporary directory and remove it after completion.

## Limitations

Phase 2 supports concrete dates only. It has no recurrence model, timezone-aware timestamp model, migration rollback command, production backup plan, concurrency/load qualification, or PostgreSQL compatibility layer.
