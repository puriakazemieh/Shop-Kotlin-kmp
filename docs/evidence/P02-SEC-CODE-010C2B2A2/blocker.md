# P02-SEC-CODE-010C2B2A2 — integrity boundary review

Reviewed 2026-11-01. No product code or database was changed.

`CB_Managed_User_Deletion::delete()` checks `wp_posts.post_author` and `wp_comments.user_id` before `START TRANSACTION`, then deletes user meta and the user. Two independent connections can insert a dependent row after the check. Moving the check into the transaction or locking the user row alone does not force ordinary WordPress writes to check that row. A table lock can serialize inserts during the delete, but an independent writer can insert a row referencing the deleted numeric user ID immediately after the lock releases. Without a referential constraint or control over every writer, the acceptance claim that this path cannot leave dependent content orphaned is not provable.

The task allows only the managed deletion class, focused tests and docs. Adding database constraints/triggers, changing every post/comment writer, or introducing a migration and rollback crosses that scope. The task explicitly requires stopping when an M-sized solution cannot prove the invariant. The existing managed deletion route must not be declared safe against concurrent writes. Native deletion protection and the existing healthy deletion test were inspected, not re-run or changed in this review.

## Ordered work needed after a design decision

1. **S — Integrity contract:** Define supported WordPress topology, storage engines, all dependent tables and existing orphan handling. Choose a database-enforced invariant or document the bounded cooperative-writer guarantee. Acceptance: executable two-connection counterexample and an approved invariant; evidence includes schema and compatibility review.
2. **M — Enforcement:** With an authorized scope and migration plan, implement the chosen invariant and fail-closed deletion behavior. Acceptance: native deletion protection and ordinary successful deletion still work; evidence includes schema upgrade and rollback checks.
3. **S — Concurrency proof and QA:** Exercise two independent synthetic database connections with both interleavings and direct SQL writes; record exact steps and results. Keep `AWAITING_MANUAL_QA` until owner confirms installed synthetic deletion QA.

No subtask is READY until the integrity contract and scope are approved. The current card is BLOCKED rather than DONE or AWAITING_MANUAL_QA.
