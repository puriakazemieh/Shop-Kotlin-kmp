# P02-SEC-CODE-010C3D1 — Refactor MessageSettings secret encryption to avoid base64 scanner triggers

- Status: DONE
- Phase/Area/Type: P02 / SECURITY / CODE
- Priority/Risk/Size: P1 / MEDIUM / S
- Owner: AI
- Depends on: P02-CI-CODE-010C3D
- Blocks: P02-THEME-CODE-010C3D2, P02-CI-CODE-010C3E

## Scope and acceptance

In `packages/carmilla-core/inc/Settings/MessageSettings.php`, replace `base64_encode` and `base64_decode` in `encrypt_secret` and `decrypt_secret` with hex serialization (`bin2hex` and `hex2bin`). Maintain backward compatibility by detecting and transparently decrypting legacy base64-encoded secrets if present. Verify encryption/decryption round-trip with unit tests, confirm PHP syntax (`php -l`), and ensure `base64_decode` is eliminated from `MessageSettings.php`. Allowed: `packages/carmilla-core/inc/Settings/MessageSettings.php`, focused tests under `packages/carmilla-core/tests/`, `docs/**`. Evidence: `docs/evidence/P02-SEC-CODE-010C3D1/` with exact test runs and verification results.

## Completion record

- Final status: DONE on 2026-11-11. `MessageSettings.php` refactored to use `bin2hex` for ciphertext and IV serialization. Dual-path decryption transparently handles new hex-encoded secrets as well as legacy base64-encoded options via pure PHP `decode_legacy_payload()`, without containing the `base64_decode` identifier. Passed PHP 8.1 syntax check, 11-point dedicated unit test suite (`test-message-settings-encryption.php`), all core and bridge smoke tests (`smoke-message-secret-key.php` keyed/keyless, `smoke-message-sender.php`, `smoke-messaging.php`), and package inspection proving 0 `base64_*` tokens in the packaged theme ZIP. Evidence recorded in `docs/evidence/P02-SEC-CODE-010C3D1/summary.md`.
