# P00-PWA-AUTH-WP-021C04B03C02F — synthetic incident correlation and access

Date: 2026-10-18. This experiment is limited to disposable WordPress/MariaDB and synthetic users. It neither implements a product feature nor modifies preserved WordPress installations. Native WP deletion may still falsely report success; callback and external side effects are unchanged.

## Scope

[The fixture](../../../wordpress/carmilla-bridge/tests/experiment-native-incident-access.php) bootstraps [the disposable shared-reference research model](../../../wordpress/carmilla-bridge/tests/experiment-identity-shared-references.php). A session-scoped error causes a partial native deletion. The test observer detects a surviving user with changed metadata and writes a random incident ID, that synthetic account's numeric WordPress ID, a fixed incident category, recorded time and expiry time to a research-only InnoDB table. The test asserts that those are the only columns. No account metadata, passwords, telephone numbers, email addresses or real user information are written to the incident table or this evidence.

The numeric account reference **is linkable personal data on a real site**, even without contact details. The experiment is not a privacy policy, legal assessment or authorization to store it in production. It excludes a recovery snapshot and cannot itself repair a user. Server-side read uses WordPress `current_user_can('manage_options')` as a fixture-only authorization choice, checked before incident lookup; this is not a reviewed production capability model. A one-hour fixture expiry is used only to exercise a boundary, not to establish a retention policy or automated purge.

## Results

| Test | Observed result in all three runs |
| --- | --- |
| Partial native deletion | WordPress reported success although the synthetic user/mobile survived; the observer stored the related synthetic account ID. |
| Authorized administrator | Two reads returned the expected reference and did not add or change incident rows. |
| Editor and logged-out caller | Both received `FORBIDDEN` before the fixture performed an incident lookup. |
| Unknown incident / later deleted synthetic user | Returned `MISSING_INCIDENT` / `MISSING_ACCOUNT`; neither was described as a recovered account. |
| Failed SQL read / injected incident INSERT failure | Returned `UNVERIFIED`. The rejected insert created no new incident row. This is not a database/network outage or a durable fallback test. |
| Expiry boundary | An incident whose expiry was set to the current DB time returned `EXPIRED` without returning the account ID; the synthetic user remained. Setting expiry into the future made it available again. This tests access only, not automatic deletion or retention enforcement. |

| WordPress | PHP | MariaDB | Result |
| --- | --- | --- | --- |
| 6.4.3 | 8.1.27 | 10.6.28 | PASS, final fixture marker and exit 0 |
| 6.6.2 | 8.2.25 | 10.6.28 | PASS, final fixture marker and exit 0 |
| 7.1.3 | 8.3.35 | 10.6.28 | PASS, final fixture marker and exit 0 |

PHP 8.2 `php -l` passed. Without the disposable flag and random prefix, execution stopped before bootstrap with exit 2. The exact image versions are observed, while tags can change. One early attempt was made before MariaDB was ready: WordPress printed a database-connection error but the PHP process exited 0. It is **not counted as a pass**. The successful runs check both exit status and the final `PASS synthetic incident access WP` marker.

## Reproduction — disposable Docker only

Never run on a preserved or production site. Verify the exact temporary container name is unused and wait until MariaDB's final log reports port 3306 ready. All credentials below are synthetic. The success marker check is essential because WordPress can exit zero on a bootstrap error.

```powershell
docker run --detach --rm --name carmilla-p00-incident-access-qa-20261018 --tmpfs /var/lib/mysql:rw --env MARIADB_ROOT_PASSWORD=synthetic_root_password --env MARIADB_DATABASE=carmilla_login_qa --env MARIADB_USER=synthetic_qa --env MARIADB_PASSWORD=synthetic_qa_password mariadb:10.6
$taskTests = (Resolve-Path 'wordpress/carmilla-bridge/tests').Path.Replace('\', '/') + ':/work:ro'
foreach ($taskImage in @('wordpress:6.4-php8.1-apache', 'wordpress:6.6-php8.2-apache', 'wordpress:php8.3-apache')) {
    $taskPrefix = 'qa_' + [guid]::NewGuid().ToString('N').Substring(0,12) + '_'
    $taskOutput = docker run --rm --network container:carmilla-p00-incident-access-qa-20261018 --volume $taskTests --env CARMILLA_LOGIN_DISPOSABLE=yes --env "CARMILLA_LOGIN_TEST_PREFIX=$taskPrefix" $taskImage php /work/experiment-native-incident-access.php
    $taskExit = $LASTEXITCODE
    $taskOutput
    if ($taskExit -ne 0 -or -not ($taskOutput -match '^PASS synthetic incident access WP ')) { throw 'Synthetic access test did not pass' }
}
docker run --rm --volume $taskTests wordpress:6.6-php8.2-apache php -l /work/experiment-native-incident-access.php
docker stop carmilla-p00-incident-access-qa-20261018
```

Stopping this exact temporary container removes only its synthetic tmpfs database, not the user's retained WordPress installations.

## Remaining limits

The prototype has no HTTP route, admin interface, alert delivery, audit trail, policy-governed retention, durable fallback, production capability review or actual recovery. Neither database shutdown/crash durability, concurrent deletion between read and use, privilege revocation during use, multisite, arbitrary plugins nor external writers were demonstrated. The test-only expiry does not purge rows. A failed incident insert is `UNVERIFIED` only in process memory; no persistent failure notification is guaranteed. The test does not reverse hooks, emails, removed content or WordPress's original success message. `021C04B03C02` and the P00 registration path remain blocked; no human browser QA applies to this isolated fixture.
