# P02-SEC-CODE-010C1B — Keep migration export plaintext off disk

- Status: TODO
- Phase/Area/Type: P02 / SECURITY / CODE
- Priority/Risk/Size: P1 / HIGH / M
- Owner: AI; human confirmation required for migration export
- Depends on: P02-CI-CODE-010C1
- Blocks: P02-CI-CODE-010C2
- Authorization: owner explicitly expanded product-security scope on 2026-11-01.

## Scope and acceptance

Ensure the legacy export command never writes unencrypted posts/media payloads to the output directory and never reports success after a failed encrypted write. Test synthetic records and a forced write failure, asserting that no `posts.ndjson` or `media.ndjson` remains. Preserve encrypted export format. Allowed: `wordpress/carmilla-bridge/includes/Migration/Export_Command.php`, its focused synthetic test, `docs/tasks.md`, this card and `docs/evidence/P02-SEC-CODE-010C1B/`. No production data. Run PHP syntax, focused migration smoke and relevant package checks. Manual QA: disposable WordPress site with synthetic content, encrypted export, decryption/import check, failure cleanup, expected absence of plaintext files; keep AWAITING_MANUAL_QA until owner confirms.

## Completion record

- Started at:
- Completed at:
- Changed files:
- Commands and exit codes:
- Manual tester/date/result:
- Evidence paths:
- Remaining risks/blockers:
- Final status: TODO
