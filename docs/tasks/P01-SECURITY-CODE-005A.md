# P01-SECURITY-CODE-005A — Android Keystore token storage

- Status: DONE
- Owner: AI
- Depends on: P01-SECURITY-CODE-004
- Scope: فقط `core/data/**`, `core/network/**`, `docs/**`.
- Goal: access/refresh token از SharedPreferences عادی به EncryptedSharedPreferences/Android Keystore منتقل شود؛ token قدیمی حذف و login دوباره لازم شود.
- Acceptance: plaintext token در SharedPreferences صفر؛ logout/expiry هر دو token را پاک کند؛ test synthetic pass.
- Evidence: `docs/evidence/P01-SECURITY-CODE-005A/`
- Rollback: forward-fix؛ plaintext token دوباره فعال نشود.
- Completed: 2026-08-25؛ Android QA توسط کاربر تأیید شد؛ `:core:data:compileAndroidMain` exit code 0.
