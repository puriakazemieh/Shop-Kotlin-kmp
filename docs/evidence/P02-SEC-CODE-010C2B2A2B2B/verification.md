# P02-SEC-CODE-010C2B2A2B2B — operator entrypoint evidence

2026-11-02. `wordpress/carmilla-bridge/tools/user-integrity.php` is an explicit PHP CLI entrypoint. It is not wired to plugin activation or web requests. `preflight` reads and prints the verifier state. `install` refuses unsupported/partial/unreadable topology, requires a readable nonempty backup file with matching SHA-256, runs the migration core, and reports DDL/postcheck failure without claiming transactional rollback. An already installed state is a no-op.

Disposable installed WordPress test `CARMILLA_LOGIN_DISPOSABLE=yes php /tmp/smoke-user-integrity-operator-real-wp.php`: exit 0, 8 PASS, 0 FAIL. Scratch tables covered preflight, missing backup, wrong digest, unchanged schema on denial, injected DDL failure, successful operator invocation, idempotent repeat, and unchanged real WordPress tables. PHP 8.1 syntax checks passed. Direct CLI `preflight` returned `uninstalled` with exit 0; missing and mismatched backup inputs returned exit 3. No product schema was migrated.

## Operator procedure for a real-site migration (not performed here)

1. Schedule a maintenance window and stop site writes. Confirm single-site MariaDB/InnoDB compatibility with `php tools/user-integrity.php preflight`; require `uninstalled` or `installed` and inspect the orphan/engine checks in the verifier. A partial/unsupported/unreadable result is a stop.
2. Take a complete database backup using the site's approved backup process. Restore it to an isolated disposable clone and verify user/post/comment counts and a clean WordPress boot. Record backup path, size, SHA-256, restore date/operator and clone verification. A digest match alone proves file identity, **not** that the dump is complete or restorable.
3. Only after that restore proof, run `php tools/user-integrity.php install <absolute-backup-file> <sha256>` from the plugin directory on the intended site. The PHP process must be able to read the same backup file. Require exit 0 and `installed` from a second `preflight` call. A DDL failure can leave a partial state; stop writes and restore the verified backup on the approved maintenance path before any retry.
4. Run the migration and deletion QA matrix from the following task on the clone first. Never treat this technical command as authorization to modify production; that requires a separate operator action and approval.

The CLI does not validate backup completeness, execute a restore, or change the managed deletion route. The next QA and deletion-guard cards cover those boundaries. No human action is needed to accept this isolated CLI implementation.
