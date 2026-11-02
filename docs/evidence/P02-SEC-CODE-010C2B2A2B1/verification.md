# P02-SEC-CODE-010C2B2A2B1 — verifier evidence

2026-11-02. Read-only `CB_User_Integrity::inspect()` checks single-site, MariaDB 10.6+, InnoDB users/posts/comments, unsigned non-null bigint reference columns, zero nonzero-orphan rows, and the exact generated columns plus RESTRICT foreign keys. It reports `uninstalled`, `installed`, `partial`, `unsupported` or `unreadable`. SQL read failure cannot return a clean state. It does not run DDL or authorize deletion; the later guard card will use it.

Tested in disposable `test-env-wordpress-test-1` (WordPress 7.1.3, PHP 8.3.35, MariaDB 10.6.28): `CARMILLA_LOGIN_DISPOSABLE=yes php /tmp/smoke-user-integrity-verifier-real-wp.php`, exit 0, six PASS and zero FAIL. Scratch InnoDB tables were dropped by `finally`. Cases: valid uninstalled schema; orphan refusal; generated column without FK as partial; both FKs verified installed; injected missing metadata table as unreadable; recovery after removing the fault. PHP 8.1 syntax checks for helper and smoke test passed. No retained or production database was changed.

Current installed fixture is correctly `uninstalled`; no migration has run. MySQL 8 and other database topologies are not claimed supported. Next migration card must preflight and install only on a verified topology, then repeat the verifier and migration/rollback tests. No human QA is necessary for this read-only helper.
