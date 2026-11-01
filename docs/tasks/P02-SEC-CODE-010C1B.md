# P02-SEC-CODE-010C1B — Keep migration export plaintext off disk

- Status: AWAITING_MANUAL_QA
- Phase/Area/Type: P02 / SECURITY / CODE
- Priority/Risk/Size: P1 / HIGH / M
- Owner: AI; human confirmation required for migration export
- Depends on: P02-CI-CODE-010C1
- Blocks: P02-CI-CODE-010C2
- Authorization: owner explicitly expanded product-security scope on 2026-11-01.

## Scope and acceptance

Ensure the legacy export command never writes unencrypted posts/media payloads to the output directory and never reports success after a failed encrypted write. Test synthetic records and a forced write failure, asserting that no `posts.ndjson` or `media.ndjson` remains. Preserve encrypted export format. Allowed: `wordpress/carmilla-bridge/includes/Migration/Export_Command.php`, its focused synthetic test, `docs/tasks.md`, this card and `docs/evidence/P02-SEC-CODE-010C1B/`. No production data. Run PHP syntax, focused migration smoke and relevant package checks. Manual QA: disposable WordPress site with synthetic content, encrypted export, decryption/import check, failure cleanup, expected absence of plaintext files; keep AWAITING_MANUAL_QA until owner confirms.

## Completion record

- Started at: 2026-11-01
- Completed at: pending human QA
- Changed files: `Export_Command.php`, focused synthetic smoke, queue, this card, evidence.
- Commands and exit codes: Docker PHP 8.1 and 7.4 export/failure smoke, existing P07 migration smoke, PHP 8.1 and 7.4 production syntax, local QA ZIP construction and `git diff --check` all exit 0.
- Manual tester/date/result: Codex ran real disposable WordPress source/target export, failure, legacy-file and repeated-import checks on 2026-11-01; all final probes passed. Owner acceptance remains pending.
- Evidence paths: `docs/evidence/P02-SEC-CODE-010C1B/verification.md`; ignored QA ZIP in `.artifacts/p02-security-010C1B/`.
- Remaining risks/blockers: owner acceptance remains pending; the real installed export/import and filesystem failure probes passed. C2 remains BLOCKED until 010C1A/B are confirmed.
- Final status: AWAITING_MANUAL_QA
