# P00-PWA-AUTH-WP-021C04B03C02G — inactive incident storage

2026-10-18. This is an **inactive storage primitive**, not detection, alerting, recovery, a WordPress deletion fix, or an authorization to migrate or change an existing site. No preserved installation or database was touched.

## Implemented scope

Historical note: `021C04B03C02H` subsequently added the fixed `USER_SURVIVES` category and reran the 21-check storage regression. The observations below describe G at its completion; H's additional scope and limitations are documented [here](../P00-PWA-AUTH-WP-021C04B03C02H/verification.md).

`CB_Deletion_Incident_Store` defines, but does not create, an InnoDB schema; it registers no hooks, routes, UI or migrations. Records contain a random incident ID, installation digest, numeric account reference, fixed category and timestamps. A numeric account reference is linkable personal data; the code does not establish a production privacy or retention policy. The caller must explicitly choose a TTL of 1 second through 7 days; the bound is a technical limit, not an approved retention period. Cleanup is bounded, site-scoped, explicitly invoked and capability-guarded, not scheduled. Authorized reads use `manage_options` and happen only after a capability check; production capability and multisite behavior require review.

The store verifies the expected InnoDB table, exact columns, types, nullability, ASCII collations and primary key. It rejects malformed installation IDs and schema; errors are generic. `AVAILABLE` only returns a stored reference; it does **not** assert that the account currently exists or that a deletion was repaired. The dedicated mysqli connection must be configured without automatic reconnect and used only by trusted integration code. A write failure throws; a failed read returns `UNVERIFIED`, never `MISSING` or a claimed recovery.

## Tests

The real WordPress fixture installed only synthetic accounts and random-prefix tables in an isolated MariaDB 10.6.28 database. All three combinations below returned exit 0, `Checks: 21; failures: 0.` and the final success marker:

| WordPress | PHP | Result |
| --- | --- | --- |
| 6.4.3 | 8.1.27 | PASS |
| 6.6.2 | 8.2.25 | PASS |
| 7.1.3 | 8.3.35 | PASS |

Checks cover administrator/editor/logged-out access, installation separation, malformed/unknown IDs, minimal stored fields, expiry boundaries, bounded cleanup, unexpected schema, invalid clock, real failed table query, synthetic trigger-rejected insert and closed-connection failures. The trigger rejection returns a generic error and leaves no incident row. PHP 8.2 and 7.4 lint passed; execution without the disposable flag and random prefix exited 2 before bootstrap.

After inspection, the temporary DB container had no bind mounts or host ports and `/var/lib/mysql` was tmpfs. It was stopped, removing only its synthetic temporary database. Image tags are mutable; versions above are observations, not guarantees.

## Reproduce in a disposable fixture only

Check the exact container name is unused; wait for MariaDB's final ready message on port 3306. The credentials below are synthetic. Check the final marker as well as exit status, because WordPress bootstrap failures can exit zero.

```powershell
docker run --detach --rm --name carmilla-p00-incident-store-20261018 --tmpfs /var/lib/mysql:rw --env MARIADB_ROOT_PASSWORD=synthetic_root_password --env MARIADB_DATABASE=carmilla_login_qa --env MARIADB_USER=synthetic_qa --env MARIADB_PASSWORD=synthetic_qa_password mariadb:10.6
$taskSource = (Resolve-Path 'wordpress/carmilla-bridge').Path.Replace('\','/') + ':/work:ro'
foreach ($taskImage in @('wordpress:6.4-php8.1-apache', 'wordpress:6.6-php8.2-apache', 'wordpress:php8.3-apache')) {
    $taskPrefix = 'qa_' + [guid]::NewGuid().ToString('N').Substring(0,12) + '_'
    $taskOutput = docker run --rm --network container:carmilla-p00-incident-store-20261018 --volume $taskSource --env CARMILLA_LOGIN_DISPOSABLE=yes --env "CARMILLA_LOGIN_TEST_PREFIX=$taskPrefix" $taskImage php /work/tests/smoke-deletion-incident-store-real-wp.php
    $taskExit = $LASTEXITCODE
    $taskOutput
    if ($taskExit -ne 0 -or -not ($taskOutput -match '^PASS inactive deletion incident storage WP ')) { throw 'Incident test did not pass' }
}
```

## Critical limitations

There is no durable fallback when the incident database is unavailable: this code reports failure only to its caller. It has no observer or end-to-end lifecycle; native WP can still falsely report success, and external callbacks, posts, comments and plugin effects cannot be undone. No account snapshot or recovery is implemented. No privacy policy, retention approval, production capability review, crash/commit-durability proof, multisite verification, real admin warning or actual migration is asserted. The incident class is deliberately not bootstrapped. `021C04B03C02`, its parents, registration and P00 Gate remain open. No manual browser QA is applicable to this inactive internal component.
