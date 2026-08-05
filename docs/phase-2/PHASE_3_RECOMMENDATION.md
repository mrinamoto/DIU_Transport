# Recommended Phase 3 Objective

Phase 3 should harden the canonical web baseline and complete administrator-managed catalog foundations before adding broad feature modules.

## Priority order

1. Add administrator CRUD and validation for routes, buses, and drivers, including safe deactivation rules when active schedules reference them.
2. Add security operations: rate limiting, structured security/audit events, session revocation strategy, secret rotation guidance, dependency scanning, and account lifecycle controls.
3. Add migration tooling, backup/restore rehearsal, concurrent-write/load tests, and a documented PostgreSQL migration design without switching databases prematurely.
4. Improve frontend accessibility, browser testing, error recovery, pagination/filtering, and schedule-date navigation.
5. Resolve or clearly quarantine unmounted pre-Phase-2 Node modules and separately decide the future of the legacy Java application without rewriting it incidentally.

## Exit criteria

Phase 3 should leave catalog data manageable without direct SQL, sensitive administrator actions auditable, migrations and backups rehearsed, and critical workflows covered by API plus browser-level tests. Production deployment and real institutional data should remain blocked until privacy, identity, infrastructure, and operational ownership are formally approved.

PostgreSQL implementation, React, JavaFX, cloud deployment, live GPS, payments, and production university integration should not be bundled into this hardening phase.
