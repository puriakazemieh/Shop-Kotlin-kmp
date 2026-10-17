# P00-PWA-AUTH-WP-021C04B03A — synthetic user-insert feasibility

- Status: DONE; Size: M; Owner: AI; Risk: HIGH
- Depends on: `P00-PWA-AUTH-WP-021C04B02`.
- Experiment with database-enforced, single-owner identity claims for **new user-row INSERT only**, exclusively in isolated synthetic MariaDB and real WordPress core. Exact synthetic ASCII/binary equality only; no production canonicalization or deployment.
- Test actual concurrent independent `wp_insert_user` calls and direct uncooperative SQL inserts: conflicting login/email/cross-namespace owner rejected atomically, identical login/email on the same user permitted, independent users succeed, and statement/transaction failure leaves no partial claim. Compare the existing unprotected synthetic race. Never expose identities or credentials in test output.
- Do not implement or imply protection of UPDATE, DELETE, `cb_mobile`, normalization, migration, WordPress data outside this fixture, hooks, routes or existing mobile behavior. Record reproducible redacted evidence in `docs/evidence/P00-PWA-AUTH-WP-021C04B03A/`; if infeasible or still larger than M, stop and document the reason.

## Completion — 2026-10-17

The isolated INSERT-only database feasibility matrix passed on three real WordPress/PHP combinations; the unprotected race was reproduced. On rejected inserts WordPress returned ID 0 and fired `user_register` with ID 0, a critical unresolved compatibility/side-effect finding. This task completes the **experiment only**, not a safe registration service or migration. Evidence and reproduction: [insert-experiment.md](../evidence/P00-PWA-AUTH-WP-021C04B03A/insert-experiment.md).
