# P02-SEC-QA-010C2B2A2B2C — Verify integrity migration recovery

- Status: BLOCKED
- Phase/Area/Type: P02 / Security / QA
- Priority/Risk/Size: P1 / HIGH / S
- Owner: AI
- Depends on: P02-SEC-CODE-010C2B2A2B2E
- Blocks: P02-SEC-CODE-010C2B2A2B2; P02-SEC-CODE-010C2B2A2B3

## Scope and acceptance

On a disposable WordPress database copy, prove fresh install, upgrade with valid legacy rows, orphan preflight refusal, interrupted migration, exact constraint verification, native WordPress writes and deletion/reassignment, and backup restore/rollback with zero unintended orphan creation. Record SQL state, versions, cleanup and exact Persian QA instructions in chat if a human-only step remains. Do not claim production migration or phase gate PASS. Evidence: `docs/evidence/P02-SEC-QA-010C2B2A2B2C/`.

## Completion record

- Failed acceptance at: 2026-11-02
- Evidence: `docs/evidence/P02-SEC-QA-010C2B2A2B2C/native-compatibility-failure.md` and `native-wp-probe.php`
- Blocker: after installing the comment FK on a disposable WordPress database clone, native `wp_delete_user($owner, $reassign)` reassigns the post but leaves the comment `user_id`; the FK rejects deletion. The clone was restored and removed.
- Remediation: ordered review `P02-SEC-REVIEW-010C2B2A2B2D` then code `P02-SEC-CODE-010C2B2A2B2E`; rerun this full QA afterward.
- Final status: BLOCKED
