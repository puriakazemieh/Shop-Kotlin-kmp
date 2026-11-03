# P02-SEC-CODE-010C2B5D — Verify and harden root REST alias dispatch

- Status: TODO
- Phase/Area/Type: P02 / Security / CODE
- Priority/Risk/Size: P1 / HIGH / M
- Owner: AI
- Depends on: P02-SEC-CODE-010C2B5C
- Blocks: P02-CI-CODE-010C2B6

## Scope and acceptance

Characterize root `/api/users/me`, `/api/addresses*`, `/api/favorites*`, `/api/recently-viewed*` aliases with actual HTTP cookie/no nonce, valid REST nonce, bearer token and malformed query/header values for read and write verbs. Preserve native REST permission and validation semantics; prevent any alias bypass or unsafe raw query forwarding, and avoid logging private path data. Verify canonical-route parity and tenant/auth isolation in disposable WordPress; if scope exceeds M, decompose before code. Allowed: plugin alias dispatch, focused tests, `docs/**`. Evidence: `docs/evidence/P02-SEC-CODE-010C2B5D/`.

## Completion record

- Final status: TODO
