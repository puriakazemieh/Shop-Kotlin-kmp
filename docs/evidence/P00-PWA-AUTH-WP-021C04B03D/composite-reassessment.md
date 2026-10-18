# P00-PWA-AUTH-WP-021C04B03D — composite identity reassessment

2026-10-18. **Result: BLOCKED; not a production identity solution or a complete composite acceptance.** The owner authorized assessing this card before its dependency, not waiving that dependency. No preserved WordPress site, real account or live route was changed.

## What was run

A new guarded [writer experiment](../../../wordpress/carmilla-bridge/tests/experiment-identity-composite-wp-writers.php) first runs the shared login/email/mobile claims-and-references fixture. It then tests real `wp_insert_user` and `wp_update_user` against that **same shared synthetic schema**, using cross-namespace identity conflicts, and checks actual persisted rows, claim ownership and callback observations. It purposely exits 4 after reproducing blockers. Three real WordPress/PHP image combinations were run:

| WordPress / PHP | Rejected insert | Rejected update | Result |
| --- | --- | --- | --- |
| 6.4.3 / 8.1.27 | returns ID 0; `user_register` executes | returns existing ID; `profile_update` executes | exit 4 / BLOCKED |
| 6.6.2 / 8.2.25 | same | same | exit 4 / BLOCKED |
| 7.1.3 / 8.3.35 | same | same | exit 4 / BLOCKED |

Each run checked that the rejected row change did not transfer the existing claim. The insert left no new user or claim; the update kept its old email/claim. Nevertheless, the WordPress return values and callback execution are not reliable indicators that the requested write happened. This is an injected database-trigger rejection, not a claim that the user's site has experienced it. Callback observation uses in-memory flags; no external integration was called, so its actual side effects are neither tested nor reversible by this evidence.

The existing [concurrency/failure fixture](../../../wordpress/carmilla-bridge/tests/experiment-identity-shared-concurrency.php) was independently rerun with fresh synthetic prefixes on the same three WP/PHP combinations. Its exercised claim races, rollback and forced deadlock checks reached their expected observations. Each run again found `wp_delete_user` returning success and calling delete hooks after an injected metadata failure, while the user and mobile remained and some metadata was already removed. A tested snapshot ordering produced one delete and a competing insert timeout, with no inconsistent claim in that run. Each run correctly exited 4 / BLOCKED. This is not an exhaustive scheduler or writer proof, and these are separate fresh installations using the same experimental schema, not one transaction combining all experiments.

PHP 7.4 syntax check passed; invoking the new script without the synthetic flag and prefix failed before bootstrap with exit 2. The tests used MariaDB 10.6, a new synthetic-only database and unique randomly generated table prefixes. The database container `carmilla-p00-identity-composite-20261018` publishes no host port and has only the separate named volume `carmilla-p00-identity-composite-20261018`; both are retained for future synthetic QA. Each test PHP container is temporary. No raw identity, token, password or SQL-driver error was recorded.

## Reproduction on the retained synthetic fixture only

```powershell
$taskTests = (Resolve-Path 'wordpress/carmilla-bridge/tests').Path.Replace('\','/') + ':/work:ro'
foreach ($taskImage in @('wordpress:6.4-php8.1-apache', 'wordpress:6.6-php8.2-apache', 'wordpress:php8.3-apache')) {
    foreach ($taskScript in @('experiment-identity-composite-wp-writers.php', 'experiment-identity-shared-concurrency.php')) {
        $taskPrefix = 'qa_' + [guid]::NewGuid().ToString('N').Substring(0,12) + '_'
        docker run --rm --network container:carmilla-p00-identity-composite-20261018 --volume $taskTests --env CARMILLA_LOGIN_DISPOSABLE=yes --env "CARMILLA_LOGIN_TEST_PREFIX=$taskPrefix" $taskImage php "/work/$taskScript"
        # Exit 4 is the deliberately reported blocker, NOT a passing gate.
        if ($LASTEXITCODE -ne 4) { throw 'Unexpected result; inspect sanitized output.' }
    }
}
```

Do not point these fixture scripts at a retained real site or production database. Repeated runs add fresh synthetic tables; they do not reset previous data. Image tags may change and the exact results require the versions above.

## What still blocks this card and its parents

- The chosen normal WordPress deletion approach still needs an operational, durable warning/recovery strategy with correctly scoped retention/authorization; WordPress and third-party callbacks can already have run and their external effects cannot be rolled back automatically. The previously built inactive observer/store/discovery do not establish durable fallback during a database outage.
- Supported writers need a proven contract for insert/update failure and side effects. The user chose normal WP/plugin compatibility; bypassing WP hooks with the narrow controlled-SQL experiment would violate that choice. A warning after a callback cannot undo its side effects. No supported-writer solution has yet been demonstrated.
- The combined matrix is incomplete for arbitrary plugins/direct SQL, cache layers, all concurrency orders, normalization, multisite/custom tables, migration/backfill, permissions, recovery, crash ambiguity and external effects. The trigger/procedure prototype is research-only. Neither migration nor browser registration may be activated based on these experiments.
- Owner manual QA can validate a later integrated fixture but cannot replace the missing security design or approve these results as a pass.
