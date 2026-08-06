# Notification Delivery Boundary

In-app notifications remain canonical. Creation in PUBLISHED state, draft publication, and automatic publication insert an idempotent `notification_outbox` NOOP job inside the same SQLite transaction. The idempotency key is `notification:<id>:NOOP`; the recipient reference is only `AUDIENCE:ALL` or `ROLE:<role>`.

The outbox lifecycle is PENDING, PROCESSING, SUCCEEDED, FAILED, or CANCELLED, with bounded attempts. The implemented provider path performs a local state transition only and makes no network request. Admin endpoints expose safe job state and audited retry/cancel actions. Payloads, personal destinations, credentials, and provider secrets are absent.

A future provider must implement a separate adapter at the `outboxService` dispatch boundary, accept a minimal internal recipient reference, resolve destinations outside logging/audit paths, enforce consent and suppression policy, and supply secret management, delivery receipts, dead-letter handling, and integration tests. EMAIL/SMS/PUSH are not active Phase 5 channels.
