# Accessibility and Browser Evidence

Executed 2026-08-06 with Playwright 1.62.1 and Chromium 151.0.7922.34 (local Playwright bundle 1234): 6/6 browser tests passed. Firefox and WebKit bundles were not locally installed, so those engines were not executed and no Edge claim is made.

Automated axe scans found 0 serious or critical violations on login, existing administrator/support views, Users, Outbox, and Operations. Tested viewports included desktop default, 390×844 mobile, and 768×1024 tablet. Automated checks covered 200% CSS zoom without horizontal overflow, forced-colors media, visible focus, initial tab order, skip-link focus, keyboard form submission/navigation, Escape closing the recovery dialog, and focus return.

The in-app browser manual walkthrough confirmed login, user administration, empty NOOP outbox, schema-v4 metrics, and zero captured console errors using synthetic credentials. Outstanding manual work: screen-reader announcements across multiple products, native 200% browser zoom on multiple OS/browser combinations, touch target review on physical devices, and Firefox/WebKit/Edge once approved local binaries exist. This is evidence, not a claim of full WCAG compliance.
