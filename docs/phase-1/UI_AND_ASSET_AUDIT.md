# UI and Asset Audit

## Current UI technologies

- Desktop: Java Swing with Nimbus/system look-and-feel, custom colors, emoji icons, tables, dialogs, gradients, and fixed-size frames.
- Web: one 18-line placeholder `frontend/index.html` with inline CSS. There is no web dashboard, form, client API code, or stylesheet.
- JavaFX/FXML: not found.

## Desktop navigation and screens

| Screen | Evidence | Actual behavior status |
|---|---|---|
| Splash/login | `DIUTransportSystem`, `LoginFrame` | Designed; startup unverified; login has unsafe demo fallback |
| Registration | `RegisterFrame` | Persists through `UserService` if DB works; Student/Teacher/Staff only |
| User dashboard | six tabs: Schedule, Transport Card, Billing, Lost & Found, Contact, Profile | Mostly hardcoded/mock content |
| Admin dashboard | Dashboard, Users, Buses, Schedule, Drivers, Transport Cards, Billing, Notifications, Settings | Generic empty management tables and dialogs; not wired to service CRUD |

`UserDashboard` hardcodes sample schedules, bills, found items, card details, contacts, and summary totals. Lost-item and feedback buttons validate minimally, show success, then discard input. Profile editing explicitly says “Coming Soon.” The status bars claim database connectivity without checking it.

## UX and accessibility findings

- Large fixed dimensions (`1200x700`, other explicit sizes) and dense tabular layouts are not responsive to small screens or font scaling.
- Emoji are used as functional icons; rendering varies by OS/font and accessible names are not independently defined.
- Labels are visually associated through layout but not consistently via `labelFor`; no screen-reader strategy is evident.
- Keyboard support exists for Enter on login, but focus order, mnemonics, high contrast, and full keyboard navigation are unverified.
- Many empty/loading/error states are absent because tables are static.
- Destructive admin buttons are visually present; confirmation/real behavior is inconsistent and the generic table actions are not connected.
- Validation feedback exists on registration/login but persistence errors are often flattened into generic messages.
- Inline styling dominates Swing constructors and the placeholder HTML. There is no shared web design system or duplicate CSS because no CSS files exist.

## Asset inventory

| Asset | State |
|---|---|
| University logo | Not found |
| App icon | `images/icon.png` referenced in `Constants`, file absent and no runtime load found |
| Bus/campus photos | Not found; not required for functional Phase 2 |
| Web fonts | None |
| Icon library | None; emoji only |
| Desired reference screenshot | Not present in repository or supplied audit attachment |

## Missing/broken asset recommendations

Do not download assets until ownership/licensing is confirmed. If the owner supplies approved originals, suggested future targets are:

- `assets/brand/diu-logo.svg` — preferred scalable university logo.
- `assets/icons/app-icon.svg` plus packaged PNG sizes if desktop packaging requires them.
- `assets/images/bus-hero.webp` — optional photographic hero only if the future web design needs it.
- `assets/images/campus-dsc.webp` — optional campus context image.
- `assets/icons/*.svg` — accessible, consistent functional icons.

SVG is preferable for logos/icons. WebP is useful only for photographic images. Real bus/campus photography is not necessary for core scheduling workflows.

## Licensing warnings

- No asset license/provenance file exists.
- DIU name/logo use may require university brand authorization.
- Do not copy images from a screenshot or the public web without an explicit license.
- Emoji rendering comes from platform fonts and is unsuitable as a controlled brand asset.

## UI conclusion

The Swing application demonstrates a visual concept, not verified end-to-end functionality. The web UI has not been implemented. Phase 2 should not redesign either UI; it should choose the canonical platform, remove false-success/mock claims from the chosen critical path, and define a small set of verifiable screens.
