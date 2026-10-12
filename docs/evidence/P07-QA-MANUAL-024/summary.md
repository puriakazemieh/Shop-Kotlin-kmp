# Evidence for P07-QA-MANUAL-024

## Baseline
- git status: Clean before changes.

## Changes
- Task is designated for HUMAN execution.
- Handoff instructions have been prepared for the user to perform a complete staging migration, delta migration, and rollback (UAT and backup window validation).
- Status set to AWAITING_MANUAL_QA pending user validation.

## Reviewer
- Reviewer: AI Agent (Handoff)
- Result: AWAITING_MANUAL_QA

## Local WordPress UI confirmation — 2026-10-12
- Tester: product owner
- Environment: isolated local WordPress at `http://localhost/carmilla-p07-qa/` using XAMPP; Carmilla Bridge active.
- Build fingerprint: commits `2e751388` and `b5755439`.
- Result: login succeeded; a synthetic post was created and deleted; the Bridge plugin was confirmed active; no browser error was reported.
- Scope note: this confirms the local browser smoke test only. It does **not** complete the required staging migration, delta migration, backup/restore, rollback, or acceptance-window validation.

## Isolated migration rehearsal — 2026-10-12
- Environment: two persistent local XAMPP WordPress installations with separate databases: source `http://localhost/carmilla-p07-qa/` and destination `http://localhost/carmilla-p07-destination/`.
- Data: synthetic posts and synthetic metadata only; no customer, payment, health, or production data was used.
- Backup: SQL backups were taken before import and retained outside the repository in `D:\Carmilla-QA\migration-rehearsal\backups`.
- Full import: encrypted package imported 9 records. Parent/child relation, URL rewriting, allowed metadata, and exclusion of a blocked billing-email field were verified.
- Delta: an existing synthetic post was updated and one synthetic post added. Overlay import reported 1 inserted and 9 updated; repeating the same package reported 10 unchanged.
- Rollback: the destination database was restored from its pre-import backup; the migration mappings for this test source were removed because the table was created after the backup. Post-import rehearsal records and mappings both verified at count zero; destination WordPress returned HTTP 200.
- Finding fixed: the overlay export relied on a timestamp absent from the historical seed table. Schema version 1.0.2 now adds `created_at`, and overlay export fails explicitly if its query cannot run.
- Result: the automated local rehearsal passed. The task remains `AWAITING_MANUAL_QA` because production-like staging UAT, a human acceptance window, and explicit human sign-off are still required by the task card.
