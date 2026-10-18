# P00-PWA-AUTH-WP-021C05A — isolated refresh and logout

2026-10-18. This is a narrow preparatory component. It registers no route, emits no HTTP header, and changes no preserved WordPress/database. The deletion blocker and the PWA login, registration, reset and browser QA are **not** resolved by this task.

## Implementation and limits

`class-cb-web-auth-session.php` checks the reviewed request policy before session operations, uses the transactional store for refresh rotation and revocation, and returns the refresh credential only in the existing one-shot redacted cookie handoff. The policy supplies matching past-expiry cookie options for logout. The access-token issuer must be wired to the same store object; the store returns the account ID to trusted code after rotation. A missing user, token/signing error or store exception fails closed.

The service makes a best-effort revocation if signing or cookie creation fails after a committed rotation; if storage is then unavailable it returns 503 without asserting revocation. A logout storage failure returns 503 and an expired-cookie handoff: clearing a browser cookie is **not** evidence that the server family was revoked. Callers must actually enforce `no-store`, emit headers/cookies correctly and integrate all other guards in `021C07`; this component does not do those things itself. Concurrent tests below exercise the shared store, not a simultaneous browser or HTTP route.

## Verification

- Before implementation the new WP test failed with exit 2 because the required service file did not exist. Three subsequent disposable real-WordPress runs passed **17/17** checks each: WordPress 6.4.3/PHP 8.1.27, 6.6.2/PHP 8.2.25 and 7.1.3/PHP 8.3.35; MariaDB 10.6.28. Image tags are mutable and the listed versions were observed during these runs.
- Covered origin/method/header rejection, missing/unknown/expired credential, rotation, replay and family separation, cookie flags/redaction, logout/idempotence, password change, failed signing, failed cleanup, closed database, and mismatched installation/store wiring. Tests do not exercise HTTP headers or a browser.
- Existing real-WordPress password-login regression passed **21/21** on WP 6.6.2/PHP 8.2.25. Existing transactional session-store suite passed **36/36**, including two competing refreshes, refresh vs logout, password-change race, commit/connection failures and rollback. Its isolated synthetic database was separate from the WP test database.
- PHP 8.2 `php -l` passed for the store, token, policy, new service and new WP test. Running the new test without its disposable flag/prefix refused execution with exit 2. No manual QA was applicable to the inactive service.

## Reproduction — disposable containers only

First confirm that the exact names are unused. These databases use tmpfs, no published ports and synthetic credentials; stopping only these exact test containers destroys their temporary data. Do not point this test at preserved WordPress sites or databases.

```powershell
docker run --detach --rm --name carmilla-p00-refresh-qa-20261018 --tmpfs /var/lib/mysql:rw --env MARIADB_ROOT_PASSWORD=synthetic_root_password --env MARIADB_DATABASE=carmilla_login_qa --env MARIADB_USER=synthetic_qa --env MARIADB_PASSWORD=synthetic_qa_password mariadb:10.6
# Wait for the final MariaDB log line reporting port 3306, not initialization port 0.
$taskBridge = (Resolve-Path 'wordpress/carmilla-bridge').Path.Replace('\', '/') + ':/work:ro'
foreach ($taskImage in @('wordpress:6.4-php8.1-apache', 'wordpress:6.6-php8.2-apache', 'wordpress:php8.3-apache')) {
    $taskPrefix = 'qa_' + [guid]::NewGuid().ToString('N').Substring(0,12) + '_'
    $taskOutput = docker run --rm --network container:carmilla-p00-refresh-qa-20261018 --volume $taskBridge --env CARMILLA_LOGIN_DISPOSABLE=yes --env "CARMILLA_LOGIN_TEST_PREFIX=$taskPrefix" $taskImage php /work/tests/smoke-web-refresh-logout-real-wp.php
    $taskExit = $LASTEXITCODE
    $taskOutput
    if ($taskExit -ne 0 -or -not ($taskOutput -match '^PASS web refresh logout WP ')) { throw 'WP test did not pass' }
}
docker stop carmilla-p00-refresh-qa-20261018
```

The login regression used the same disposable DB and a new random prefix with `/work/tests/smoke-web-login-real-wp.php`; its `Checks: 21; failures: 0.` marker and exit code were verified. The concurrency regression used a second exact-name temporary container `carmilla-p00-refresh-race-20261018` with `--tmpfs /var/lib/mysql:rw`, `MARIADB_ALLOW_EMPTY_ROOT_PASSWORD=yes`, database `carmilla_session_qa`, `CARMILLA_TEST_DB_PORT=3306` and a random `CARMILLA_TEST_TABLE_PREFIX`. Both exact temporary containers were stopped after checking their tmpfs configuration; their synthetic data was removed, not the retained sites.
