# Recommended Phase 5 Objective

## Recommendation

Make Phase 5 a **production-readiness and data-governance design/rehearsal phase**, not a deployment and not a broad feature expansion.

The recommended objective is to define and prove the operational boundaries required before real institutional evaluation:

1. Establish production identity and account lifecycle requirements, including administrator recovery, revocation, session/token strategy, and role source of truth.
2. Define retention, access, redaction, export, and incident procedures for audit logs, feedback, employee/contact data, and notifications.
3. Design an asynchronous notification-delivery boundary with opt-in preferences, templates, retry/idempotency, and a fictional local adapter; do not send real email/SMS during rehearsal.
4. Add structured observability, health/readiness separation, operator runbooks, and measurable backup/restore recovery objectives.
5. Conduct a separately approved, synthetic-data PostgreSQL compatibility and migration rehearsal with rollback evidence while keeping SQLite available until the decision is accepted.
6. Expand accessibility/manual testing to Firefox, WebKit, keyboard-only, screen reader, zoom, and forced-colors states.

## Why this should precede cards or payments

Phase 4 introduces more identity-linked operational data and user communications. Production governance, recovery, observability, and storage decisions should be explicit before adding higher-risk transport-card, billing, or payment data. Cards and payments should remain a later, separately authorized project with its own threat model and compliance review.

## Proposed exit criteria

- Approved architecture and data-classification/retention decisions.
- Tested account recovery/revocation and permission lifecycle using fictional data.
- Notification delivery interface demonstrated with a local no-send adapter and deterministic tests.
- Operator runbooks and measurable backup/restore/incident rehearsals.
- Synthetic PostgreSQL migration and rollback rehearsal with reconciliation evidence, without deployment.
- Expanded browser/assistive-technology evidence and no unresolved serious/critical findings in tested states.

This document is a recommendation only. No Phase 5 implementation, deployment, PostgreSQL migration, email/SMS delivery, card, billing, or payment work was performed in Phase 4.
