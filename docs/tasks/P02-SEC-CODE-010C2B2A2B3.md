# P02-SEC-CODE-010C2B2A2B3 — Guard managed deletion with integrity proof

- Status: DONE
- Phase/Area/Type: P02 / Security / CODE
- Priority/Risk/Size: P1 / HIGH / S
- Owner: AI
- Depends on: P02-SEC-CODE-010C2B2A2B2B
- Blocks: P02-SEC-CODE-010C2B2A2B2E; P02-SEC-CODE-010C2B2A2B; P02-SEC-QA-010C2B2A2C

## Scope and acceptance

Before any managed deletion, require the verifier to prove both installed constraints and supported topology; otherwise return unavailable without writes. Preserve native capability block, dependency refusal, transaction rollback, ordinary clean deletion, cache/event behavior. Test missing/partial/failed verification and valid constraints on disposable installed WordPress. Allowed: managed deletion class, integrity verifier, focused tests and `docs/**`. Evidence: `docs/evidence/P02-SEC-CODE-010C2B2A2B3/`.

The two-connection probe showed that a comment inserted after the old pre-transaction check is anonymized by the trigger while managed deletion incorrectly returns success. Acquire the user-row lock and repeat dependency checks inside the transaction before deleting metadata. A competing insert must be refused or make deletion return `dependencies_exist`, never silently cause account deletion.

## Completion record

- Completed at: 2026-11-02
- Changed files: managed deletion transaction boundary, focused disposable WordPress tests, this card, queue and evidence
- Verification: migrated clone guard smoke 6 PASS/0 FAIL; original uninstalled fixture refusal PASS; prior read-failure regression 7 PASS/0 FAIL; two independent DB connection race PASS; PHP lint passed
- Evidence: `docs/evidence/P02-SEC-CODE-010C2B2A2B3/verification.md`
- Final status: DONE
