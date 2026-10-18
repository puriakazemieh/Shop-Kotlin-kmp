# P00-PWA-AUTH-WP-021C04B03C01 — synthetic shared identity reference model

- Status: READY; Size: M; Owner: AI; Risk: HIGH
- Depends on: `P00-PWA-AUTH-WP-021C04B03B`; scope-review evidence: `docs/evidence/P00-PWA-AUTH-WP-021C04B03C/mobile-baseline.md`.
- Develop a **test-only**, disposable MariaDB/real-WordPress model of one exact-binary synthetic identity claim owned by one user with independently tracked references from user login, nonempty email and multiple `cb_mobile` rows. Do not infer that a reference is unused merely because one source was removed.
- Test sequential INSERT/UPDATE/DELETE with both real WordPress metadata APIs and direct SQL, duplicate same-owner rows, cross-namespace same-owner aliases, removing one versus the last reference, and keeping `billing_phone` outside identity claims. Explicitly reject or document unsupported fixture values; no production phone normalization or identity merging.
- Preserve existing user-row experiments and sites; no route, migration, live schema or OTP changes. Record redacted evidence in `docs/evidence/P00-PWA-AUTH-WP-021C04B03C01/`. A passing sequential fixture does not establish safe concurrency, reassignment or production behavior; those belong to C02 and the composite gate.
