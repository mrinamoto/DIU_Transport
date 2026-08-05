# Phase 4 Test Evidence

Date: 2026-08-05 (Asia/Dhaka)

## Automated results

| Command | Result |
|---|---:|
| `npm run check` | 62/62 JavaScript files passed syntax checking |
| `npm run test:phase2` | 25/25 passed |
| `npm run test:phase3` | 22/22 passed |
| `npm run test:phase4` | 21/21 passed |
| `npm test` | 68/68 combined backend tests passed |
| `npm run test:browser` | 4/4 passed |
| `npm audit --audit-level=low` | 0 vulnerabilities |

The Phase 3 count is one higher than its original 21-test baseline because the database-operations suite now includes an explicit non-destructive Phase 3-to-Phase 4 migration case. All original Phase 2 and Phase 3 scenarios remain present.

## Phase 4 API coverage

The 21-test isolated Phase 4 suite covers schema/foreign keys; RBAC; employee normalization, validation, privacy, deactivation and reactivation; contact validation/order/status; manual notification lifecycle, audience filtering, per-user read state, cancellation and expiry; special-trip draft/edit/approval/cancellation/completion, invalid transitions, inactive assignments, bus/driver conflicts, record preservation, automatic notices; automatic schedule update/cancellation notices; feedback authentication, validation, ownership, assignment, response, resolution, transitions and dedicated rate limiting; audit redaction and request IDs.

## Database and operations evidence

- Fresh initialization reached schema version 3.
- A synthesized Phase 3 schema upgraded to version 3, preserved its route row, passed `foreign_key_check`, and added Phase 4 tables.
- Online backup and temporary-copy restore verification passed at schema version 3.
- The command-level sanitized export rehearsal completed with 13 tables.
- Automated export assertions confirm exclusion of credential, identity/contact, notification, special-trip, and feedback free-text fields.
- The tracked development database was not used by tests or rehearsals; isolated temporary databases were removed afterward.

## Browser and accessibility evidence

Browser actually automated: Playwright `chromium`, headless, desktop and 390 x 844 mobile viewport. Firefox and WebKit were not run.

Four browser tests passed:

1. Preserved student registration and administrator catalog/schedule/audit workflow.
2. Phase 4 employee, contact, notification, special-trip, student visibility/read/feedback, and administrator review workflow.
3. Desktop axe scans at login and the Employees, Special trips, Notifications, Emergency contacts, and Feedback administrator views.
4. Mobile student navigation, overflow check, and axe scan at Notifications.

Final axe result: zero serious or critical violations on all seven tested states. An initial mobile run exposed insufficient contrast caused by opacity on read notifications; the style was changed to a dashed border without opacity, and the complete final browser run passed.

## Manual rendered-page walkthrough

The Codex in-app Browser was used against an isolated local server. Health rendered as online; fictional student registration reached the authenticated workspace; Schedules, Special trips, Notifications, Emergency contacts, and My feedback rendered with the expected headings; browser console warning/error count was zero. The server was stopped, its database removed, and port 4184 confirmed free.

## Checks not claimed

No Firefox, WebKit, Edge-specific, real-device, screen-reader, full keyboard-only, 200% zoom, high-contrast/forced-colors, load, penetration, production restore, email/SMS, or deployment test was run.

## Manual follow-up checklist

- [ ] Edge desktop: registration/login, every role navigation item, forms, conflicts, and logout.
- [ ] Firefox desktop: the same critical administrator and student workflow.
- [ ] Keyboard only: logical tab order, visible focus, select controls, status actions, confirmations, and feedback review.
- [ ] Screen reader: landmarks/headings, form instructions/errors, dynamic loading/results, notification read state, and feedback status.
- [ ] 200% zoom: no lost content or overlapping controls at desktop width.
- [ ] Windows high contrast/forced colors: focus, status, error, link, and destructive-action distinguishability.
- [ ] Physical mobile viewport near 375 px: navigation wrapping, forms, cards, dialogs, links, and no horizontal overflow.
