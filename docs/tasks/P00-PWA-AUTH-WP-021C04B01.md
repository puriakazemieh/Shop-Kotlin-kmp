# P00-PWA-AUTH-WP-021C04B01 — identity writer inventory

- Status: DONE; Size: S; Owner: AI; Risk: HIGH
- Depends on: `P00-PWA-AUTH-WP-021C04A`.
- Perform a read-only, bounded code and WordPress-core inventory of identity creation, changes, lookup and authentication relevant to registration, OTP, profile, admin, migration and both product hosts. Record precise file/line references and explicitly distinguish known code from uninspected third-party writers and deployment data.
- Identify current normalization, duplicate/cross-namespace collisions, schema and concurrency limitations; do not access account records on preserved WordPress installations, change files outside task documentation, or make claims about live data.
- Record redacted evidence in `docs/evidence/P00-PWA-AUTH-WP-021C04B01/` and list safety requirements and questions for B02. No endpoint, UI, migration, database or manual QA change is in this task.

## Completion — 2026-10-17

- Recorded a bounded source inventory of Bridge, Theme/core, KMP routes, WordPress 6.6.2 core and the locally installed WooCommerce 11.2.1 source, plus the prior synthetic race/schema evidence. No live account records were read or modified.
- Evidence: `docs/evidence/P00-PWA-AUTH-WP-021C04B01/inventory.md`. It identifies distinct OTP/mobile/profile identifiers, independently reachable WordPress/WooCommerce writers and the limitations of the P07 content migration. It does not claim an exhaustive third-party/deployment audit.
