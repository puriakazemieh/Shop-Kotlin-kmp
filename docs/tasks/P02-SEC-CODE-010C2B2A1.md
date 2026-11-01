# P02-SEC-CODE-010C2B2A1 — Fail closed on managed deletion reads

- Status: DONE
- Phase/Area/Type: P02 / Security / CODE
- Priority/Risk/Size: P1 / HIGH / S
- Owner: AI
- Depends on: P02-CI-CODE-010C2B2
- Blocks: P02-SEC-CODE-010C2B2A2

## Scope and acceptance

`CB_Managed_User_Deletion` currently casts failed dependency/count reads to zero. Distinguish a valid zero from SQL error/null and return `unavailable` without deleting or reporting success. Test both dependency read failures and post-delete verification failure with isolated synthetic WordPress/DB faults; retain transaction rollback and cache behavior. Do not redesign concurrent deletion in this card. Allowed: `wordpress/carmilla-bridge/includes/class-cb-managed-user-deletion.php`, focused tests, `docs/**`. For the changed deletion flow, record exact installed synthetic QA and retain `AWAITING_MANUAL_QA` until owner confirmation. Evidence: `docs/evidence/P02-SEC-CODE-010C2B2A1/`.

## Completion record

- Started at: 2026-11-01
- Changed files: managed deletion class, focused installed-WP test, this card, queue and evidence
- Commands and exit codes: PHP 8.1 syntax checks 0/0; installed-WP focused smoke 0 (7 PASS); QA ZIP built and hashed
- Evidence paths: `docs/evidence/P02-SEC-CODE-010C2B2A1/qa-20261101.md`
- Manual tester/date/result: agent installed synthetic check 2026-11-01 PASS; owner confirmed evidence in chat on 2026-11-01
- Completed at: 2026-11-01
- Remaining risks/blockers: concurrent dependency race is separately queued
- Final status: DONE
