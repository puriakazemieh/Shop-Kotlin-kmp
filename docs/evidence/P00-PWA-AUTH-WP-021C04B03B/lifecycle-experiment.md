# P00-PWA-AUTH-WP-021C04B03B — bounded user-row lifecycle experiment

Date: 2026-10-17. This is **research on a disposable synthetic database**, not a safe registration implementation or an authorization for installation or migration. No preserved WordPress site was modified. No notification emails were sent; the isolated test intercepts `wp_mail`.

## Method and results

The fixture starts with the previous real-core INSERT experiment, creates test-only UPDATE and DELETE triggers, and exercises users created after those triggers. Both the WordPress users table and claim table use InnoDB. The claims represent **only login and nonempty email references in one source row**; equality uses exact synthetic ASCII/binary bytes. One claim remains if either field still references it. The fixture rejects changing the source user ID, which it does not model. The pre-existing synthetic administrator is not backfilled.

| WordPress / PHP | Lifecycle matrix |
| --- | --- |
| 6.4.3 / 8.1.27 | 18 checks passed |
| 6.6.2 / 8.2.25 | 18 checks passed, plus five repeated runs |
| 7.1.3 / 8.3.35 | 18 checks passed |

Tests covered real `wp_update_user` and `wp_delete_user`, direct SQL updates/deletes, same-owner aliases and field changes, cross-namespace conflicts, rejected ID changes, transaction rollback of update/delete and claim state, and concurrent independent processes for update/update, insert/update, SQL insert versus WordPress update, delete/insert, and delete/update. Each examined race checked the resulting rows and owners; deadlock or rejected statements are not treated as successful writes. PHP syntax and fail-closed refusal without the disposable flag and random prefix were also checked. Tests are samples, not an exhaustive interleaving proof.

## New unresolved WordPress behavior

On all three versions a real `wp_update_user` attempted a cross-namespace conflict that WordPress's normal same-column email check did not catch. The database trigger rejected the row update and the original row and ownership remained intact, **but WordPress returned the existing user ID as if successful and fired `profile_update`**. The test intercepted email and observed the hook, not actual third-party effects. As with the previous INSERT finding, database atomicity does not make callers or external side effects truthful and reversible. This is a critical compatibility blocker, not an implementation ready for real accounts.

The test does not claim that a failed WordPress delete rolls back prior metadata, hooks, content reassignment or external operations. It does not solve the previously observed zero-ID `user_register` behavior. The composite gate must address both findings for every supported writer or remain blocked.

## Boundaries and reproduction

- No `cb_mobile`, multi-row metadata references, OTP, production comparison/normalization, existing-identity migration, arbitrary DDL, drop-ins, multisite, engine matrix, replicated topology, recovery or real deployment was tested. Claims do not protect arbitrary writers able to bypass or modify the test schema. An atomic database statement does not imply atomic application hooks, notifications or other external effects.
- For a clean reproduction, use the isolated memory-backed MariaDB setup and fresh random test prefix described in [the preceding INSERT evidence](../P00-PWA-AUTH-WP-021C04B03A/insert-experiment.md). Run `wordpress/carmilla-bridge/tests/experiment-identity-user-lifecycle.php` in a WordPress container attached only to that isolated database, with `CARMILLA_LOGIN_DISPOSABLE=yes` and `CARMILLA_LOGIN_TEST_PREFIX=qa_<12 lowercase hexadecimal characters>_`. The script runs the INSERT fixture first and then the lifecycle checks. Use a new prefix for each execution; mount the bridge test directory read-only.
- Inspect and stop only the explicitly created disposable database container afterward. Its memory-backed synthetic data is discarded, not a preserved site. `wordpress:latest` resolved to WordPress 7.1.3 / PHP 8.3.35 in this run; it is not an immutable image identifier.
