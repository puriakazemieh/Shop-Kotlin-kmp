# P02-SEC-CODE-010C2B2A2B2E — Preserve native deletion with comment integrity

- Status: DONE
- Phase/Area/Type: P02 / Security / CODE
- Priority/Risk/Size: P1 / HIGH / M
- Owner: AI
- Depends on: P02-SEC-CODE-010C2B2A2B3
- Blocks: P02-SEC-QA-010C2B2A2B2C

## Scope and acceptance

Implement only the selected bounded design from the review. Native `wp_delete_user` of a clean user and of a user with reassigned post/comment references must succeed without orphaning nonzero references. Managed-account native deletion must remain denied. Direct SQL insert/update and concurrent managed deletion must remain protected by database-enforced integrity even if a WordPress hook does not run. Exercise both healthy and injected-failure paths in a disposable installed WordPress clone. Decompose before coding if implementation exceeds M. Evidence: `docs/evidence/P02-SEC-CODE-010C2B2A2B2E/`.

## Completion record

- Completed at: 2026-11-02
- Changed files: integrity verifier and explicit migration, WordPress comment-cache adapter, standalone boot, focused smoke tests, evidence and queue
- Verification: disposable migrated WordPress clone native probe 11 PASS/0 FAIL; verifier 7 PASS, migration 7 PASS, operator 8 PASS; managed-deletion race probe PASS; PHP lint passed
- Evidence: `docs/evidence/P02-SEC-CODE-010C2B2A2B2E/verification.md`
- Final status: DONE
