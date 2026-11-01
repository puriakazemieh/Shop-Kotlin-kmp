# P02-SEC-CODE-010C2B3A2B — Repair booking partial writes

- Status: TODO
- Phase/Area/Type: P02 / Security / CODE
- Priority/Risk/Size: P1 / HIGH / M
- Owner: AI
- Depends on: P02-SEC-REVIEW-010C2B3A2A
- Blocks: P02-CI-CODE-010C2C

## Scope and acceptance

Use the characterized failure matrix to ensure a failed appointment post/meta/lock update cannot leave a falsely successful response or unusable orphan slot. Preserve the unique slot constraint, user ownership and successful booking DTO. If a safe correction exceeds M, decompose before coding. Allowed: clinic controller, focused tests, `docs/**`. Installed synthetic QA and owner confirmation required. Evidence: `docs/evidence/P02-SEC-CODE-010C2B3A2B/`.

## Completion record

- Final status: TODO
