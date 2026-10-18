# P00-PWA-AUTH-WP-021C04B03C02H — inactive deletion observer and incident storage

2026-10-18. This component is **not active in WordPress**: it registers no hooks, routes, UI or migrations. Only the disposable test fixture attaches it to native deletion hooks. No retained WordPress installation or real user was touched.

## Implementation

The observer accepts trusted WordPress users/usermeta table names and a dedicated non-reconnecting mysqli connection. A `begin` call confirms the user exists and streams its metadata into an HMAC using an ephemeral per-process random key. It retains only the digest in memory, not the raw metadata. Row count and individual field lengths have technical limits; a failed query or exceeded limit makes the observation unverified. The `after` call consumes that state. If the account remains and metadata differs it records `PARTIAL_USER_SURVIVES`; if the account remains and metadata is identical it records `USER_SURVIVES`. If no user remains, the result is only `NO_SURVIVING_USER_OBSERVED`, not proof of a clean deletion. Missing or duplicate start, database errors or incident-write failure return `UNVERIFIED`. G's store now accepts the two fixed survivor categories.

The digest is not a backup or recovery snapshot. The stored numeric user ID is linkable personal data; the 300-second test TTL is not an approved production retention period. All methods are internal building blocks, not a production authorization model.

## Actual checks

On disposable InnoDB WordPress and MariaDB 10.6.28, the fixture attached the observer to `delete_user` and `deleted_user`, while retaining a separate plugin-like listener. A session-specific trigger induced either a single synthetic metadata-delete failure or failure of all metadata deletes. In each of these combinations, the base reference fixture and all 13 observer-specific assertions passed with exit 0 and final PASS marker:

| WordPress | PHP | Outcome |
| --- | --- | --- |
| 6.4.3 | 8.1.27 | PASS |
| 6.6.2 | 8.2.25 | PASS |
| 7.1.3 | 8.3.35 | PASS |

Native success left no surviving-user incident and the other listener ran. Both injected failures preserved WP's `true` return and the other listener, while producing the correct distinct incident category. Missing/duplicate start, direct SQL bypass, a closed observer connection and failed incident-store write were not treated as success. The separate G storage regression passed all 21 checks on WP 6.6.2/PHP 8.2.25. PHP 7.4 lint passed for both classes and the fixture; without disposable flag and random prefix execution exited 2 before WordPress bootstrap.

The temporary database had no host port or bind mounts and used tmpfs; after inspection it was stopped, removing only its synthetic data. Image tags are mutable and the listed versions are observations.

## Reproduce on disposable Docker only

Check the temporary container name is unused and wait for the final database ready message on port 3306. Credentials are synthetic. Check the final success marker as well as exit status.

```powershell
docker run --detach --rm --name carmilla-p00-observer-store-20261018 --tmpfs /var/lib/mysql:rw --env MARIADB_ROOT_PASSWORD=synthetic_root_password --env MARIADB_DATABASE=carmilla_login_qa --env MARIADB_USER=synthetic_qa --env MARIADB_PASSWORD=synthetic_qa_password mariadb:10.6
$taskBridge = (Resolve-Path 'wordpress/carmilla-bridge').Path.Replace('\','/') + ':/bridge:ro'
$taskTests = (Resolve-Path 'wordpress/carmilla-bridge/tests').Path.Replace('\','/') + ':/work:ro'
foreach ($taskImage in @('wordpress:6.4-php8.1-apache', 'wordpress:6.6-php8.2-apache', 'wordpress:php8.3-apache')) {
    $taskPrefix = 'qa_' + [guid]::NewGuid().ToString('N').Substring(0,12) + '_'
    $taskOutput = docker run --rm --network container:carmilla-p00-observer-store-20261018 --volume $taskBridge --volume $taskTests --env CARMILLA_LOGIN_DISPOSABLE=yes --env "CARMILLA_LOGIN_TEST_PREFIX=$taskPrefix" $taskImage php /work/smoke-deletion-incident-observer-real-wp.php
    $taskExit = $LASTEXITCODE
    $taskOutput
    if ($taskExit -ne 0 -or -not ($taskOutput -match '^PASS inactive native deletion incident observer WP ')) { throw 'Observer test did not pass' }
}
```

## Remaining gaps

The two digest snapshots and the existence read are not a transactional global snapshot. Concurrent mutations, user-ID reuse, changed state before/after the observer, process crash, direct SQL, nested deletions, multisite, persistent object caches, other plugins and unknown external side effects are not proven safe or fully detected. A missing user does not establish that metadata, content, comments, or plugin data were cleanly removed. WP's false success and callback effects are unchanged. A failed database write has no durable fallback or admin alert. No snapshot, recovery, production privacy/retention/capability policy, live deployment or manual QA is established. The parent `021C04B03C02`, registration and P00 Gate remain blocked.
