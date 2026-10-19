# Inactive deletion-incident notice — synthetic verification

2026-10-19. The notice is **not registered or active on any site**. It does not restore data, prove deletion succeeded, catch every failure, or unblock identity migration.

The callable notice checks `manage_options` before accessing the existing site-scoped store, shows at most 100 validated observations with numeric account references and fixed escaped labels, and explicitly says recovery has not been performed. An unavailable store produces an explicit sanitized error instead of an empty/success result. No incident yields no displayed notice; this is **not** evidence that all deletions are healthy. Account references can be personal data and the retention policy is not established.

On real isolated WordPress 6.4.3/PHP 8.1.27, 6.6.2/PHP 8.2.25 and 7.1.3/PHP 8.3.35 with MariaDB 10.6.28, each run produced `Checks: 9; failures: 0` and the final PASS marker. Checks include administrator/editor/anonymous access, separate site scope, empty and populated lists, bounded 100-row output and a closed DB. PHP 7.4 syntax checks passed. Tests printed no incident account ID, credential, synthetic contact address or raw database error. The separate persisted synthetic database used random table prefixes; no retained site was touched.

```powershell
docker start carmilla-p00-identity-composite-20261018
$taskWork = (Resolve-Path 'wordpress/carmilla-bridge').Path.Replace('\','/') + ':/work:ro'
foreach ($taskImage in @('wordpress:6.4-php8.1-apache','wordpress:6.6-php8.2-apache','wordpress:php8.3-apache')) {
    $taskPrefix = 'qa_' + [guid]::NewGuid().ToString('N').Substring(0,12) + '_'
    $taskOutput = docker run --rm --network container:carmilla-p00-identity-composite-20261018 --volume $taskWork --env CARMILLA_LOGIN_DISPOSABLE=yes --env "CARMILLA_LOGIN_TEST_PREFIX=$taskPrefix" $taskImage php /work/tests/smoke-deletion-incident-notice-real-wp.php
    $taskExit = $LASTEXITCODE
    $taskOutput
    if ($taskExit -ne 0 -or -not ($taskOutput -match '^PASS inactive deletion incident notice WP ')) { throw 'Notice test incomplete.' }
}
```

## Remaining work and owner QA

The observer, store and notice are all inactive: they have no deployed hooks, migration, out-of-database durable alert, complete concurrency/crash detection, pagination, privacy/retention approval or recovery workflow. Some failures can prevent an incident from being recorded at all. External callback effects cannot be undone. The native WordPress false-success and identity writer blockers remain; parent C02/B03 are still BLOCKED.

After a separate synthetic admin fixture is safely integrated, the owner should sign in as its synthetic administrator, trigger the fixture's deliberately failed synthetic deletion, and verify that the warning says the account may remain and no automatic recovery was performed. Sign in as the fixture's editor and confirm the warning is not shown. Finally use the fixture's storage-failure control and verify an explicit unavailable warning, not a clean bill of health. The fixture and precise addresses must be supplied by the integration task; these checks cannot be performed on the existing sites yet. Do not share account identifiers or screenshots containing personal data. No owner QA is claimed.
