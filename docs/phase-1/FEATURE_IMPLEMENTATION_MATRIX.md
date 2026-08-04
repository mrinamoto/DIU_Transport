# Feature Implementation Matrix

Statuses describe the repository as a whole. “Implemented” means code exists; it does not mean the application ran. No end-to-end feature reached **VERIFIED WORKING** because neither application started and no tests exist.

| # | Feature | Status | Evidence | Missing pieces / risk |
|---:|---|---|---|---|
| 1 | User registration | IMPLEMENTED BUT NOT VERIFIED | Java `RegisterFrame.performRegistration` -> `UserService.register`; web `POST /api/auth/register`; tables `users` | Runtime blocked; web role escalation |
| 2 | Student registration | IMPLEMENTED BUT NOT VERIFIED | `RegisterFrame` Student fields; `Student`; web validator requires `student_id` | No identity verification; ID not unique in web schema |
| 3 | Teacher registration | IMPLEMENTED BUT NOT VERIFIED | `Teacher`; Java role form; generic web register | No teacher-specific verification |
| 4 | Staff registration | IMPLEMENTED BUT NOT VERIFIED | `Staff`; Java role form; generic web register | No staff-specific verification |
| 5 | Admin creation | BROKEN | Java auto-seeds default admin; web public registration accepts admin | Both paths insecure; no controlled provisioning |
| 6 | Login | BROKEN | `UserService.login`; `POST /api/auth/login` | Java plaintext and demo bypass; no runtime verification |
| 7 | Logout | PARTIALLY IMPLEMENTED | Java dashboards call `SessionManager.logout` | No web UI or token invalidation/revocation endpoint |
| 8 | Password storage | BROKEN | Java `users.password`; Node `password_hash`/bcrypt | Desktop plaintext; two incompatible auth stores |
| 9 | Password reset/recovery | PLACEHOLDER OR MOCK ONLY | Java dialog claims email was sent; web admin reset route | No user recovery/token/email workflow |
| 10 | Profile view | PARTIALLY IMPLEMENTED | Java profile tab from session; web `GET /api/auth/me` | Java mock/limited; no web page |
| 11 | Profile update | PARTIALLY IMPLEMENTED | `UserService.updateUser`; web admin `PUT /api/users/:id` | User dashboard says “Coming Soon”; no self-service web update |
| 12 | Role-based permissions | BROKEN | Web `requireRole('admin')`; Java dashboard selection | Public admin registration; stale JWT role; desktop services lack policy |
| 13 | Bus CRUD | IMPLEMENTED BUT NOT VERIFIED | Java `BusService` add/get/update/delete; Java `buses` table | Admin UI generic/unwired; no web bus routes |
| 14 | Bus status | IMPLEMENTED BUT NOT VERIFIED | `BusStatus`; `BusService.updateBusStatus`; both schemas | Enum/string inconsistency; no verified UI flow |
| 15 | Driver CRUD | IMPLEMENTED BUT NOT VERIFIED | Java `DriverService`; `drivers` tables | Admin UI generic/unwired; no web driver routes |
| 16 | Employee CRUD | NOT FOUND | Java `employees` table and `Employee` model only | No service, controller, or screen |
| 17 | Route CRUD | IMPLEMENTED BUT NOT VERIFIED | Web `routesApi.js` public reads/admin CRUD; Java `Route` model/table | No Java `RouteService`; no real frontend |
| 18 | Stop management | PARTIALLY IMPLEMENTED | Java comma-separated stops; Node JSON `routes.stops` | No stop entity/CRUD/validation |
| 19 | Ordered route stops | PARTIALLY IMPLEMENTED | Array/text order is retained in route records | No ordinal keys, per-stop times, or relational integrity |
| 20 | Daily schedule viewing | PARTIALLY IMPLEMENTED | Web public `GET /api/schedules`; Java `ScheduleService.getTodaySchedules`; user UI hardcoded table | Not runtime-verified; Swing table not DB-backed |
| 21 | Schedule CRUD | IMPLEMENTED BUT NOT VERIFIED | Web admin schedule POST/PUT/DELETE; Java `ScheduleService` | No verified UI/API test |
| 22 | Bus assignment | IMPLEMENTED BUT NOT VERIFIED | Schedule `bus_id`; `BusService.assignBusToRoute`; web schedule payload | No conflict/availability enforcement in web |
| 23 | Driver assignment | IMPLEMENTED BUT NOT VERIFIED | Web schedule `driver_id`; Java `DriverService.assignDriverToBus` | Java schedule lacks driver; no verified flow |
| 24 | Schedule conflict detection | PARTIALLY IMPLEMENTED | Java `ScheduleService.hasConflictingSchedule` | Not called by confirmed UI; overlap logic/day equality limited; absent in web |
| 25 | Special trips | PARTIALLY IMPLEMENTED | Notification enum/type; Node schedule has `day_type` | No trip entity, request/workflow, event metadata |
| 26 | Exam-day trips | PARTIALLY IMPLEMENTED | Node `day_type` allows `exam`; seed labels exam schedule | No exam-date/calendar model or dedicated workflow |
| 27 | Event/club trips | NOT FOUND | No table, class, route, or UI persistence | Notification text alone is insufficient |
| 28 | Industrial visits | NOT FOUND | No source/schema evidence | Missing entirely |
| 29 | Schedule cancellation | IMPLEMENTED BUT NOT VERIFIED | Node schedule status `cancelled`; Java `deactivateSchedule`; notification helper | No verified atomic cancel+notify workflow |
| 30 | Notifications | IMPLEMENTED BUT NOT VERIFIED | Java `NotificationService`; web notification CRUD/read tracking | User Swing dialog/mock; no web page |
| 31 | Transport card | IMPLEMENTED BUT NOT VERIFIED | Java model/service/table; web `/api/cards` | Swing card largely hardcoded; no web page |
| 32 | Card validity/status | PARTIALLY IMPLEMENTED | `TransportCard.isExpired`; schema status/expiry fields | No automatic expiry job/invariant; not verified |
| 33 | Billing/payment status | IMPLEMENTED BUT NOT VERIFIED | Java `BillingService`; web `/api/billing`; tables | Swing values hardcoded; Node lacks `partial` despite requirements |
| 34 | Cost chart | PLACEHOLDER OR MOCK ONLY | `Constants` fixed role fees; Swing billing summaries | No route/semester cost-chart entity or API |
| 35 | Emergency contacts | IMPLEMENTED BUT NOT VERIFIED | Java contacts/default seed; web public/admin `/api/contacts` | Swing hardcoded; personal-data provenance unknown |
| 36 | Feedback/contact form | PARTIALLY IMPLEMENTED | Web `/api/feedback`; Swing feedback button | Swing discards input; no browser form |
| 37 | Lost-item report | PARTIALLY IMPLEMENTED | Web authenticated `POST /api/lostfound`; Java service/model | Swing success is fake; runtime unverified |
| 38 | Found-item report | PARTIALLY IMPLEMENTED | Combined `lost_found.status` can be `found`; Java model represents found item | No explicit found-report workflow/form in web or Swing |
| 39 | Lost-and-found administration | IMPLEMENTED BUT NOT VERIFIED | Web admin list/update/delete; Java `LostFoundService` | Admin Swing has no dedicated tab; no tests |
| 40 | Claim verification | NOT FOUND | Status/claimed fields only | No claimant proof, approval, handover, or access checks |
| 41 | Review/remarks | PARTIALLY IMPLEMENTED | Node `remarks` column/update route | Java schema/model differ; no user/admin UI evidence |
| 42 | Search and filters | PARTIALLY IMPLEMENTED | Multiple Java service search methods; web list query filters | Swing search dialogs are mostly illustrative; no web client |
| 43 | Sample data | BROKEN | Java default contacts/admin; destructive Node seed | Personal-looking data/known credentials; seed deletes all rows |
| 44 | Validation | PARTIALLY IMPLEMENTED | Java model/form checks; `backend/utils/validators.js`; route checks | Inconsistent and incomplete for dates, amounts, IDs, status, role changes |
| 45 | Error handling | PARTIALLY IMPLEMENTED | try/catch and JSON/dialog errors throughout | Many errors swallowed to false/empty; misleading connected/success UI |
| 46 | Audit logging | NOT FOUND | Recent Activity is hardcoded UI data | No immutable actor/action log table or service |
| 47 | Database backup/initialization | BROKEN | Java creates schema/defaults and has backup/restore; Node initializes schema | Java missing driver; restore unsafe/unsupported; schemas collide; seed destructive |
| 48 | Tests | NOT FOUND | No test files/dependencies/scripts/CI | Zero automated coverage |
| 49 | Documentation | BROKEN | `README.md`, VS Code configs, `sources.txt` | README is aspirational; paths/launch targets stale; no setup/security docs |
| 50 | Deployment readiness | BROKEN | No Java package/build, no web lockfile, placeholder frontend | Neither app starts; SQLite/local state unsuitable for production hosting |

## Feature conclusions

- **Verified working features: none end-to-end.** JavaScript parsing and tool/runtime detection are verification results, not user features.
- The strongest source-only implementation is the untracked Express CRUD API for schedules, routes, cards, notifications, lost-and-found, contacts, users, billing, feedback, and authentication.
- The strongest tracked implementation is the Java service/model breadth, but Swing feature screens frequently display hardcoded data instead of service results.
- Missing or materially broken scope includes secure admin provisioning, secure desktop authentication, employee management, event/club trips, industrial visits, claim verification, audit logging, a real web UI, tests, and deployment packaging.
