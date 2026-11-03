# P02-SEC-CODE-010C2B5D1 — Characterize root alias authentication parity

- Status: READY
- Phase/Area/Type: P02 / Security / QA
- Priority/Risk/Size: P1 / HIGH / M
- Owner: AI
- Depends on: P02-SEC-CODE-010C2B5C
- Blocks: P02-SEC-CODE-010C2B5D2

## Scope and acceptance

In disposable WordPress, run actual HTTP requests against root and canonical REST aliases for `/api/users/me`, `/api/addresses*`, `/api/favorites*`, and `/api/recently-viewed*`. Cover anonymous, cookie without REST nonce, cookie with valid nonce, and bearer authentication for representative read and write verbs. Record status/body parity, user state changes, and any bypass. Use only synthetic accounts/data; clean them up. Characterization may document a defect for D2 without claiming security PASS. No production source edits. Evidence: `docs/evidence/P02-SEC-CODE-010C2B5D1/` with environment, commands, redacted results, cleanup.

## Completion record

- Final status: TODO
