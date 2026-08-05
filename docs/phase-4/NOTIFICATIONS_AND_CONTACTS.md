# Notifications and Emergency Contacts

## Notifications

Authenticated users receive published notifications whose publication time has arrived, expiry time has not passed, and audience is either unrestricted or matches their current backend role. Draft, cancelled, expired, future, and role-mismatched records are hidden. Read state is stored in `notification_reads` with a composite notification/user key; marking one user's item read does not affect another user.

Administrators can create and edit drafts, publish drafts, cancel drafts or published items, explicitly expire published items, and filter/paginate all records. Manual types are `SCHEDULE_UPDATE`, `SCHEDULE_CANCELLATION`, `NEW_ROUTE`, `SPECIAL_TRIP`, `EMERGENCY`, and `GENERAL`. Related entity references are validated when supplied.

Schedule updates/cancellations and special-trip approvals/cancellations create published notifications automatically. Stable deduplication keys prevent duplicate event notices. All lifecycle actions and automatic creation are redacted audit events.

## Emergency contacts

Administrators can create, update, order, deactivate, and reactivate validated contacts. Authenticated non-admin users see active contacts ordered by `display_order`; inactive contacts remain available in the administrator history. Contact roles are `TRANSPORT_MANAGER`, `TRANSPORT_OFFICER`, `EMERGENCY_HOTLINE`, and `OTHER`.

Audit metadata contains field names/status changes only and excludes contact names, phone numbers, email addresses, and availability text.

## Frontend and verification

The Notifications view supports draft creation and lifecycle actions for administrators and mark-read for users. The Emergency contacts view supports administrator creation/status actions and active user viewing. Tests cover audience filtering, lifecycle visibility, per-user read isolation, unauthorized mutation, manual publishing, automatic events, validation, ordering/status visibility, and redaction. Chromium automation exercises administrator publishing/contact creation and student reading; axe scans both views.

## Limitations

Delivery is in-app and pull-based. There is no email, SMS, push service, websocket, retry queue, delivery receipt, localization, or distributed scheduler. Emergency contacts are not public to anonymous users.
