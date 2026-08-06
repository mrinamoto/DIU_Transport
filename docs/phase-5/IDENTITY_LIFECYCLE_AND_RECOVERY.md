# Identity Lifecycle and Recovery

`users.status` controls ACTIVE/INACTIVE eligibility; `security_status` is the narrow ACTIVE/LOCKED/SUSPENDED security state. Administrator APIs allow safe paginated inspection and validated transitions. A transaction recounts usable administrators before any change and refuses a demotion, deactivation, lock, or suspension that would remove the final active recovery path.

JWTs contain `auth_version`. Authentication reloads the user and requires an active account, ACTIVE security state, and exact token version. Role/status/security changes, explicit revocation, recovery initiation, and recovery completion increment the version. There is no plaintext blacklist.

Failed passwords increment `failed_login_count`; the configured threshold applies a time-bounded lock. A successful login clears the count. Errors are generic. Administrators can explicitly unlock; every security mutation is audited without email, password, hash, or token metadata.

Recovery uses 32 random bytes. Only SHA-256 is stored in `password_recovery_tokens`; the administrator sees the raw token once in the local handover dialog. It expires, is superseded by a later request, and can be consumed once. Completion bcrypt-hashes the new password and revokes earlier sessions. Phase 5 sends no email/SMS. Production delivery, identity proofing, dual control, and operator approvals remain Phase 6 work.
