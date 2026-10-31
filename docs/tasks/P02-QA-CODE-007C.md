# P02-QA-CODE-007C — real WordPress smoke integration

- Status: DONE
- Phase/Area/Type: P02 / QA / CODE
- Priority/Risk/Size: P0 / HIGH / M
- Owner: AI; completion authority: BOTH for CI evidence
- Depends on: P02-QA-CODE-007C6 (ordered children C1–C6; original 007B dependency moves to C1)
- Blocks: parent P02-QA-CODE-007
- Requirement: parent P02-QA-CODE-007 and 007A/007B fixture and matrix evidence.

## Goal and scope

Run real-WordPress smoke tests in isolated disposable databases with per-test random prefixes, correct WordPress path and explicit opt-in flags. Preserve failure exit codes and cleanup without touching any customer site.

Allowed: `.github/workflows/wordpress-integration.yml`, `wordpress/carmilla-bridge/tests/**`, `docs/**` under the owner's 2026-10-31 scope approval. Forbidden: production sites/data, unscoped workflows, dependency upgrades, bypassing test opt-in flags or relaxing negative security assertions.

## Acceptance and evidence

- The real-WP smoke subset executes on the declared supported matrix, or a reduced matrix with an explicit compatibility rationale.
- Clean install and activation remain green; a negative synthetic fixture fails CI.
- Redacted local and actual CI evidence records PHP/WP/Woo versions, revision, commands, exit codes and cleanup; no unexecuted check is marked passed.
- Evidence in `docs/evidence/P02-QA-CODE-007C/`; parent may be reconsidered only after original criteria pass.

Manual QA: for network/migration effects, provide exact disposable environment, synthetic data, steps, and expected results; hold AWAITING_MANUAL_QA until human confirmation.

## Completion record

- Started at: 2026-10-31
- Completed at: 2026-10-31
- Changed files: CI workflow and test fixture through children C1–C6; this card, `docs/tasks.md`, and parent evidence.
- Commands and exit codes: CI runs 41194859046 (6/6 jobs), 41196005502 (10/10), 41197637470 (16/16), 41198134020 (20/20), 41198628730 (23/23), 41199892940 (26/26), and 41215947307 (27/27) all concluded success. Child checkpoints record guard-negative exit 2 and PHP lint exit 0. Synthetic negative gate run 41190362802 intentionally exited 17; normal gate run 41190660399 passed.
- Manual tester/date/result: Not required for CI fixture/orchestration changes; no product network or migration implementation changed.
- Evidence paths: `docs/evidence/P02-QA-CODE-007C/verification-20261031.md`, child checkpoints C1–C6 and linked CI runs.
- Remaining risks/blockers: Version-specific real-WP smokes run on one pinned compatibility cell, not all six matrix combinations; production migration acceptance is outside this card.
- Final status: DONE (2026-10-31)
