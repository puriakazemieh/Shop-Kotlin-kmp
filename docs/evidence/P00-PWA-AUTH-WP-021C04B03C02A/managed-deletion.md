# P00-PWA-AUTH-WP-021C04B03C02A — disposable deletion experiment

Date: 2026-10-18. This is a **research fixture**, not an implementation of WordPress user deletion. No preserved site, database, plugin, theme, route or schema was changed.

## Boundary and design decision

The observed native `wp_delete_user` path calls metadata deletion, ignores a failed deletion's return value, then can continue with user deletion and callbacks. The existing `C02` experiment reproduces this using an injected SQL error; this is not an assertion that a user's site experienced the error. SQL rollback cannot retract hooks, emails, external calls or other effects already performed.

The deliberately narrower prototype is a separate direct-SQL fixture, with a dedicated connection, a clean single-site synthetic installation, named synthetic accounts, no reassignment and no owned posts, links or comments. It locks the target user and metadata, checks every delete's result, and treats a failed or ambiguous transaction as failure/indeterminate. `users`, `usermeta`, `identity_claims` and `identity_refs` are checked as InnoDB. The fixture's identity triggers/procedures are research schema on the temporary database only. The path is exercised with its own writes and a retry, not arbitrary cooperating or uncooperative writers. Unknown plugin data, WordPress deletion hooks and all production deletion paths are explicitly unsupported.

On confirmed commit or rollback it invalidates the WordPress in-process user and user-meta caches. `clean_user_cache` can itself invoke callbacks: neither arbitrary callback effects nor persistent/external caches were tested or reversed. Native `delete_user`, `deleted_user`, `delete_user_meta` and `deleted_user_meta` hooks are **bypassed**, not globally disabled; therefore compatibility with normal plugins and WordPress's complete deletion lifecycle is absent. A controlled in-memory notification occurs only after commit, where an independent SQL connection verifies the four table views. If that notification fails, the committed deletion is not rolled back: the fixture has no durable outbox or delivery retry, and makes no exactly-once or external-service guarantee. A failed COMMIT or rollback is reported as `INDETERMINATE`, not safe success.

## Executed matrix and observations

All three runs exited 0, with MariaDB 10.6.28, independent random table prefixes and no host-published DB port:

| WordPress | PHP | Result |
| --- | --- | --- |
| 6.4.3 | 8.1.27 | PASS |
| 6.6.2 | 8.2.25 | PASS |
| 7.1.3 | 8.3.35 | PASS |

Each run first executes the previous shared-reference fixture, then verifies:

1. With an injected metadata-delete failure, native `wp_delete_user` returns `true` and emits deletion hooks even though the user and mobile claim remain and earlier metadata was removed.
2. The managed fixture successfully deletes an eligible user, metadata and identity rows; an independent connection sees committed absence before the one controlled notification. Local cache reads agree. Native deletion hooks are not called.
3. An injected failure on a later metadata row follows at least one successful delete statement. The controlled caller receives `FAILED`; exact persisted user, all metadata, claims and references match the pre-attempt snapshot, local cache reads agree, and there is no controlled notification or native deletion hook.
4. After clearing the synthetic session fault, retry commits once; a subsequent retry reports `NOT_FOUND` without a second notification. This is a sequential retry test, not a claim about simultaneous writers.
5. A synthetic account owning a post is refused as `UNSUPPORTED` without changing its identity snapshot. No reassignment is attempted.
6. An intentionally failing in-memory post-commit notification produces `COMMITTED_NOTIFICATION_FAILED` with the user still deleted; a retry does not repair that notification. This is an explicit remaining limitation.

The PHP 8.2 syntax check passed. Running without the disposable flag and valid random prefix was rejected before bootstrapping WordPress (Docker process exit 1; guard message: `Disposable test flag and random prefix required.`).

## Reproduction — isolated synthetic environment only

Run from this repository on a disposable Docker host; never point this fixture at a real or preserved database. Synthetic credentials below are only for this transient container. The MariaDB data directory is tmpfs, and the container publishes no port. Verify that the exact temporary container name is unused before creating it; stopping **this test container only** destroys its synthetic data.

```powershell
docker run --detach --rm --name carmilla-p00-managed-deletion-qa-20261018 --tmpfs /var/lib/mysql:rw --env MARIADB_ROOT_PASSWORD=synthetic_root_password --env MARIADB_DATABASE=carmilla_login_qa --env MARIADB_USER=synthetic_qa --env MARIADB_PASSWORD=synthetic_qa_password mariadb:10.6
# Wait for the container's MariaDB ready-for-connections log before running the tests.
$taskTests = (Resolve-Path 'wordpress/carmilla-bridge/tests').Path.Replace('\', '/') + ':/work:ro'
foreach ($taskImage in @('wordpress:6.4-php8.1-apache', 'wordpress:6.6-php8.2-apache', 'wordpress:php8.3-apache')) {
    $taskPrefix = 'qa_' + [guid]::NewGuid().ToString('N').Substring(0,12) + '_'
    docker run --rm --network container:carmilla-p00-managed-deletion-qa-20261018 --volume $taskTests --env CARMILLA_LOGIN_DISPOSABLE=yes --env "CARMILLA_LOGIN_TEST_PREFIX=$taskPrefix" $taskImage php /work/experiment-managed-synthetic-deletion.php
}
docker run --rm --volume $taskTests wordpress:6.6-php8.2-apache php -l /work/experiment-managed-synthetic-deletion.php
docker stop carmilla-p00-managed-deletion-qa-20261018
```

The exact fixture source is [experiment-managed-synthetic-deletion.php](../../../wordpress/carmilla-bridge/tests/experiment-managed-synthetic-deletion.php). Its prerequisite is the [shared-reference fixture](../../../wordpress/carmilla-bridge/tests/experiment-identity-shared-references.php).

## Remaining gate

This completes only the narrowly scoped experiment. The native deletion failure is **not fixed**, no production-ready deletion or login is claimed, and parent task `021C04B03C02` must separately reassess and rerun its complete acceptance. Concurrency with arbitrary plugins, uncooperative/direct SQL writers, multisite, posts/reassignment, custom tables, persistent caches, crash/commit ambiguity and external side effects remains unproved.
