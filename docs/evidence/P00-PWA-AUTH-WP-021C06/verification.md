# P00-PWA-AUTH-WP-021C06 — inactive session migration

2026-10-18. Automated initial-install and recovery probes passed in synthetic databases; **AWAITING_MANUAL_QA**. The prerequisite `021C05` remains incomplete. Neither this result nor a future limited fixture confirmation authorizes route activation, real-site migration, PWA acceptance or a Phase 00 Gate.

## Scope and limitations

`CB_Web_Session_Migration` is not bootstrapped by the plugin and runs only when explicitly invoked by trusted code. It creates an initial version-1 namespace only when all three table names are absent, serializes cooperating migrators through a database advisory lock, checks the required storage structure and marker, and reports `CURRENT` without altering a verified installation. It refuses partial/incompatible schemas, unknown versions and lock/storage errors with a sanitized exception. It never automatically deletes, repairs or downgrades tables. DDL commits independently in MariaDB: a failure after the first table can leave a partial schema and must not be represented as rolled back. [Recovery procedure](recovery.md).

The code and runtime adapter are not connected: the existing store alone does not enforce the migration marker, so integration must fail closed on a missing marker. This is initial installation and idempotent re-entry, **not** an upgrade from a previous schema. Advisory locks do not stop uncooperative SQL writers. Information-schema visibility and adequate privileges must be independently established for real hosting; no remote/TLS DB, multisite, wpdb drop-in, other database engine, crash during DDL, live WordPress deployment or production security review is claimed.

## Results

- Real synthetic WordPress 6.3.5/PHP 7.4.33, 6.4.3/PHP 8.1.27, 6.6.2/PHP 8.2.25 and 7.1.3/PHP 8.3.35, MariaDB 10.6.28: each `Checks: 16; failures: 0` and `PASS inactive session migration`. Tests cover fresh install and repeat, an active session and pre-existing synthetic account/post, partial schema, future version, malformed column, index, engine, marker and trigger, lock contention, invalid prefix and closed connection.
- A separate restricted synthetic DB user was granted CREATE on only the first session table. Actual second-table DDL failed; the test observed the first table preserved and a subsequent migration refused: `PASS actual mid-migration DDL permission failure preserved partial schema and retry failed closed`.
- The earlier WordPress adapter regression passed 7/7 on each PHP 8.1, 8.2 and 8.3 fixture. PHP 7.4 syntax checks passed. The initially attempted `wordpress:6.3-php7.4-apache` tag did not exist; the verified test instead used the locally retained official WordPress 6.3.5 source with `php:7.4-cli` and its mysqli extension.
- In the separate persistent fixture, a synthetic database dump was stored in a dedicated volume and restored into a **different new** synthetic database. Row counts and selected stored user, metadata, post, session-family and token fields matched without printing those values; a real WordPress bootstrap on the restored copy verified the synthetic account, post, current migration marker and an active synthetic session. This is not a restore of either retained user site.
- The disposable test database was stopped only after checking its exact name, tmpfs storage, absence of bind mounts and host ports. Its synthetic data was removed. Both previously retained WordPress sites were untouched. The two volumes below and their separate database container remain available.

## Persistent synthetic fixture and owner manual check

- Container: `carmilla-p00-session-migration-fixture-20261018`; Docker volume: `carmilla-p00-session-migration-fixture-20261018`; backup volume: `carmilla-p00-session-migration-backup-20261018`. No database port is exposed to the host. These are not either of the retained WordPress installations.
- For a manual rerun, start Docker and the named container if stopped. In PowerShell, from `D:\Android\AndroidStudioProjects\kmp-shop`, run the commands below. Each run creates additional **synthetic** tables under a new random prefix; it does not reset or delete prior data.

```powershell
docker start carmilla-p00-session-migration-fixture-20261018
$taskWork = (Resolve-Path 'wordpress/carmilla-bridge').Path.Replace('\','/') + ':/work:ro'
$taskPrefix = 'qa_' + [guid]::NewGuid().ToString('N').Substring(0,12) + '_'
docker run --rm --network container:carmilla-p00-session-migration-fixture-20261018 --volume $taskWork --env CARMILLA_ADAPTER_DISPOSABLE=yes --env "CARMILLA_ADAPTER_TEST_PREFIX=$taskPrefix" wordpress:6.4-php8.1-apache php /work/tests/smoke-web-session-migration-real-wp.php
```

Success: `Checks: 16; failures: 0.` and the final `PASS inactive session migration` line. For the separately restored copy, run the following; success is the final `PASS restored synthetic WordPress...` line. Its synthetic session has a bounded expiry, so the restored-copy check may require recreating the fixture if run much later.

```powershell
docker run --rm --network container:carmilla-p00-session-migration-fixture-20261018 --volume $taskWork --env CARMILLA_ADAPTER_RESTORE_DISPOSABLE=yes --env CARMILLA_ADAPTER_TEST_PREFIX=qa_f909753dabcf_ wordpress:6.4-php8.1-apache php /work/tests/verify-session-migration-restore-real-wp.php
```

Report both outputs without screenshots containing passwords, URLs with secret keys or personal information. Manual confirmation here covers only the synthetic migration fixture; it cannot approve production migration or satisfy the incomplete login dependency.
