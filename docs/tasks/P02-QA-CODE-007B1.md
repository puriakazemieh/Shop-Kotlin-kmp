# P02-QA-CODE-007B1 — PHP 7.4 compatibility of migration metadata guard

- Status: READY
- Phase/Area/Type: P02 / QA / CODE
- Priority/Risk/Size: P0 / HIGH / S
- Owner: AI; completion authority: AI
- Depends on: P02-QA-CODE-007A
- Blocks: P02-QA-CODE-007B
- Requirement: 007B matrix's declared PHP 7.4 cell and RTK PHP 7.4 minimum.

## Goal and scope

Remove the PHP 8-only `str_contains` call from `Migration_Config::is_denied_meta_key` without weakening the denylist or changing migration behavior. This child is authorized by the owner's 2026-10-31 instruction to split tasks as needed and finish Phase 02. It is limited to the compatibility defect discovered by executing the 007B smoke subset.

Allowed: `wordpress/carmilla-bridge/includes/Migration/Migration_Config.php`, `wordpress/carmilla-bridge/tests/smoke-p07-seed-migration.php`, `docs/**`. Forbidden: migration schema/data changes, production sites, unrelated PHP modernization, dependency upgrades, relaxing denied-key rules.

## Acceptance and evidence

- Characterize the failing PHP 7.4 test before the fix and retain its output/exit.
- Preserve exact true/false behavior for existing denied keys, mixed-case token/password/secret/api_key keys, and an allowed public key.
- Run PHP 7.4, 8.1 and 8.2 lint and the focused smoke test; record exact exit codes and redacted output in `docs/evidence/P02-QA-CODE-007B1/`.
- No network, migration or UI behavior changes; manual QA N/A. If a broader incompatibility appears, stop and reassess size/scope.

## Completion record

- Started at:
- Completed at:
- Changed files:
- Commands and exit codes:
- Manual tester/date/result:
- Evidence paths:
- Remaining risks/blockers:
- Final status: READY
