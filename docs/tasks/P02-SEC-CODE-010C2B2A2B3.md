# P02-SEC-CODE-010C2B2A2B3 — Guard managed deletion with integrity proof

- Status: TODO
- Phase/Area/Type: P02 / Security / CODE
- Priority/Risk/Size: P1 / HIGH / S
- Owner: AI
- Depends on: P02-SEC-CODE-010C2B2A2B2
- Blocks: P02-SEC-CODE-010C2B2A2B; P02-SEC-QA-010C2B2A2C

## Scope and acceptance

Before any managed deletion, require the verifier to prove both installed constraints and supported topology; otherwise return unavailable without writes. Preserve native capability block, dependency refusal, transaction rollback, ordinary clean deletion, cache/event behavior. Test missing/partial/failed verification and valid constraints on disposable installed WordPress. Allowed: managed deletion class, integrity verifier, focused tests and `docs/**`. Evidence: `docs/evidence/P02-SEC-CODE-010C2B2A2B3/`.

## Completion record

- Final status: TODO
