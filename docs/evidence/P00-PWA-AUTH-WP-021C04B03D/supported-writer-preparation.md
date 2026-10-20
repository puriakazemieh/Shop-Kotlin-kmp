# Supported WordPress identity writer preparation — 2026-10-20

## Scope

`CB_Identity_Supported_Writer` is an inactive supported-writer primitive. It
uses normal `wp_insert_user()` behaviour, short MySQL named locks for the
exact supplied identifiers, then reads the committed WordPress rows again
after callbacks have run. It never creates triggers, does not install a
schema, does not delete a user on uncertainty, and does not register a REST
route.

If a callback or another writer creates an identity conflict after the core
write, it returns `cb_identity_recovery_required` rather than a user id and
marks only the new account with `cb_identity_writer_state=needs_recovery`.
This prevents a future caller from issuing credentials while falsely claiming
that the callback can be rolled back. It does not make raw SQL or arbitrary
third-party writers safe and is not enabled by `CB_Auth_Controller`.

## Static verification

The local XAMPP PHP 8.2 linter accepted all of these files:

- `wordpress/carmilla-bridge/includes/class-cb-identity-supported-writer.php`
- `wordpress/carmilla-bridge/includes/class-cb-standalone-boot.php`
- `wordpress/carmilla-bridge/tests/smoke-identity-supported-writer-real-wp.php`

`git diff --check` also passed.

## Runtime verification

On 2026-10-20 the smoke fixture ran against a retained, isolated MariaDB
container with a new random table prefix for every invocation. It passed on:

- WordPress 6.4.3 / PHP 8.1.27
- WordPress 6.6.2 / PHP 8.2.25
- WordPress 7.1.3 / PHP 8.3.35

Each run verified five conditions: a normal successful write after the normal
`user_register` callback; a duplicate rejected before any new user/callback;
a conflict introduced by a callback returning recovery-required instead of a
success; unsupported bytes rejected before a WordPress write; and exact login,
email and mobile lookup resolving only when there is one owner.

The fixture used only randomly prefixed tables in the synthetic database. It
did not modify either retained WordPress installation. The parent remains
`BLOCKED`: this is evidence for a limited supported writer, not proof that raw
SQL, arbitrary third-party writers, migration, deletion recovery or the public
registration route are globally safe.
