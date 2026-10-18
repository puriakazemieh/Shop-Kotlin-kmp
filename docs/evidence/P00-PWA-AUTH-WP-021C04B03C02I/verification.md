# P00-PWA-AUTH-WP-021C04B03C02I — bounded incident discovery

2026-10-18. The inactive store now offers capability-guarded `list_unexpired`: at most 100 newest unexpired records for the configured site, returning only incident ID, numeric account reference and fixed category. It does not prove the user still exists or that the deletion was repaired. Malformed records, invalid bounds or clock and query failures fail closed, not as an empty list; unauthorized calls are denied before querying. There is no pagination, acknowledgment, visible warning, fallback storage, recovery, product hook, route or migration. In particular, more than 100 records can leave older records undiscoverable; production incident UX and operational handling remain unimplemented. Numeric IDs are linkable personal data, not an approved retention or privacy policy.

On disposable MariaDB 10.6.28 and real WordPress 6.4.3/PHP 8.1.27, 6.6.2/PHP 8.2.25 and 7.1.3/PHP 8.3.35, each extended storage suite passed `Checks: 33; failures: 0`. This includes all 21 prior checks and 12 checks of scoped/bounded ordering, editor and anonymous denial, malformed record, expiry, failed queries, invalid clock and a closed connection. The existing native observer fixture also emitted its final PASS marker on each combination. PHP 7.4 and local PHP 8.2 syntax checks passed for both modified files.

The first observer invocation was misconfigured without its required read-only `/bridge` mount and failed; the corrected invocation below reran both complete suites on all three combinations with fresh synthetic prefixes. No existing WordPress installation was changed. After inspection, the dedicated synthetic database container had only tmpfs database storage, no bind mounts and no host ports; it was stopped and only its temporary synthetic data was removed. The separate browser fixture and the user's preserved sites remain untouched.

## Reproduction on a new disposable container only

```powershell
docker run --detach --rm --name carmilla-p00-incident-list-example --tmpfs /var/lib/mysql:rw --env MARIADB_ROOT_PASSWORD=synthetic_root_password --env MARIADB_DATABASE=carmilla_login_qa --env MARIADB_USER=synthetic_qa --env MARIADB_PASSWORD=synthetic_qa_password mariadb:10.6
# Wait for the final MariaDB ready message on port 3306.
$taskBridge = (Resolve-Path 'wordpress/carmilla-bridge').Path.Replace('\','/') + ':/bridge:ro'
$taskWork = (Resolve-Path 'wordpress/carmilla-bridge').Path.Replace('\','/') + ':/work:ro'
foreach ($taskImage in @('wordpress:6.4-php8.1-apache','wordpress:6.6-php8.2-apache','wordpress:php8.3-apache')) {
    foreach ($taskScript in @('smoke-deletion-incident-store-real-wp.php','smoke-deletion-incident-observer-real-wp.php')) {
        $taskPrefix = 'qa_' + [guid]::NewGuid().ToString('N').Substring(0,12) + '_'
        $taskOutput = docker run --rm --network container:carmilla-p00-incident-list-example --volume $taskBridge --volume $taskWork --env CARMILLA_LOGIN_DISPOSABLE=yes --env "CARMILLA_LOGIN_TEST_PREFIX=$taskPrefix" $taskImage php "/work/tests/$taskScript"
        $taskExit = $LASTEXITCODE
        $taskOutput
        if ($taskExit -ne 0 -or -not ($taskOutput -match '^PASS inactive (native deletion incident observer|deletion incident storage) WP ')) { throw 'Synthetic test failed' }
    }
}
```

Do not activate this component or infer that the native-deletion, safe identity, migration, registration or Phase 00 acceptance blockers are resolved.
