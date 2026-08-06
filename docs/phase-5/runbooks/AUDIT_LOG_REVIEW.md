# Audit Log Review
## Trigger or symptom
Periodic review, incident inquiry, or approval verification.
## Immediate containment
Restrict review to authorized administrators and preserve relevant time ranges.
## Verification commands
Use `/api/audit-logs` with strict action/entity/outcome/actor/date filters and pagination.
## Safe diagnostic information
Record action, entity type/ID, outcome, actor ID, request ID, timestamp, and redacted metadata.
## Recovery steps
Correlate with operational request logs; investigate denied/conflict events; document conclusions.
## Validation after recovery
Expected sensitive mutations have matching audit records and no metadata contains secrets/personal content.
## Escalation point
Escalate missing/tampered records, unknown administrators, or secret material.
## Actions that must not be performed
Do not export unrestricted personal data, alter audit rows, or infer intent without corroboration.
## Evidence to retain
Approved redacted extracts, filters, reviewer, timestamps, and conclusions.
