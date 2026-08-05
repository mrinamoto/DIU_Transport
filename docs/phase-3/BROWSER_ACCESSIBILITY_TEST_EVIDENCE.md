# Browser and Accessibility Test Evidence

Date: 2026-08-05

## Automated environment

```powershell
npm run test:browser
```

Actual browser: Playwright Chromium 151 (headless shell v1234), one worker.  
Result: 2 passed, 0 failed in the final run (6.9 seconds).  
Firefox and WebKit were not installed or run. Edge was not run.

The test owns an isolated temporary SQLite database, initializes/migrates/seeds it, provisions a runtime-fictional administrator, starts/stops an ephemeral Express server, and cleans its temporary files. Credentials are generated at runtime; no external service or real secret is used.

## Critical workflow coverage

The Chromium workflow verifies:

1. approved public registration;
2. public `ADMIN` registration rejection;
3. student login;
4. student schedule view;
5. no rendered student admin catalog UI;
6. backend 403 for student catalog mutation;
7. admin login;
8. bus creation;
9. driver creation;
10. route creation;
11. schedule creation/update using catalog values;
12. normalized duplicate bus rejection;
13. future-schedule deactivation conflict;
14. admin audit entry visibility;
15. logout removing access.

## Automated accessibility result

The second Chromium test runs `@axe-core/playwright` on the login state and authenticated administrator workspace. It asserts no `serious` or `critical` violations; the final run passed both states with zero violations at those impact levels.

The HTML also supplies an English `lang`, unique page title, semantic banner/main/footer/regions, labeled form controls, named buttons, heading hierarchy, live alert/status regions, table captions/headers, visible focus styling, responsive overflow control, and reduced-motion support. This is automated evidence, not a complete accessibility audit.

## In-app browser walkthrough

An additional isolated in-app Chromium walkthrough registered a fictional student, displayed two fictional schedules, confirmed catalog-management navigation was not rendered for that role, checked `lang=en` and one main landmark, changed to a 375 × 844 effective viewport with no horizontal overflow, logged out, reset the viewport, finalized the tab, stopped the server, and removed only its dedicated temporary database files.

## Issues encountered and resolved before final run

- Playwright's Chromium headless shell was initially absent; the matching browser artifact was installed.
- The initial test server origin did not match configured CORS; test configuration was corrected to its actual origin.
- Async form handlers accessed `event.currentTarget` after `await`; the form reference is now captured before the asynchronous boundary.
- Strict locators for repeated labels were scoped to their panels/tables.

These intermediate failures are not reported as passes; the final 2/2 result was rerun after each fix.

## Manual checks remaining

- Complete keyboard-only traversal, including every edit/deactivate/confirmation path.
- Screen-reader smoke tests with NVDA/JAWS/VoiceOver.
- Browser zoom at 200% across all admin tables/forms.
- Windows high-contrast/forced-colors inspection.
- Chrome headed visual inspection at desktop and additional mobile widths.
- Edge and Firefox visual/workflow verification; WebKit/Safari verification where available.
- Human review of error announcement timing, focus movement after mutations, and color/contrast beyond automated rules.
