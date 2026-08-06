# Authentication Incident
## Trigger or symptom
Unexpected login failures, lock spikes, stale authorization, or suspected account takeover.
## Immediate containment
Revoke the affected user's sessions; suspend the account when warranted while preserving the final administrator.
## Verification commands
Use protected user/audit/metric endpoints and request-ID filters; never query passwords or raw tokens.
## Safe diagnostic information
Record user ID, state transitions, aggregate failures, timestamps, and request IDs.
## Recovery steps
Verify identity out of band, unlock or initiate one-time recovery, rotate secrets if compromise is suspected.
## Validation after recovery
Old JWT fails; new login works; role/state is correct; audit events are present.
## Escalation point
Escalate on administrator compromise, widespread failures, or secret exposure.
## Actions that must not be performed
Do not reveal account existence publicly, log credentials, disable rate limits, or disable final-admin protection.
## Evidence to retain
Redacted audit events, metric snapshots, actions/approvals, and affected time window.
