# P02-SEC-CODE-010C2B5A — Normalize integration form input and redirect

- Status: DONE
- Phase/Area/Type: P02 / Security / CODE
- Priority/Risk/Size: P1 / MEDIUM / S
- Owner: AI
- Depends on: P02-CI-CODE-010C2B5
- Blocks: P02-SEC-CODE-010C2B5B

## Scope and acceptance

In the admin integration test-message form, unslash and validate the scalar type/recipient/body inputs before sanitization, and use a host-validated admin redirect. Normalize read-only `tab` query input without changing nonce/capability gates. Characterize malformed arrays, slashed text and redirect target; test with synthetic/mocked adapters only, no real SMS/email. Allowed: integration controller, focused tests, `docs/**`. Evidence: `docs/evidence/P02-SEC-CODE-010C2B5A/`.

## Completion record

- Final status: DONE (2026-11-03)
- Evidence: `docs/evidence/P02-SEC-CODE-010C2B5A/verification.md`; direct WordPress and HTTP scenarios passed without external delivery.
