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
