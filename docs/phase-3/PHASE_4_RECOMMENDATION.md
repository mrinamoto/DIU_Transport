# Phase 4 Recommendation

Date: 2026-08-05

## Recommended objective

Phase 4 should be a separately approved production-readiness and PostgreSQL migration-pilot phase. It should not deploy by default and should preserve the Phase 3 Node/Express API and plain frontend until evidence justifies architectural change.

## Priority order

1. Define production requirements: identity owner, privacy/data classification, retention, availability, recovery objectives, accessibility target, traffic/concurrency, and operational ownership.
2. Add account lifecycle controls: administrator-managed users, password reset, credential rotation, optional MFA, session/token revocation, and safe security-event response.
3. Design a PostgreSQL schema and one-way rehearsal importer from the sanitized export; validate types, constraints, sequences, case uniqueness, timestamps/timezone, JSON, and row reconciliation in an isolated disposable instance.
4. Add production operations: structured redacted logs, metrics/alerts, distributed rate limiting, trusted reverse-proxy/TLS configuration, secrets management, database encryption, scheduled backup retention, and an approved restore runbook.
5. Expand quality gates: Firefox/WebKit/Edge, full WCAG 2.2 AA review with assistive technology, concurrency/load tests, migration rollback rehearsal, dependency/security scanning, and threat modeling.
6. Only after those gates, prepare a deployment proposal with environments, approvals, rollback, monitoring, and data-governance signoff.

## Exit criteria for a PostgreSQL pilot

- No real data is used until privacy/security approval and a documented lawful purpose exist.
- Import is repeatable, idempotent or restartable, and reconciles every safe row count/key.
- All Phase 2 and Phase 3 behavioral tests pass against an adapter-supported PostgreSQL test environment.
- Case-normalized uniqueness, foreign keys, checks, schedule conflicts, route-stop ordering, audit JSON, dates/times, and identity sequences are verified.
- SQLite remains untouched during rehearsal and rollback is demonstrated with disposable targets.

## Explicit non-recommendations

Do not combine migration, framework replacement, Java modernization, real-data import, and deployment into one phase. React/JavaFX/cloud migration is not required to address the current highest risks. Phase 3 itself performed none of this Phase 4 work.
