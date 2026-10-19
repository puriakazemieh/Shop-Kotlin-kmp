# Inactive atomic OTP consumer — limited synthetic evidence

2026-10-19. This is **not an integrated OTP fix**. No retained WordPress site, real user, real SMS or real endpoint was changed. The class is not loaded or wired to a route; the existing legacy OTP race remains exploitable under the characterized scheduling.

## Results

The isolated test used WordPress 6.4.3/PHP 8.1.27, 6.6.2/PHP 8.2.25 and 7.1.3/PHP 8.3.35 on MariaDB 10.6.28 with an InnoDB options table, database-backed transients and process-local caches. On each combination, two independent PHP processes were held after both validated the same synthetic code, then released; one consumed the matching database row and the other was rejected. Sequential reuse, malformed and incorrect code, unsupported purpose, expired code, persistent-cache mode, injected DB-delete failure and DB-read failure were rejected. Delete failure left the row intact. All runs exited 0 and printed the final PASS marker.

The first exploratory run exposed a variable-name collision in the fixture; after correction the full three-version matrix was rerun. PHP 7.4 syntax checks passed for both files. Running the fixture without its disposable flag and random prefix was rejected before bootstrap. No code or credential was logged.

## Reproduction only on the isolated synthetic fixture

The retained `carmilla-p00-identity-composite-20261018` database container uses its separate named persistent volume and has no host-published database port. Starting it and running the test adds only randomly prefixed synthetic tables; never point the fixture at an existing WordPress site or production. Images are mutable tags. The test installs a failure trigger only on its newly created synthetic options table.

```powershell
docker start carmilla-p00-identity-composite-20261018
$taskTests = (Resolve-Path 'wordpress/carmilla-bridge/tests').Path.Replace('\','/') + ':/work:ro'
$taskBridge = (Resolve-Path 'wordpress/carmilla-bridge').Path.Replace('\','/') + ':/bridge:ro'
foreach ($taskImage in @('wordpress:6.4-php8.1-apache', 'wordpress:6.6-php8.2-apache', 'wordpress:php8.3-apache')) {
    $taskPrefix = 'qa_' + [guid]::NewGuid().ToString('N').Substring(0,12) + '_'
    $taskOutput = docker run --rm --network container:carmilla-p00-identity-composite-20261018 --volume $taskTests --volume $taskBridge --env CARMILLA_LOGIN_DISPOSABLE=yes --env "CARMILLA_LOGIN_TEST_PREFIX=$taskPrefix" $taskImage php /work/test-otp-atomic-consumer-real-wp.php
    $taskExit = $LASTEXITCODE
    $taskOutput
    if ($taskExit -ne 0 -or -not ($taskOutput -match '^PASS atomic OTP consumer WP ')) { throw 'Test incomplete.' }
}
```

## Boundaries that remain open

The conditional delete only protects cooperating consumers under the tested database conditions. It does not make issuance, resend/cooldown, failed-attempt limits, authentication filters, identity uniqueness or the existing mobile handler safe. It does not support persistent object caches, noncooperating writers or mixed old/new consumers. It relies on database clock and has not established behavior under surrounding transactions, replicas/proxies, DB clock skew or other engines. A filter that overrides password verification or DB operations can also invalidate assumptions. Integration must address all of these and must preserve the mobile contract before use. Owner network/browser QA remains separate; neither `021C04C` nor the Phase 00 gate is complete.
