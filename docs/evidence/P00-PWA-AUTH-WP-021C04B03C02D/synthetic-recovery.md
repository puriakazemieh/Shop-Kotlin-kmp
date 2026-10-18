# P00-PWA-AUTH-WP-021C04B03C02D — bounded synthetic recovery

Date: 2026-10-18. This is an isolated research fixture, not a recovery feature for a customer's site. The owner's selected direction retains native WordPress deletion. Neither the native false-success message nor the effects of callbacks were fixed. No preserved WordPress installation, real account, route, core file, deployment or real-site schema was changed.

## Scope and method

[The new fixture](../../../wordpress/carmilla-bridge/tests/experiment-native-deletion-recovery.php) runs actual WordPress using the [disposable shared-reference research fixture](../../../wordpress/carmilla-bridge/tests/experiment-identity-shared-references.php): synthetic users, randomly prefixed InnoDB tables and synthetic claims/references only. It creates no posts or comments for its target users. A session-specific database trigger injects failure when native `wp_delete_user()` deletes one synthetic `cb_mobile` row. In the observed case, WP returns `true`, the user and identity references survive, but some non-identity user metadata has already disappeared.

The experiment captures both states in memory, before deletion and after the injected failure. An ephemeral per-run key seals the fixture bundle; missing sections and subsequent truncation are refused. This demonstrates a narrow integrity check, **not** an authenticated durable backup, operator authorization, proof that the original capture was complete, or a production key-management scheme. The recovery is attempted only if the user row and all synthetic claim/reference rows match before and after, the remaining metadata is unchanged, and the missing rows do not include `cb_mobile`.

With `REPEATABLE READ`, the fixture locks and compares the current user, metadata and research identity rows with the post-failure state. It inserts only missing metadata using their exact original primary keys, without UPDATE, REPLACE or identity merging. It verifies the complete captured state before commit; a mismatch or failed insert rolls back. It invalidates the local metadata cache after commit and verifies the synthetic marker via the WP metadata API. These are direct SQL inserts in a synthetic environment: restoration does not execute normal metadata creation hooks.

## Observed results

| Test | Result in all three combinations |
| --- | --- |
| Native injected failure | WP reported success while the user survived with missing metadata; research identity claims/references stayed attached to that user. |
| Missing, incomplete or later-truncated in-memory snapshot | Refused without database writes. A missing required section was also refused when the test recomputed its seal. |
| Unchanged supported partial state | Missing metadata restored with original IDs; user, metadata, research claims and references exactly matched the captured pre-deletion state. The marker was readable through WordPress. |
| Independent SQL writer during the locked recovery | One attempted insertion into the locked target metadata range hit MariaDB lock timeout; the same kind of writer succeeded after commit. This is one controlled interleaving, not a complete concurrency proof. |
| Independent writer changed metadata before recovery | Refused as stale and preserved the changed state. A changed user record was independently refused and preserved. |
| Missing metadata ID occupied by another synthetic user | Recovery failed and rolled back its earlier inserts; both accounts' observed state remained unchanged. |
| Repeat after successful recovery | Returned `ALREADY_RESTORED` without writes. |

| WordPress | PHP | MariaDB | Result |
| --- | --- | --- | --- |
| 6.4.3 | 8.1.27 | 10.6.28 | PASS, exit 0 |
| 6.6.2 | 8.2.25 | 10.6.28 | PASS, exit 0 |
| 7.1.3 | 8.3.35 | 10.6.28 | PASS, exit 0 |

PHP 8.2 `php -l` passed. Without the disposable flag and valid random prefix the fixture rejected execution before WP bootstrap, exit 2. Tags are mutable; versions are the observed runtime versions, not a guarantee for future image pulls. Tests used a temporary Docker database with no published ports and `/var/lib/mysql` in tmpfs. No snapshot contents, real credentials or personal data were recorded here.

## Reproduction — disposable Docker only

Never point the fixture at a preserved or production site. First verify that the exact container name below is not already in use. Wait until the final MariaDB log reports port 3306 ready. The listed credentials are synthetic fixture values only.

```powershell
docker run --detach --rm --name carmilla-p00-recovery-qa-20261018 --tmpfs /var/lib/mysql:rw --env MARIADB_ROOT_PASSWORD=synthetic_root_password --env MARIADB_DATABASE=carmilla_login_qa --env MARIADB_USER=synthetic_qa --env MARIADB_PASSWORD=synthetic_qa_password mariadb:10.6
$taskTests = (Resolve-Path 'wordpress/carmilla-bridge/tests').Path.Replace('\', '/') + ':/work:ro'
foreach ($taskImage in @('wordpress:6.4-php8.1-apache', 'wordpress:6.6-php8.2-apache', 'wordpress:php8.3-apache')) {
    $taskPrefix = 'qa_' + [guid]::NewGuid().ToString('N').Substring(0,12) + '_'
    docker run --rm --network container:carmilla-p00-recovery-qa-20261018 --volume $taskTests --env CARMILLA_LOGIN_DISPOSABLE=yes --env "CARMILLA_LOGIN_TEST_PREFIX=$taskPrefix" $taskImage php /work/experiment-native-deletion-recovery.php
}
docker run --rm --volume $taskTests wordpress:6.6-php8.2-apache php -l /work/experiment-native-deletion-recovery.php
docker stop carmilla-p00-recovery-qa-20261018
```

Stopping that exact disposable container destroys its synthetic tmpfs database. It does not touch the user's preserved WordPress installations.

## Limits and outstanding work

The snapshots and seal exist only during this process. No durable incident, alert, backup retention policy, privacy control, real operator authorization, audit trail or production integration exists. This fixture starts and ends with known synthetic data; partial captures at source, process crashes, commit ambiguity, arbitrary races including ABA changes, direct writers outside the locked range, non-InnoDB tables, different isolation levels, multisite, persistent object caches and unknown plugins were not comprehensively tested. A WP hook or another writer can change things outside these four observed tables. Metadata insertion bypasses WP hooks; the fixture does not establish plugin compatibility. The chosen single failure cannot establish recovery when the identity row itself is lost or a user was actually deleted.

Callbacks, notifications, external actions, deleted/reassigned posts, comments and unknown plugin data cannot be reconstructed or undone by this evidence. The original native false success remains. Durable detection/warning and authorization/privacy are separate work; neither `021C04B03C02` nor its parent registration task is unblocked. There is no browser-facing change or manual QA request for this research fixture.
