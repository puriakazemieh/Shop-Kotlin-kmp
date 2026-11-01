# P02-SEC-CODE-010C1A — Remove fixed fallback key for messaging secrets

- Status: DONE
- Phase/Area/Type: P02 / SECURITY / CODE
- Priority/Risk/Size: P1 / HIGH / M
- Owner: AI; human confirmation required for the live messaging setting path
- Depends on: P02-CI-CODE-010C1
- Blocks: P02-CI-CODE-010C2
- Authorization: owner explicitly expanded product-security scope on 2026-11-01.

## Scope and acceptance

Remove the public fixed encryption key from the shared message settings implementation. When neither site key exists, new credentials must fail closed without storing a plaintext or predictably encrypted secret; provider use must fail closed. Document treatment of any existing ciphertext made with the old fallback and a safe admin rotation path. Preserve normal site-key behavior and test both keyed and keyless paths with synthetic credentials. Allowed: `wordpress/packages/carmilla-core/inc/Settings/MessageSettings.php`, `wordpress/packages/carmilla-core/inc/Messaging/MessageSender.php`, `wordpress/carmilla-bridge/includes/class-cb-sms-http-adapter.php`, `wordpress/carmilla-bridge/includes/class-cb-mail-adapter.php`, directly affected messaging smoke tests, `docs/tasks.md`, this card and `docs/evidence/P02-SEC-CODE-010C1A/`. No live credentials or production data. Run PHP syntax, focused smoke, and relevant package checks. Manual QA: disposable WordPress Theme-only and Bridge-only settings, empty-key and normal-key scenarios, and rotation of a synthetic legacy credential; keep AWAITING_MANUAL_QA until owner confirms.

## Completion record

- Started at: 2026-11-01
- Completed at: 2026-11-01, after owner accepted the installed synthetic QA evidence in this chat.
- Changed files: shared MessageSettings and MessageSender, Bridge SMS/SMTP adapters, focused smoke tests, this card, queue and evidence.
- Commands and exit codes: Docker PHP 8.1 keyless/keyed synthetic smoke, core/Bridge messaging smoke, PHP syntax (7 files), and local QA ZIP construction all exit 0; `git diff --check` exit 0.
- Manual tester/date/result: Codex ran four real disposable WordPress installs and authenticated admin form checks on 2026-11-01; all final probes passed. Owner explicitly accepted this QA evidence in chat on 2026-11-01.
- Evidence paths: `docs/evidence/P02-SEC-CODE-010C1A/verification.md`; ignored QA ZIPs under `.artifacts/p02-security-010C1A/`.
- Remaining risks/blockers: external SMS/SMTP provider delivery was not tested and is outside this synthetic acceptance; no claim of provider production readiness.
- Final status: DONE
