# P02-QA-CODE-007B — clean WordPress/Woo/PHP CI matrix

- Status: READY
- Phase/Area/Type: P02 / QA / CODE
- Priority/Risk/Size: P0 / HIGH / M
- Owner: AI; completion authority: BOTH for CI evidence
- Depends on: P02-QA-CODE-007A
- Blocks: P02-QA-CODE-007C
- Requirement: parent P02-QA-CODE-007 and 007A fixture contract.

## Goal and scope

Make declared PHP/WP/Woo matrix cells install WordPress, WooCommerce, Theme and Bridge from a clean disposable environment. Run the hermetic and activation smoke subset with fail-on-error and collect redacted reports.

Allowed: `.github/workflows/wordpress-integration.yml`, `wordpress/carmilla-bridge/tests/**`, `wordpress/carmilla-theme/**`, `docs/**` under the owner's 2026-10-31 scope approval. Forbidden: production sites/data, unscoped workflows, dependency upgrades, weak CI gates, destructive operations outside disposable fixtures.

## Acceptance and evidence

- Each declared matrix cell executes clean install and activation with recorded versions and exit codes.
- Hermetic and activation smoke suite runs; a deliberately failing synthetic smoke makes its cell fail.
- Local Docker reproduction where possible and actual CI run URL/revision/matrix logs are recorded; no CI PASS may be inferred from workflow syntax alone.
- Evidence in `docs/evidence/P02-QA-CODE-007B/`; parent remains open until 007C.

Manual QA: if CI/network behavior needs human acceptance, provide exact disposable environment, synthetic data, steps and expected output, then use AWAITING_MANUAL_QA.

## Completion record

- Started at:
- Completed at:
- Changed files:
- Commands and exit codes:
- Manual tester/date/result:
- Evidence paths:
- Remaining risks/blockers:
- Final status: READY
