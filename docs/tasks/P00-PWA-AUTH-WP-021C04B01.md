# P00-PWA-AUTH-WP-021C04B01 — identity writer inventory

- Status: READY; Size: S; Owner: AI; Risk: HIGH
- Depends on: `P00-PWA-AUTH-WP-021C04A`.
- Perform a read-only, bounded code and WordPress-core inventory of identity creation, changes, lookup and authentication relevant to registration, OTP, profile, admin, migration and both product hosts. Record precise file/line references and explicitly distinguish known code from uninspected third-party writers and deployment data.
- Identify current normalization, duplicate/cross-namespace collisions, schema and concurrency limitations; do not access account records on preserved WordPress installations, change files outside task documentation, or make claims about live data.
- Record redacted evidence in `docs/evidence/P00-PWA-AUTH-WP-021C04B01/` and list safety requirements and questions for B02. No endpoint, UI, migration, database or manual QA change is in this task.
