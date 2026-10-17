# P00-PWA-AUTH-WP-021C04B — identity race investigation

Date: 2026-10-17. Result: blocker, not a passed registration test or a claim about the contents of either preserved local WordPress site. No existing site, customer account or database was inspected or altered.

## Reproduction

`wordpress/carmilla-bridge/tests/audit-wp-registration-identity-race.php` installs WordPress in a separate disposable MariaDB database and starts two PHP processes against the same synthetic installation. Each calls actual `wp_insert_user()` with the same synthetic login and email. A test-only `pre_user_url` filter blocks each process after WordPress has checked login/email and before insertion. Only after both processes arrive does the harness release both. Each returns a newly created user; a fresh SQL count finds two corresponding rows. The harness prints only results, not credentials or user data.

| Core / PHP / MariaDB | Duplicate creation reproduced |
|---|---|
| WordPress 6.4.3 / PHP 8.1.27 / MariaDB 10.6 | yes |
| WordPress 6.6.2 / PHP 8.2.25 / MariaDB 10.6 | yes |
| WordPress 7.1.3 / PHP 8.3.35 / MariaDB 10.6 | yes |

The synthetic WordPress 6.6.2 schema reports non-unique `user_login_key` and `user_email` indexes, and usermeta has no unique constraint for `cb_mobile`. This is an observation about these tested environments, not a statement that every deployment has the same schema. The core checks in `wp_insert_user` do not make concurrent insertion atomic. `WP_INSTALLING` and the barrier exist only in the fixture; no plugins or production traffic were tested.

## Security and scope

The existing browser password-login helper now rejects ambiguous local identifiers across username, email and mobile, but this neither prevents duplicate creation nor repairs legacy mobile registration or OTP. Browser registration is not wired or enabled. The M card prohibits migrations and live hooks, so it cannot implement a global ownership guarantee across core, plugins, legacy mobile writes, admin edits and browser registration, or safely reconcile any pre-existing duplicates. Merely adding a lock inside a new route cannot constrain writers that do not use that lock; a simplistic unique email index also fails to model cross-namespace collisions and may be incompatible with existing data or extensions.

The proposed next work is to specify and review identity ownership and all supported writers, then design a reversible collision-aware migration with synthetic concurrent tests and a separate human review gate, and finally implement guarded registration including a truthful partial-account-creation failure result. Actual scope, compatibility and data migration strategy require that design; no schema change is implied or executed by this evidence.

No public route, cookie, real browser, successful registration flow, migration, owner QA or C08 completion is claimed.
