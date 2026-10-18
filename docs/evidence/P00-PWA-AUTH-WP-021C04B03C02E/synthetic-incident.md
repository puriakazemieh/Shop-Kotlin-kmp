# P00-PWA-AUTH-WP-021C04B03C02E — redacted synthetic deletion incident

Date: 2026-10-18. This experiment is confined to disposable WordPress and a temporary MariaDB instance with synthetic accounts. It is not a production incident system or an administrator warning. Native WP deletion, its false-success return and callback side effects remain unchanged. No preserved site, real user, real-site schema, route, plugin or deployment was modified.

## What was tested

[The fixture](../../../wordpress/carmilla-bridge/tests/experiment-native-deletion-incident.php) uses [the existing disposable WordPress/shared-reference setup](../../../wordpress/carmilla-bridge/tests/experiment-identity-shared-references.php). It observes native `delete_user` and `deleted_user` hooks and compares synthetic in-memory pre/post metadata with local user and research claim/reference rows. A test listener at priority 99 still executes. This is one observed local scenario, not proof of arbitrary plugin compatibility.

When the observer sees the same partial-delete condition as C02C/C02D and its insert/readback succeed, a synthetic InnoDB table records only a random 128-bit incident ID, a fixed incident category, a fixed source category and UTC time. The ID is an event identifier, not an account identifier or an authentication credential. The table has exactly those four columns; the independent reader verifies the schema and allowlisted values. No user ID, metadata, email, phone, credential, access token or recovery snapshot is persisted to this table or printed in this evidence. A timestamp could still be correlated with other data, so this is data minimization, not a general anonymity or privacy-compliance claim. The record cannot identify the affected user for real-world recovery.

| Case | Observed result in each combination |
| --- | --- |
| Clean synthetic deletion | WP returned true, the four inspected local tables had no remaining rows for the user, and no incident was written. This is `NO_LOCAL_ANOMALY`, not a universal clean bill of health. |
| Injected partial metadata deletion | WP returned true and another listener executed, yet the user and mobile metadata survived. The observer recorded a redacted partial-deletion incident. |
| Repeated store using the same in-memory incident ID | One row remained; two additional attempts read the same permitted incident values. This does not establish deduplication across process loss or distinct IDs. |
| Injected database rejection of the incident INSERT | The observer returned `UNVERIFIED` in process memory, not `NO_LOCAL_ANOMALY` or `RECORDED`, and no new incident row appeared. Once the test removed the injected failure, an explicit in-process retry with the same ID stored exactly one row. |
| Simulated unavailable observer probe | `UNVERIFIED` in process memory, no new incident; this is not a real network outage or crash. |
| Separate process readback | The writer process exited normally; a later independent PHP process, with no shared memory or WP bootstrap, connected to the still-running temporary DB and verified both redacted rows. |

| WordPress writer | PHP writer/reader | MariaDB | Writer and independent reader |
| --- | --- | --- | --- |
| 6.4.3 | 8.1.27 | 10.6.28 | PASS, exit 0 |
| 6.6.2 | 8.2.25 | 10.6.28 | PASS, exit 0 |
| 7.1.3 | 8.3.35 | 10.6.28 | PASS, exit 0 |

`php -l` passed with PHP 8.2. A run without the disposable flag/prefix was rejected before bootstrap, exit 2. Image tags are mutable; versions above are observed values. The database had no published port and its data was in tmpfs.

## Reproduction on disposable Docker only

Never run this on a preserved or production site. Verify the exact temporary container name is unused, start it, then wait until its final MariaDB log reports port 3306 ready. Credentials below are synthetic fixture values. The second process must start only after its corresponding writer has exited successfully.

```powershell
docker run --detach --rm --name carmilla-p00-incident-qa-20261018 --tmpfs /var/lib/mysql:rw --env MARIADB_ROOT_PASSWORD=synthetic_root_password --env MARIADB_DATABASE=carmilla_login_qa --env MARIADB_USER=synthetic_qa --env MARIADB_PASSWORD=synthetic_qa_password mariadb:10.6
$taskTests = (Resolve-Path 'wordpress/carmilla-bridge/tests').Path.Replace('\', '/') + ':/work:ro'
foreach ($taskImage in @('wordpress:6.4-php8.1-apache', 'wordpress:6.6-php8.2-apache', 'wordpress:php8.3-apache')) {
    $taskPrefix = 'qa_' + [guid]::NewGuid().ToString('N').Substring(0,12) + '_'
    docker run --rm --network container:carmilla-p00-incident-qa-20261018 --volume $taskTests --env CARMILLA_LOGIN_DISPOSABLE=yes --env "CARMILLA_LOGIN_TEST_PREFIX=$taskPrefix" --env CARMILLA_INCIDENT_MODE=write $taskImage php /work/experiment-native-deletion-incident.php
    if ($LASTEXITCODE -ne 0) { throw 'Synthetic incident writer failed' }
    docker run --rm --network container:carmilla-p00-incident-qa-20261018 --volume $taskTests --env CARMILLA_LOGIN_DISPOSABLE=yes --env "CARMILLA_LOGIN_TEST_PREFIX=$taskPrefix" --env CARMILLA_INCIDENT_MODE=read $taskImage php /work/experiment-native-deletion-incident.php
    if ($LASTEXITCODE -ne 0) { throw 'Synthetic incident reader failed' }
}
docker run --rm --volume $taskTests wordpress:6.6-php8.2-apache php -l /work/experiment-native-deletion-incident.php
docker stop carmilla-p00-incident-qa-20261018
```

Stopping the exact temporary container destroys its synthetic tmpfs database. It does not target the user's preserved WordPress installations.

## Boundaries and unresolved blocker

The result shows persistence **across normal writer-process termination while the same database is running**. It does not show disk persistence, database/container restart, crash during the hook, interrupted transaction, loss of network, or any durable fallback when the incident insert itself fails. `UNVERIFIED` is only in process memory; if that process ends after a failed insert, this experiment has no independent warning, durable retry or alert. The read-after-write can also fail after a write succeeds, so that outcome remains unverified rather than evidence that no row exists.

No warning delivery, admin UI, operator authorization, audit trail, retention, durable recovery snapshot, privacy policy, affected-account correlation, automatic recovery, production schema or product integration exists. Direct SQL bypasses native WP hooks. Other callback order, subsequent writes, unknown plugin tables, multisite, persistent caches and external side effects are outside this test. The fixture does not undo callbacks, emails or deleted/reassigned content, and the original WP success message remains potentially misleading. Parent `021C04B03C02` stays BLOCKED; this result does not complete registration, P00 or the overall C08 task. No human browser QA applies to this isolated research experiment.
