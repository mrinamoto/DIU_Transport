# Suspected Secret Exposure
## Trigger or symptom
A credential, JWT signing secret, recovery token, or private key may have been disclosed.
## Immediate containment
Restrict access, stop dissemination, revoke affected authentication, and preserve evidence securely.
## Verification commands
Search changed tracked files and approved secret-manager audit data without printing secret values.
## Safe diagnostic information
Use secret type, fingerprint, location class, time window, and affected environment.
## Recovery steps
Rotate through the approved secret system, restart safely, revoke tokens, and remove exposed copies from authorized stores.
## Validation after recovery
Old credentials fail; readiness succeeds with new configuration; no secret appears in logs/reports.
## Escalation point
Immediately notify the security owner for production or repository exposure.
## Actions that must not be performed
Do not paste, email, commit, or echo the secret; do not rewrite Git history without incident-owner approval.
## Evidence to retain
Redacted fingerprint, timestamps, scope, rotation proof, and approvals.
