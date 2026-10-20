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

## Runtime status

The real-WordPress smoke fixture is committed but has **not** passed yet.
Docker Desktop's engine did not answer during this run. The XAMPP CLI attempt
stopped before the fixture could create its random-prefix tables because that
CLI process could not connect to the local database; it did not modify either
retained WordPress installation. The parent remains `BLOCKED`; no route,
migration, registration flow or user-facing claim is enabled by this change.
