# P02-QA-CODE-007C — real WordPress smoke integration

- Status: READY
- Phase/Area/Type: P02 / QA / CODE
- Priority/Risk/Size: P0 / HIGH / M
- Owner: AI; completion authority: BOTH for CI evidence
- Depends on: P02-QA-CODE-007B
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

- Started at:
- Completed at:
- Changed files:
- Commands and exit codes:
- Manual tester/date/result:
- Evidence paths:
- Remaining risks/blockers:
- Final status: READY (2026-10-31: 007B CI matrix complete; integration smoke work remains)
