# Data Inventory and Classification

Categories: PUBLIC, INTERNAL, PERSONAL, SECURITY_SENSITIVE, OPERATIONAL, AUDIT, and DERIVED. Backups contain every table and must be treated as SECURITY_SENSITIVE. Sanitized exports use explicit column allowlists.

| Table / fields | Class and purpose | Access / API | Logs | Sanitized export | Retention / lifecycle |
|---|---|---|---|---|---|
| users: name, email | PERSONAL identity | self and admin safe views | never | excluded; role/status only | retain while account/history requires; anonymize only under approved policy |
| users: password_hash, auth_version, failures, locks | SECURITY_SENSITIVE authentication | middleware/admin policy; never returned | never | hash/counters excluded; version only for parity | password hashes until replacement; counters reset; lifecycle with account |
| recovery tokens/hash/timestamps | SECURITY_SENSITIVE recovery | hash DB-only; raw shown once | never | hash excluded; state/timestamps allowed | expired records become candidates after 30 days |
| drivers/employees contact fields | PERSONAL / OPERATIONAL assignment and support | admin; minimum active public view | never | names/contact/notes excluded | preserve transport history; deactivate rather than delete |
| buses/routes/stops/schedules/special trips | OPERATIONAL; selected published schedule fields PUBLIC-to-authenticated | authenticated; mutations admin | IDs/status only when necessary | structural/operational allowlist | core history is not a retention target |
| notifications content | INTERNAL or PUBLIC-to-audience | matching authenticated role; admin manages | content never | content excluded; state retained | expired/cancelled older than documented policy may be candidate |
| notification_reads | DERIVED / PERSONAL preference | owning user only via notification result | never | identifiers/timestamp for rehearsal | policy candidate only when approved |
| notification_outbox | OPERATIONAL | admin only | aggregate state only | safe IDs/state/error code | retain for delivery evidence; bounded policy pending |
| emergency contacts | PERSONAL / OPERATIONAL | active records to authenticated users; admin full | never | contact details excluded | deactivate; preserve history |
| feedback content/response | PERSONAL / INTERNAL support | owner and admin | never | content excluded; state only | retain per approved support/legal policy |
| audit_logs | AUDIT / INTERNAL | admin only | not duplicated | redacted structured metadata included | no Phase 5 deletion; governance approval required |
| data_lifecycle_runs | AUDIT / OPERATIONAL | tooling/administration | aggregate only | safe mode/counts | retain as lifecycle evidence |
| schema_migrations | OPERATIONAL | readiness/tooling | version only | manifest | permanent with database |

Backups and unsanitized database copies inherit the highest classification present. They must not be attached to reports, committed, or exposed through the application. Export files are rehearsal artifacts and must still be access controlled because linkage through identifiers may remain possible.
