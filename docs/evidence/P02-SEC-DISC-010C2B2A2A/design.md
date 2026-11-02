# P02-SEC-DISC-010C2B2A2A — managed deletion integrity contract

Reviewed 2026-11-02. This card changed no product schema or code. All SQL experiments used disposable scratch tables in the installed WordPress test database and dropped them afterward.

## Observed fixture and current failure

Disposable `test-env-wordpress-test-1` / `test-env-db-1`: WordPress 7.1.3, PHP 8.3.35, MariaDB 10.6.28, single-site. `wp_users`, `wp_usermeta`, `wp_posts` and `wp_comments` are InnoDB. Their reported approximate row counts were 13, 211, 11 and 1. No foreign keys on these four tables and no triggers on users/posts/comments were found. Read-only orphan counts for nonzero `post_author` and `comment.user_id` were both zero. These observations describe this fixture, not another installation.

`CB_Managed_User_Deletion::delete()` checks posts/comments before `START TRANSACTION`, removes usermeta, then deletes the user. The installed WordPress schema permits post/comment user IDs without a matching user. WordPress insertion paths and direct SQL are independent writers; a PHP hook or a lock taken only by the managed deletion function cannot enforce a lasting invariant.

Run `race-probe.php` in the disposable WordPress container with `CARMILLA_LOGIN_DISPOSABLE=yes`. It creates separate InnoDB scratch user/post tables and two independent `mysqli` connections, then drops the tables. Observed: `connections=17,18 before=0 orphan_after_interleaving=1 post_delete_insert=accepted total_orphans=2`, exit 0; a follow-up table-name check reported zero scratch tables. The probe demonstrates the missing database constraint; it does not delete a real WordPress user.

## Supported invariant and candidate mechanism

For supported single-site InnoDB installations, every **nonzero** `wp_posts.post_author` and `wp_comments.user_id` must reference an existing `wp_users.ID` at commit. Zero remains valid for the WordPress anonymous/system cases. Use a nullable stored/generated column `NULLIF(post_author,0)` and `NULLIF(user_id,0)` respectively, indexed with a `RESTRICT` foreign key to `wp_users.ID`. The foreign keys make both insert/update and user delete obey the same InnoDB row-lock boundary, including direct SQL and other plugins. On MariaDB 10.6 scratch tables, a `PERSISTENT` generated nullable column plus `RESTRICT` FK was created successfully: author 0 inserted, nonzero missing author failed with a FK error. No WordPress table was altered. MySQL 8 and other engines/versions remain **unverified**, so the implementation must capability-test its exact DDL and reject unsupported topology; never claim broad MySQL support from this probe.

This invariant intentionally covers all users, not just managed users, because a dependent row cannot carry a reliable managed marker after usermeta deletion. It can change behavior for existing non-Carmilla imports and plugins that write orphan references. That compatibility effect requires an explicit preflight and a safe failure path. WordPress native deletion/reassignment must be tested before enabling it.

## Implementation boundary for the next M card

1. Limit initial support to a verified single-site InnoDB schema and database/version with successful generated-column/FK capability probe. Check prefix, column types, existing constraints/triggers, engine, privileges, and multisite before migration; reject unknown topology. Inventory existing nonzero orphan references without exposing row content. If any exist, stop; no automatic deletion or reassignment.
2. Back up the database before any real migration. On a disposable copy first, add the nullable generated columns, indexes and `RESTRICT` FKs to posts and comments with deterministic names. Use an explicit, idempotent migration routine rather than assuming `dbDelta` manages foreign keys. Re-read `information_schema` to prove both constraints installed. Partial DDL failure must be reported and repaired or rolled back before managed deletion is allowed.
3. In the managed deletion class, fail closed unless both verified constraints and the supported topology are present. Keep the existing dependency checks, native capability protection, transactional usermeta/user deletion, rollback and post-commit cache/event behavior. FK rejection must be an unavailable/dependencies result, never success. No code path should quietly fall back to the unprotected deletion.
4. Migration rollback is a maintenance operation: stop writes, restore a verified backup or remove the exact constraints/columns in reverse order only after confirming no dependent rows can become orphaned. Test fresh install, upgrade, orphan preflight failure, partial DDL failure, native delete/reassign, anonymous/zero IDs, and rollback on disposable databases. Do not deploy to a retained or production database under this task.

Candidate files: a narrowly scoped Bridge integrity-migration class/bootstrap hook, the existing managed deletion class, focused tests and `docs/**`. If topology/host integration expands beyond M, split before coding. Theme-only support belongs to the later shared-core migration program unless separately authorized; do not claim it from this Bridge-only proof.

## Required proof in the QA card

With two genuinely independent DB connections, cover insert-before-delete, delete-before-insert, concurrent insert and delete, update of the author/user ID, direct SQL, ordinary WordPress API writes, dependencies already present, zeros, and native deletion/reassignment. Query for nonzero orphans after every interleaving. Success means zero new orphans, an expected FK/fail-closed rejection for unsafe writes or deletion, and ordinary clean deletion still succeeds. Verify constraint presence before and after upgrade and rollback. Record versions, SQL state, exit codes and cleanup. Any unsupported topology returns a clear failure without deleting the managed user.

No owner action is needed for this read-only design. The next card must prove the mechanism on the actual supported engine before declaring it safe.
