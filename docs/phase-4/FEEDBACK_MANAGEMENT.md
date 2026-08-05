# Feedback Management

## User workflow

Authenticated users can submit categorized feedback and list/read only their own records. Categories are `SCHEDULE`, `ROUTE`, `BUS_SERVICE`, `DRIVER_BEHAVIOR`, `SAFETY`, `APPLICATION`, and `OTHER`. The server validates lengths and rejects credential-like content. Submission is protected by the common API limit and a dedicated configurable feedback limiter.

## Administrator workflow

Administrators can list and filter feedback by status, category, or active assigned employee, inspect a record, assign it, add/update a response, and move it through controlled states:

- `NEW -> IN_REVIEW` or `CLOSED`
- `IN_REVIEW -> RESOLVED` or `CLOSED`
- `RESOLVED -> CLOSED`

Reverse and terminal transitions fail with HTTP 409. Assignment requires an active employee. Resolution time is server generated.

## Privacy and audit

Ownership checks return 404 for another user's feedback record. Administrative changes generate audit metadata containing only changed field names and state transitions. Subject, message, response, submitter identity, and employee identity/contact information are not written to audit metadata. Sanitized exports exclude all feedback free text.

## Frontend and verification

Non-admin users have a submit-and-history view. Administrators have an all-feedback view with review/resolution controls. API tests cover authentication, validation, credential-content rejection, ownership isolation, filtering, assignment, transitions, inactive assignee rejection, response/resolution, redaction, and deterministic HTTP 429 behavior. Chromium automation covers submission and administrator review.

## Limitations

There are no attachments, anonymous submissions, threaded messages, appeal workflow, SLA timer, automatic escalation, retention policy, or external ticketing integration.
