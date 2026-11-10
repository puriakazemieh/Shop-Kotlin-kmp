# P02-SEC-CODE-010C3B3C1 — Remove recipient and exception data from integration logs

- Status: READY
- Phase/Area/Type: P02 / Security / CODE
- Priority/Risk/Size: P1 / HIGH / S
- Owner: AI
- Depends on: P02-CI-CODE-010C3B3C
- Blocks: P02-CI-CODE-010C3C

## Scope and acceptance

The test-message admin action logs even a redacted recipient and logs `$e->getMessage()` from a catch. Replace both with fixed, nonidentifying event codes or remove the logs. Preserve admin success/failure behavior, avoid exposing provider details in UI, and test success and exception paths with synthetic recipient and exception text; neither logs nor HTTP response may contain recipient, message body, credential or exception detail. Allowed: integrations controller, focused tests and docs/evidence. Evidence: `docs/evidence/P02-SEC-CODE-010C3B3C1/`.

## Completion record

- Final status: TODO; fix owner from C3B3C triage.
