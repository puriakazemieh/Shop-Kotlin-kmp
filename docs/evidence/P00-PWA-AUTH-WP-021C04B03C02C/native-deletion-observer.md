# P00-PWA-AUTH-WP-021C04B03C02C — native deletion observer fixture

Date: 2026-10-18. The owner chose to retain normal WordPress deletion and plugin compatibility, and to design detection and recovery for database failures while acknowledging that WP may still show a false success. This record is **only an isolated detection experiment**, not a product feature, recovery, or proof of universal compatibility. No preserved site, real user, route, core file, plugin or schema was modified.

## Fixture and observations

The existing [shared-reference research fixture](../../../wordpress/carmilla-bridge/tests/experiment-identity-shared-references.php) installs actual WP with a randomly prefixed disposable schema and synthetic users. The [observer experiment](../../../wordpress/carmilla-bridge/tests/experiment-native-deletion-observer.php) attaches ordinary test callbacks to native `delete_user`, metadata deletion and `deleted_user`; it does not suppress third-party callbacks or alter WP core. A separate test listener at priority 99 records that another callback can execute. Only synthetic metadata is kept in process memory; no metadata values, credentials or user identifiers are logged.

In each of the three runs:

- Normal `wp_delete_user` returned `true`; native hooks and the other listener fired in the observed order. The observer saw no remaining user, usermeta, or synthetic claim/reference rows and reported `NO_LOCAL_ANOMALY`. That result refers **only to those queried local tables at that instant**, not all plugin data or external effects.
- A session-scoped injected error rejected deletion of a specific synthetic `cb_mobile` metadata row. `wp_delete_user` still returned `true`, both native mobile metadata callbacks ran, and the other listener ran. Raw SQL confirmed the user and mobile row survived, while the metadata snapshot changed. The observer reported `PARTIAL_USER_SURVIVES`; the synthetic mobile claim remained with the user.
- A deliberately invalid observer query and a separately caught synthetic observer exception each produced `UNVERIFIED`, not a clean deletion result. An explicitly simulated `deleted_user` event without a preceding observed `delete_user` likewise produced `UNVERIFIED`. These are bounded simulations, not actual network disconnection or process-crash tests.
- Direct SQL deletion of a different synthetic user did not fire the WP hooks, so this observer did not run. No claim is made about direct writers being covered.

The observations do not prove that arbitrary observers, other-priority callbacks, plugins or all WP versions behave identically. The synthetic claim table is part of the research fixture, not an installed production contract. Separate enum branches for a surviving unchanged user and orphaned local data are not exercised here.

## Results

| WordPress | PHP | MariaDB | Result |
| --- | --- | --- | --- |
| 6.4.3 | 8.1.27 | 10.6.28 | PASS, exit 0 |
| 6.6.2 | 8.2.25 | 10.6.28 | PASS, exit 0 |
| 7.1.3 | 8.3.35 | 10.6.28 | PASS, exit 0 |

`php -l` on PHP 8.2 passed. Without the disposable flag and a valid random prefix the fixture was rejected before WordPress bootstrap; Docker reported exit 1. Database and WP images use locally available tags; their versions above are the observed runtime versions, not a promise that mutable tags will always resolve identically.

## Reproduction on disposable Docker only

Never point this fixture at a preserved or production site. Verify the exact temporary container name is unused. The database has no published port; `/var/lib/mysql` is tmpfs. Synthetic credentials below are used only in that temporary container. Wait for the final MariaDB log showing port 3306 ready before running the fixture.

```powershell
docker run --detach --rm --name carmilla-p00-deletion-observer-qa-20261018 --tmpfs /var/lib/mysql:rw --env MARIADB_ROOT_PASSWORD=synthetic_root_password --env MARIADB_DATABASE=carmilla_login_qa --env MARIADB_USER=synthetic_qa --env MARIADB_PASSWORD=synthetic_qa_password mariadb:10.6
$taskTests = (Resolve-Path 'wordpress/carmilla-bridge/tests').Path.Replace('\', '/') + ':/work:ro'
foreach ($taskImage in @('wordpress:6.4-php8.1-apache', 'wordpress:6.6-php8.2-apache', 'wordpress:php8.3-apache')) {
    $taskPrefix = 'qa_' + [guid]::NewGuid().ToString('N').Substring(0,12) + '_'
    docker run --rm --network container:carmilla-p00-deletion-observer-qa-20261018 --volume $taskTests --env CARMILLA_LOGIN_DISPOSABLE=yes --env "CARMILLA_LOGIN_TEST_PREFIX=$taskPrefix" $taskImage php /work/experiment-native-deletion-observer.php
}
docker run --rm --volume $taskTests wordpress:6.6-php8.2-apache php -l /work/experiment-native-deletion-observer.php
docker stop carmilla-p00-deletion-observer-qa-20261018
```

Stopping that exact temporary container destroys its synthetic tmpfs database. It does not target any of the user's preserved WordPress installations.

## Limitations and next work

This fixture neither persists incidents nor delivers an administrator warning; a crash before/inside the hook or loss of the process can prevent reporting. An exception caught by the fixture is not equivalent to a fatal process failure. Other callbacks can change state before or after this observer, and database availability, persistent caches, concurrent writers, posts/reassignment, multisite, unknown plugin tables, arbitrary direct SQL and external services are not covered. SQL reads can fail and `UNVERIFIED` itself is currently only in memory. No recovery snapshot or restore was created; callbacks, email and third-party effects are not reversible by this observer.

Next, `021C04B03C02D` must test a strictly synthetic, conflict-aware recovery approach without overwriting concurrent changes or claiming to reverse third-party effects. Durable incident capture, authorization, privacy, real plugin compatibility, crash handling and product integration remain separate work. Parent `021C04B03C02` and the PWA registration path remain blocked; no human browser QA is requested for this research fixture.
