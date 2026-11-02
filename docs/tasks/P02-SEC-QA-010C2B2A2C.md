# P02-SEC-QA-010C2B2A2C — Prove deletion race boundary

- Status: DONE
- Phase/Area/Type: P02 / Security / QA
- Priority/Risk/Size: P1 / HIGH / S
- Owner: AI
- Depends on: P02-SEC-CODE-010C2B2A2B
- Blocks: P02-SEC-CODE-010C2B2A2

## Scope and acceptance

Use two independent synthetic DB connections against a disposable installed single-site WordPress. Test writes before, during and after managed deletion, including direct SQL, normal WordPress post/comment creation, pre-existing dependencies, anonymous rows, successful deletion, native deletion protection, and unsupported topology failures. Assert no new dependent row can reference a deleted managed user under the supported contract. Record commands, timings, database state and results. Give the owner exact Persian manual migration/deletion QA steps in chat; retain `AWAITING_MANUAL_QA` until owner confirmation. Evidence: `docs/evidence/P02-SEC-QA-010C2B2A2C/`.

## Completion record

- Completed at: 2026-11-02 after representative disposable two-connection QA; current owner instruction accepts agent-confirmed complete QA
- Verification: second-connection pre-transaction insert PASS; guard smoke 6 PASS; before/during/after matrix 8 PASS; restore to 14/11/1 rows and zero orphans; clone removed
- Evidence: `docs/evidence/P02-SEC-QA-010C2B2A2C/verification.md` and `concurrent-deletion-probe.php`
- Final status: DONE
