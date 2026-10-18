# P00-PWA-AUTH-WP-021C05B01 — legacy email reset characterization

2026-10-18. This is a finding, **not a fix or a security pass**. No real site or account was modified. The test used synthetic users and intercepted WordPress mail before delivery; no raw reset keys, JWTs, email bodies or personal data were printed or retained in this evidence.

## Observed behavior

On WordPress 6.4.3/PHP 8.1.27, WordPress 6.6.2/PHP 8.2.25 and WordPress 7.1.3/PHP 8.3.35, with disposable MariaDB 10.6.28, the existing Bridge forgot-password controller generated one WP reset key and stored a transient indexed by it, then invoked `retrieve_password`, which generated a second key selected for the standard WordPress email. Before reset, WP core rejected the first key and accepted the second.

The legacy Bridge `reset_password` handler rejected the second, email-selected key as `INVALID_TOKEN` because no matching Bridge transient existed. The same handler accepted the first transient key and changed the synthetic password even though WP core had invalidated that key. The test did not print either key or returned authentication response. The unknown-email response remained generic. All eight characterization assertions passed for each version; the base synthetic identity fixture also passed. These passes establish that this behavior was reproduced, **not that password reset is safe**.

The normal email contains a WordPress reset link; this test does not claim that the native WordPress link itself fails. Nor does it demonstrate remote theft of the first key, concurrent replay, exploitation on a live deployment, OTP behavior or browser end-to-end reset. It does show an incompatibility and a stale-key validation problem in the legacy handler that must be accounted for in a secure fix. A naive second-key-only patch would not establish atomic one-time use or preserve the separate mobile and browser contracts.

PHP 7.4 lint and the disposable guard passed. The first exploratory run emitted only a missing `REMOTE_ADDR` warning in the synthetic mail template; the fixture was corrected to use the reserved documentation address `192.0.2.1`, then all three successful runs were repeated without that warning. Image tags are mutable. The database container had no host ports or bind mounts and used tmpfs; after inspection it was stopped, destroying only its synthetic data.

## Reproduction on disposable Docker only

Check the exact database container name is unused; wait for the final MariaDB ready message on port 3306. The credentials are synthetic. Require both exit 0 and the final marker; WP bootstrap failures can exit zero.

```powershell
docker run --detach --rm --name carmilla-p00-reset-probe-20261018 --tmpfs /var/lib/mysql:rw --env MARIADB_ROOT_PASSWORD=synthetic_root_password --env MARIADB_DATABASE=carmilla_login_qa --env MARIADB_USER=synthetic_qa --env MARIADB_PASSWORD=synthetic_qa_password mariadb:10.6
$taskBridge = (Resolve-Path 'wordpress/carmilla-bridge').Path.Replace('\','/') + ':/bridge:ro'
$taskTests = (Resolve-Path 'wordpress/carmilla-bridge/tests').Path.Replace('\','/') + ':/work:ro'
foreach ($taskImage in @('wordpress:6.4-php8.1-apache', 'wordpress:6.6-php8.2-apache', 'wordpress:php8.3-apache')) {
    $taskPrefix = 'qa_' + [guid]::NewGuid().ToString('N').Substring(0,12) + '_'
    $taskOutput = docker run --rm --network container:carmilla-p00-reset-probe-20261018 --volume $taskBridge --volume $taskTests --env CARMILLA_LOGIN_DISPOSABLE=yes --env "CARMILLA_LOGIN_TEST_PREFIX=$taskPrefix" $taskImage php /work/characterize-email-reset-real-wp.php
    $taskExit = $LASTEXITCODE
    $taskOutput
    if ($taskExit -ne 0 -or -not ($taskOutput -match '^PASS email reset lifecycle characterization WP ')) { throw 'Reset characterization failed' }
}
```

The legacy handler is unchanged. `P00-PWA-AUTH-WP-021C05`, the secure browser reset, integration and manual QA remain open.
