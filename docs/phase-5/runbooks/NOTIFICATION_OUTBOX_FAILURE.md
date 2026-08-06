# Notification Outbox Failure
## Trigger or symptom
FAILED jobs, growing pending count, stuck PROCESSING rows, or duplicate delivery concern.
## Immediate containment
Keep non-NOOP providers disabled and pause any future worker.
## Verification commands
Inspect protected outbox and metrics endpoints; correlate notification ID and idempotency key.
## Safe diagnostic information
Record job ID, channel, state, attempts, error code, request ID, and aggregate counts.
## Recovery steps
Correct the provider boundary, retry only eligible jobs within the attempt cap, or cancel with approval.
## Validation after recovery
State transition is correct, idempotency remains unique, audit event exists, and no network call occurred in Phase 5.
## Escalation point
Escalate duplicates, attempt-cap exhaustion, or sensitive destination exposure.
## Actions that must not be performed
Do not edit attempts manually, log payloads/destinations, or activate email/SMS.
## Evidence to retain
Redacted job timeline, metrics, audit events, code/version, and remediation approval.
