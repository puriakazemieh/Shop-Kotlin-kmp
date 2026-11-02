# P02-SEC-QA-010C2B2A2B2C — Verify integrity migration recovery

- Status: DONE
- Phase/Area/Type: P02 / Security / QA
- Priority/Risk/Size: P1 / HIGH / S
- Owner: AI
- Depends on: P02-SEC-CODE-010C2B2A2B2E
- Blocks: P02-SEC-CODE-010C2B2A2B2

## Scope and acceptance

On a disposable WordPress database copy, prove fresh install, upgrade with valid legacy rows, orphan preflight refusal, interrupted migration, exact constraint verification, native WordPress writes and deletion/reassignment, and backup restore/rollback with zero unintended orphan creation. Record SQL state, versions, cleanup and exact Persian QA instructions in chat if a human-only step remains. Do not claim production migration or phase gate PASS. Evidence: `docs/evidence/P02-SEC-QA-010C2B2A2B2C/`.

## Completion record

- Failed acceptance at: 2026-11-02
- Evidence: `docs/evidence/P02-SEC-QA-010C2B2A2B2C/native-compatibility-failure.md` and `native-wp-probe.php`
- Blocker: after installing the comment FK on a disposable WordPress database clone, native `wp_delete_user($owner, $reassign)` reassigns the post but leaves the comment `user_id`; the FK rejects deletion. The clone was restored and removed.
- Remediation: ordered review `P02-SEC-REVIEW-010C2B2A2B2D` then code `P02-SEC-CODE-010C2B2A2B2E`; rerun this full QA afterward.
- Reverified at: 2026-11-02 after native-compatibility remediation
- Verification: fresh clone install and preflight; native probe 11 PASS; scratch migration 7 PASS, verifier 7 PASS, operator 8 PASS; second-connection race PASS; full dump restore, row counts and zero-orphan checks; disposable clone cleaned
- Evidence: `docs/evidence/P02-SEC-QA-010C2B2A2B2C/verification.md`
- Final status: DONE
