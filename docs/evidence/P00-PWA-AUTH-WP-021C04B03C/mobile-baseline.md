# P00-PWA-AUTH-WP-021C04B03C — mobile reference baseline and scope review

Date: 2026-10-18. **This documents an unsafe synthetic baseline, not a mobile identity implementation.** Nothing was installed or changed on a preserved site. Only synthetic identities and a disposable MariaDB 10.6.28 database were used.

## Reproduced failure

The script first runs the prior real-WordPress user-row lifecycle fixture. It creates a new synthetic user whose login equals a synthetic ASCII mobile, and inserts **two** `cb_mobile` metadata rows for the same owner. A direct SQL change to that user's login causes the prior user-row trigger to release the old claim, although both metadata references remain. A second real WordPress `add_user_meta` call can then attach the same mobile to a different user; the test confirms two distinct owners. A separate contact-only `billing_phone` does not create an identity claim in this fixture.

| Actual WordPress / PHP | Premature release reproduced | Cross-owner mobile metadata reproduced |
| --- | --- | --- |
| 6.4.3 / 8.1.27 | Yes | Yes |
| 6.6.2 / 8.2.25 | Yes | Yes |
| 7.1.3 / 8.3.35 | Yes | Yes |

The `PASS` lines mean the **unsafe failure was reproduced**, not that ownership is protected. This does not establish the status of any real site's data, prove an account takeover occurred, or test OTP. PHP lint passed and the script refused to run without its explicit disposable flag and random table prefix.

## Why the remaining scope exceeds M

The earlier INSERT and lifecycle fixtures track only the two fields of one `wp_users` row and deliberately have no metadata-source references. Adding a standalone mobile check or trigger cannot safely implement the contract: claims must remain tied to every source reference across user login, email and potentially multiple mobile rows; user and metadata writes, ownership reassignment and removal then need transactional lock ordering and failure/concurrency tests. That is a new cross-table ownership model plus a separate high-risk integration/race matrix, not a small extension of the user-only trigger.

The work is split into two bounded sequential children: `021C04B03C01` constructs and tests the synthetic shared-reference model and sequential lifecycle; `021C04B03C02` tests reassignment, concurrency, rollback/failure and integration against real WordPress and independent SQL. The parent remains **BLOCKED**, and `021C04B03D` remains TODO. Child success will not itself establish production readiness, approved normalization, migration safety, OTP replay protection or compatibility of external hooks.

## Reproduction

Use the isolated memory-backed MariaDB setup and fresh random test prefix described in [the INSERT evidence](../P00-PWA-AUTH-WP-021C04B03A/insert-experiment.md). Mount `wordpress/carmilla-bridge` read-only in the real WordPress container, attach only to the disposable database's network namespace, and run `tests/audit-mobile-identity-claims.php` with `CARMILLA_LOGIN_DISPOSABLE=yes` and `CARMILLA_LOGIN_TEST_PREFIX=qa_<12 lowercase hexadecimal characters>_`. Use a new prefix per run. The audit seeds and checks the previous INSERT and UPDATE/DELETE fixtures before the mobile reproduction. No raw fixture identities or credentials are printed. Stop only the explicitly created disposable database container after verifying its identity.
