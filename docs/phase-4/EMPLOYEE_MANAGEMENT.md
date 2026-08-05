# Employee Management

## Implemented scope

Phase 4 adds the `employees` table and authenticated `/api/employees` resource. Administrators can create, list, inspect, update, deactivate, and explicitly reactivate employee records. Deactivation is a status transition; records are not deleted, so feedback assignment history remains intact.

Employee codes are trimmed, uppercased, and unique case-insensitively. Allowed roles are `TRANSPORT_OFFICER`, `HELPER`, `MAINTENANCE`, and `OTHER`; shifts are `MORNING`, `EVENING`, `NIGHT`, and `FLEXIBLE`; status is `ACTIVE` or `INACTIVE`. Name, phone, optional email, and notes have server-side validation. Protected identifiers/timestamps and unknown fields are rejected.

## Authorization and disclosure

- Every endpoint requires authentication.
- Only `ADMIN` can mutate records.
- Non-admin listing exposes active records only and omits phone, email, notes, status, and timestamps.
- Administrator reads include operational fields and inactive history.

## Audit behavior

Create, update, deactivate, and reactivate actions produce audit rows containing actor, entity identifier, request ID, outcome, changed field names, and status transition only. Names, employee codes, phones, emails, and notes are not copied into audit metadata.

## Frontend and verification

The administrator Employees view supports create, edit, deactivate, and reactivate. Isolated API tests cover normalization, duplicate rejection, validation, RBAC, limited non-admin projection, lifecycle preservation, and audit redaction. Browser automation covers administrator creation.

## Limitations

Employees are operational catalog records, not login accounts. There is no payroll, attendance, HR identity integration, document upload, or permanent delete workflow.
