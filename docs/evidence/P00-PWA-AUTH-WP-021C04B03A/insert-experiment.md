# P00-PWA-AUTH-WP-021C04B03A — bounded user INSERT feasibility

Date: 2026-10-17. This is a **synthetic database experiment**, not a protected registration implementation or approval to deploy. No preserved WordPress installation, production data, existing route, theme or plugin was modified.

## Method and observed results

A fresh Docker MariaDB 10.6.28 container used a memory-backed data directory and a database containing only synthetic WordPress installations. The installations used real WordPress core with fresh random `qa_..._` table prefixes. The test script embeds a test-only InnoDB claims table and AFTER INSERT trigger; all fixture values are synthetic ASCII and compared as exact binary bytes. The existing core administrator is installed before the trigger and **not backfilled**. This intentionally does not represent migration of an existing site.

| Real core / PHP | Unprotected baseline | INSERT-only experiment |
| --- | --- | --- |
| WordPress 6.4.3 / PHP 8.1.27 | Two concurrently created duplicate identities | 12 checks passed |
| WordPress 6.6.2 / PHP 8.2.25 | Two concurrently created duplicate identities | 12 checks passed |
| WordPress 7.1.3 / PHP 8.3.35 | Two concurrently created duplicate identities | 12 checks passed |

Each test uses two independent PHP processes. For WordPress the barrier is after its normal login/email uniqueness checks but before insertion; direct SQL workers do not consult an application lock. The protected fixture verified identical and cross-namespace WP races, a direct-SQL race, independent WP creations, equal login/email on one owner, distinct login/email conflicts, rollback of an earlier claim after a later statement conflict, and explicit transaction rollback of both source and claims. Both WP and claim tables were transactional InnoDB. A separate run without the explicit disposable flag and random prefix failed closed before database access; PHP syntax check passed. The unprotected run is evidence of a defect, not a security pass.

## Significant compatibility finding — not resolved

In all three tested core versions, when the trigger rejected a conflicting `wp_insert_user` INSERT, the database kept one source row/owner and no partial claim, but WordPress returned **user ID 0**, not a `WP_Error`, and invoked `user_register` with ID 0. The test observed the hook; it did not demonstrate which real plugins send email or perform other side effects. Applications that check only `is_wp_error()` can mistakenly treat ID 0 as success, and a database rollback cannot be assumed to undo PHP callbacks or external effects. This rules out presenting the trigger as a drop-in safe registration solution. Future composite proof must address this finding for supported writers and fail closed otherwise. `021C04B03`, `021C04B`, and the actual registration/PWA work remain blocked; no token or real account behavior has been changed here.

## Boundaries

- Only new-row INSERT, exact binary fixture keys, the selected MariaDB version and exercised cases were tested. WP collation, case folding, Unicode, email variants, mobile normalization, `cb_mobile`, user UPDATE/DELETE and claim-reference lifecycle remain unproven.
- The fixture does not claim coverage for migration/backfill, existing duplicates, multisite, replication, other engines, host permissions, custom database drop-ins, privileged DDL bypass, concurrent schema changes, failure recovery or external actions. An ordinary direct SQL INSERT was tested; a privileged actor who can alter triggers or claims is outside this proof.
- The test inserts a single claim for a same-row equal login/email; it is not the multi-reference lifecycle needed for future updates/deletes.
- No synthetic test result is evidence that an existing customer identity can be safely merged or that login, OTP, registration or the broader P00 manual QA is complete.

## Reproduction outline

1. Create an isolated MariaDB 10.6 container with a memory-backed `/var/lib/mysql`, a synthetic-only `carmilla_login_qa` database/user, and grant that test user `TRIGGER` only within the synthetic database. Never point the test at a preserved or live site.
2. Run `wordpress/carmilla-bridge/tests/audit-wp-registration-identity-race.php` and `wordpress/carmilla-bridge/tests/experiment-identity-user-insert.php` in separate WordPress containers sharing the synthetic database container's network namespace. Mount only `wordpress/carmilla-bridge` read-only. Set `CARMILLA_LOGIN_DISPOSABLE=yes` and a fresh random `CARMILLA_LOGIN_TEST_PREFIX=qa_<12 lowercase hexadecimal characters>_` for **each** run.
3. Confirm the baseline reports the unsafe race, and the experiment reports all 12 checks including the zero-ID hook finding. Run once per listed image; `wordpress:latest` was resolved to the versions above at test time, not assumed to be immutable. The scripts suppress raw database errors and do not print fixture identities or passwords.
4. Stop only the specifically created synthetic container after verifying its identity. Its memory-backed synthetic data is discarded; preserved WordPress directories and databases must not be removed.
