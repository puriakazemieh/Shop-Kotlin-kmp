# Backup and recovery for the session-migration fixture

This is a conservative procedure, not authority to modify either retained WordPress site. MariaDB DDL may commit each table independently. On a partial schema, unexpected version, lost connection or ambiguous result, stop the migration and keep the application feature disabled. Do not drop tables, rerun blindly, edit the marker, assume a rollback happened, or overwrite an existing database.

## Synthetic exercise performed

The source database was `carmilla_adapter_qa` inside `carmilla-p00-session-migration-fixture-20261018`. A logical dump made with `mariadb-dump --single-transaction --routines --triggers` was saved as `synthetic-before-restore.sql` in the separate Docker volume `carmilla-p00-session-migration-backup-20261018`. It was restored to the newly created, previously absent `carmilla_adapter_restore_qa` database, not over the source. Counts and selected fields were compared and the restored WordPress/session were actually bootstrapped; see [verification](verification.md). The fixture contains synthetic data only. Docker volume and backup must be retained for this review.

## Required before any future change to a retained QA site

1. Identify the exact site, database, table prefix and uploads/configuration; obtain explicit scope to change that site and verify no other instance shares the target. Record artifact versions and a rollback owner. This task did not grant deployment permission.
2. Quiesce all writes, including scheduled jobs and external workers, and take a consistent complete database backup plus matching WordPress files, uploads and configuration. A database-only `--single-transaction` dump is not automatically consistent with files, nontransactional tables or active external writers. Use an approved backup tool and least-privilege credentials; do not place credentials or backups in the repository or command history.
3. Protect the backup as sensitive data using restricted access, encryption and the applicable retention policy. Verify its integrity and restore it to a **new isolated** database and separate WordPress directory. Confirm login, representative content/uploads, version and application behavior there. Keep the original and restored copy separate; do not overwrite the retained site as a test.
4. On a migration error, leave the original data and partial schema untouched, preserve redacted diagnostics and investigate from the verified clone. Any repair or restoring the real site is a separately authorized operator action with a maintenance window and a verified restore point. External hooks, sent messages or other irreversible side effects are not undone by database restoration.

Production backup and restore have not been performed or verified by this synthetic exercise.
