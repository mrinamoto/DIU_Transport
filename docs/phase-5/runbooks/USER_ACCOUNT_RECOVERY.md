# User Account Recovery
## Trigger or symptom
Verified user cannot authenticate and approved support requests recovery.
## Immediate containment
Confirm identity through the approved offline process; suspend/revoke if compromise is suspected.
## Verification commands
Inspect safe account state and redacted audit history through administrator APIs.
## Safe diagnostic information
Use user ID, role/state, timestamps, and request IDs only.
## Recovery steps
Initiate recovery, hand the one-time token through the approved local channel, clear it from view, and have the user complete reset.
## Validation after recovery
Token reuse fails, old JWT fails, new password login succeeds, and audit events exist.
## Escalation point
Escalate failed identity proof, administrator recovery, or suspected compromise.
## Actions that must not be performed
Do not send tokens by unapproved email/SMS, log/copy them into tickets, or reset passwords directly in SQL.
## Evidence to retain
Approval, user ID, initiation/completion timestamps, and redacted audit/request IDs.
