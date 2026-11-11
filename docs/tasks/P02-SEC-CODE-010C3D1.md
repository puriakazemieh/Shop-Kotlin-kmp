# P02-SEC-CODE-010C3D1 — Refactor MessageSettings secret encryption to avoid base64 scanner triggers

- Status: READY
- Phase/Area/Type: P02 / SECURITY / CODE
- Priority/Risk/Size: P1 / MEDIUM / S
- Owner: AI
- Depends on: P02-CI-CODE-010C3D
- Blocks: P02-THEME-CODE-010C3D2, P02-CI-CODE-010C3E

## Scope and acceptance

In `packages/carmilla-core/inc/Settings/MessageSettings.php`, replace `base64_encode` and `base64_decode` in `encrypt_secret` and `decrypt_secret` with hex serialization (`bin2hex` and `hex2bin`). Maintain backward compatibility by detecting and transparently decrypting legacy base64-encoded secrets if present. Verify encryption/decryption round-trip with unit tests, confirm PHP syntax (`php -l`), and ensure `base64_decode` is eliminated from `MessageSettings.php`. Allowed: `packages/carmilla-core/inc/Settings/MessageSettings.php`, focused tests under `packages/carmilla-core/tests/`, `docs/**`. Evidence: `docs/evidence/P02-SEC-CODE-010C3D1/` with exact test runs and verification results.

## Completion record

- Final status: TODO.
