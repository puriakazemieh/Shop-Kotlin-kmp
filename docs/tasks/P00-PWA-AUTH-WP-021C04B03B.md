# P00-PWA-AUTH-WP-021C04B03B — synthetic user-update and deletion feasibility

- Status: DONE; Size: M; Owner: AI; Risk: HIGH
- Depends on: `P00-PWA-AUTH-WP-021C04B03A`.
- Extend only the synthetic proof to ordinary user-row UPDATE/DELETE, same-owner aliases, concurrent add/change/delete, rollback and safe claim lifecycle. Demonstrate no stale, released-too-early or wrongly transferred claims in the tested scenarios; test real WordPress APIs and independent SQL. Explicitly identify remaining race and support limitations.
- No live migration or site changes. Record redacted evidence in `docs/evidence/P00-PWA-AUTH-WP-021C04B03B/`. Split or stop if full coverage exceeds M.

## Completion — 2026-10-17

The isolated user-row lifecycle experiment passed its limited matrix with real WordPress APIs and concurrent SQL on three core versions. A rejected `wp_update_user` database write nevertheless returned the user ID and fired `profile_update`; this unresolved compatibility risk prevents treating the fixture as a safe deployed identity system. No real registration, migration or complete ownership protection is claimed. Evidence: [lifecycle-experiment.md](../evidence/P00-PWA-AUTH-WP-021C04B03B/lifecycle-experiment.md).
