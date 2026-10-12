# P07 Migration recovery

- Date: 2026-10-12
- Scope: `wordpress/carmilla-bridge/includes/Migration/` and Phase 07 smoke tests
- Environment: disposable Docker WordPress + MariaDB test stack
- Data: synthetic posts and metadata only; the test deletes its created posts and mappings.

## Verification

1. Docker PHP 8.1 lint for all six Migration files and `smoke-p07-seed-migration.php`: exit code 0.
2. `smoke-p07-seed-migration.php`: exit code 0. It verified the post/meta denylist, AES-256-GCM encryption, site binding, wrong-key rejection, wrong-site rejection, and expiry rejection.
3. Full WordPress lint: 241 PHP files checked with PHP 8.1; 0 failures.
4. Disposable WordPress/MariaDB smoke: exit code 0. The Bridge plugin was activated, then `smoke-p07-wordpress.php` verified first-pass import, source mapping, second-pass parent resolution, URL rewriting, allowed metadata import, blocked metadata exclusion, and unchanged-record skipping on repeat import.

## Manual QA status

No customer or staging data was migrated. `P07-QA-MANUAL-024` and the Phase 07 human gate remain pending; a backup and human review are still required before any real migration.
