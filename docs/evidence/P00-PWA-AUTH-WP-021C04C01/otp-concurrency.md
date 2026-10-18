# P00-PWA-AUTH-WP-021C04C01 — legacy OTP concurrency characterization

2026-10-18. **Finding, not a fix or security pass.** No real site, account, SMS, phone number, code, bearer or refresh token was used or recorded. The test invokes the current Bridge mobile handler directly in isolated synthetic WordPress, not the unfinished browser endpoint.

## Observation

On WordPress 6.4.3/PHP 8.1.27, 6.6.2/PHP 8.2.25 and 7.1.3/PHP 8.3.35, the real `send_login_otp` issued a synthetic OTP with the SMS transport intercepted. Two independent PHP processes called the real `login_with_otp` handler for the same synthetic identity/code. A WP `transient_...` test hook held each process **after it read the shared OTP transient**, then released both. Both returned successful authentication responses containing access and refresh tokens; the test checked only the presence of values, never their contents. A later sequential request with the same code was rejected after simulating a fresh process-local cache.

Each characterization run passed its assertions and exited 0. The pass confirms the described race was reproduced under this controlled scheduling; it does **not** mean OTP is secure, that every concurrent attempt succeeds, or that an actual live-site exploit was observed. The barrier is test-only. The test uses database-backed transients and WP's process-local object cache, no external object cache. Other caches and legacy callers remain untested; they do not become safe by assumption.

The first exploratory runs did not reach the complete test: the fixture's installing mode initially kept transients process-local, and an included fixture reused a local variable name. Both issues were corrected. A subsequent exploratory run exposed stale issuer-process cache during the sequential check; the final fixture verifies there is no external object cache and clears only that synthetic process-local cache before simulating a fresh request. All three final versions were rerun successfully. PHP 7.4 syntax check passed, and execution without the required synthetic flag/prefix exited 2 before bootstrap.

## Reproduce only on the retained synthetic database

The test uses the separate `carmilla-p00-identity-composite-20261018` MariaDB fixture and its named persistent volume, with no host-published database port. Each run adds new synthetic tables under a random prefix. It must never be pointed at a retained user site or production. No real SMS is sent.

```powershell
$taskTests = (Resolve-Path 'wordpress/carmilla-bridge/tests').Path.Replace('\','/') + ':/work:ro'
$taskBridge = (Resolve-Path 'wordpress/carmilla-bridge').Path.Replace('\','/') + ':/bridge:ro'
foreach ($taskImage in @('wordpress:6.4-php8.1-apache', 'wordpress:6.6-php8.2-apache', 'wordpress:php8.3-apache')) {
    $taskPrefix = 'qa_' + [guid]::NewGuid().ToString('N').Substring(0,12) + '_'
    $taskOutput = docker run --rm --network container:carmilla-p00-identity-composite-20261018 --volume $taskTests --volume $taskBridge --env CARMILLA_LOGIN_DISPOSABLE=yes --env "CARMILLA_LOGIN_TEST_PREFIX=$taskPrefix" $taskImage php /work/characterize-otp-concurrency-real-wp.php
    $taskExit = $LASTEXITCODE
    $taskOutput
    if ($taskExit -ne 0 -or -not ($taskOutput -match '^PASS OTP concurrency characterization WP ')) { throw 'Characterization incomplete.' }
}
```

## Remaining work

The live legacy handler remains unchanged and the race remains unresolved. A secure fix must account for issuance and consumption, concurrent mobile as well as browser requests, actual configured persistent object caches, expiration, failed storage/lock operations and compatibility; a mutex only on an unregistered browser handler cannot protect the existing mobile verifier. Unique identity resolution and authentication-filter integration are also open. The parent `021C04C`, its dependencies and P00 gate remain incomplete. Until corrected and tested, do not rely on the existing OTP flow as a one-use security guarantee.
