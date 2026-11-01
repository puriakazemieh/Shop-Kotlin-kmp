# P02-SEC-CODE-010C1A — Remove fixed fallback key for messaging secrets

- Status: READY
- Phase/Area/Type: P02 / SECURITY / CODE
- Priority/Risk/Size: P1 / HIGH / M
- Owner: AI; human confirmation required for the live messaging setting path
- Depends on: P02-CI-CODE-010C1
- Blocks: P02-CI-CODE-010C2
- Authorization: owner explicitly expanded product-security scope on 2026-11-01.

## Scope and acceptance

Remove the public fixed encryption key from the shared message settings implementation. When neither site key exists, new credentials must fail closed without storing a plaintext or predictably encrypted secret; provider use must fail closed. Document treatment of any existing ciphertext made with the old fallback and a safe admin rotation path. Preserve normal site-key behavior and test both keyed and keyless paths with synthetic credentials. Allowed: `wordpress/packages/carmilla-core/inc/Settings/MessageSettings.php`, directly affected messaging smoke tests, `docs/tasks.md`, this card and `docs/evidence/P02-SEC-CODE-010C1A/`. No live credentials or production data. Run PHP syntax, focused smoke, and relevant package checks. Manual QA: disposable WordPress Theme-only and Bridge-only settings, empty-key and normal-key scenarios, and rotation of a synthetic legacy credential; keep AWAITING_MANUAL_QA until owner confirms.

## Completion record

- Started at:
- Completed at:
- Changed files:
- Commands and exit codes:
- Manual tester/date/result:
- Evidence paths:
- Remaining risks/blockers:
- Final status: READY
