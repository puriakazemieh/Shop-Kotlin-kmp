# P02-SEC-CODE-010C2B1A — Redact public OTP provider errors

- Status: DONE
- Phase/Area/Type: P02 / Security / CODE
- Priority/Risk/Size: P1 / HIGH / S
- Owner: AI
- Depends on: P02-CI-CODE-010C2B1
- Blocks: P02-CI-CODE-010C2C

## Scope and acceptance

The public OTP endpoints currently return filter or SMS provider error text through `cb_error`. Replace these with stable generic public errors without disclosing provider endpoint, credentials, or account details. Preserve safe operational diagnostics without secrets or personal data; keep retry and cooldown behavior. Cover override and direct adapter failure with synthetic automated tests. Allowed: `wordpress/carmilla-bridge/includes/class-cb-auth-controller.php`, focused auth tests, `docs/**`. For the changed network response, record exact manual QA environment, steps and expected output, and retain `AWAITING_MANUAL_QA` until owner confirmation. Evidence: `docs/evidence/P02-SEC-CODE-010C2B1A/`.

## Completion record

- Started at: 2026-11-01
- Changed files: `wordpress/carmilla-bridge/includes/class-cb-auth-controller.php`, focused installed-WP test, this card, queue, evidence
- Commands and exit codes: PHP 8.1 syntax checks 0/0; focused installed-WP smoke 0 (7 PASS); auth error contract smoke 0 (4 PASS); QA ZIP built and hashed
- Evidence paths: `docs/evidence/P02-SEC-CODE-010C2B1A/qa-20261101.md`
- Manual tester/date/result: agent installed synthetic check 2026-11-01 PASS; owner confirmed the evidence in chat on 2026-11-01
- Completed at: 2026-11-01
- Remaining risks/blockers: none for this card
- Final status: DONE
