# Special Trips

## Data and lifecycle

Each `special_trips` row has a one-to-one foreign key to a canonical `schedules` row whose `trip_type` is `SPECIAL`. Supported categories are `EXAM`, `CLUB_EVENT`, `INDUSTRIAL_VISIT`, and `OTHER`.

The controlled lifecycle is:

`DRAFT -> APPROVED -> COMPLETED`

`DRAFT -> CANCELLED` and `APPROVED -> CANCELLED` are also allowed. Other transitions fail with HTTP 409. Drafts use a cancelled schedule row so they cannot appear or reserve resources prematurely. Only draft details can be edited.

## Conflict and catalog integration

Draft creation and editing use the existing schedule service to validate route, bus, and driver references and require active assignments. Approval activates the linked schedule through the same service, enforcing bus and driver overlap checks. Non-admin schedule listing excludes linked special trips unless approved; non-admin special-trip listing also exposes approved records only.

Approval conflicts roll back atomically and generate a redacted `SPECIAL_TRIP_APPROVE` conflict audit. Cancellation soft-cancels the linked schedule; no operational record is deleted.

## Notifications and audit

Successful approval and cancellation create published, in-app `SPECIAL_TRIP` notifications with stable deduplication keys. Create, update, approve, cancel, complete, and approval-conflict events are audited using identifiers, request IDs, changed field names, transition states, and controlled reasons—not titles, descriptions, organizers, or schedule notes.

## Frontend and verification

Administrators can create a draft and invoke approve, cancel, or complete actions. Authenticated users can view approved trips. API tests cover hidden drafts, editing, success transitions, invalid transitions, inactive assignments, bus/driver conflicts, automatic notices, cancellation preservation, RBAC, and audit request IDs. Chromium automation creates and approves an examination trip and verifies student visibility.

## Limitations

There is no recurrence, multi-day itinerary, passenger manifest, capacity reservation, approval chain, live tracking, email/SMS delivery, or transport-card integration.
