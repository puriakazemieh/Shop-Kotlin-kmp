# P00-PWA-AUTH-WP-021C04B04A — bounded synthetic identity collision audit

- Status: DONE; Size: M; Owner: AI; Risk: HIGH
- Parent: `P00-PWA-AUTH-WP-021C04B04`. The owner authorized independently executable work before the parent is ready.
- Implement an inactive, bounded, read-only exact-byte audit of WordPress login, nonempty email and `cb_mobile` ownership using a single database read. Do not include `billing_phone`, modify accounts, create claims, install hooks, expose identifiers or touch either retained site. Distinguish multiple references for the same owner from cross-owner collisions; fail closed on unsupported input, truncation, orphaned metadata and database errors.
- Test on synthetic WordPress/MariaDB with a randomly prefixed isolated fixture, including same-owner aliases, cross-namespace conflicts, duplicate metadata, unsupported input, no change to source data and query failure. Record redacted evidence under `docs/evidence/P00-PWA-AUTH-WP-021C04B04A/`.
- Exact ASCII byte comparison is not approved normalization, a collision-free migration, a concurrency guarantee, or permission to scan a real site. Keep the parent TODO and its dependencies intact.

## Completion — 2026-10-19

Inactive bounded audit tested on three isolated real WordPress/PHP combinations; all 10 checks passed on each. PHP 7.4 lint and the synthetic-only guard passed. This internal non-UI component does not itself require manual QA. [Evidence and limitations](../evidence/P00-PWA-AUTH-WP-021C04B04A/verification.md). No migration or retained-site scan was performed.
