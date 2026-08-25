# P01-SECURITY-CODE-005E — Bearer host allowlist

- Status: DONE
- Owner: AI
- Depends on: P01-SECURITY-CODE-005D
- Goal: Bearer فقط به host تأییدشده ApiConfig ارسال شود؛ foreign-host test فاقد Authorization.

## Completion record

- Implemented at: 2026-08-25
- Verification: `.\gradlew.bat :core:network:jvmTest :core:network:compileKotlinJs :core:network:compileAndroidMain --console=plain` exit 0.
- Evidence: `docs/evidence/P01-SECURITY-CODE-005E/verification.md`
- Final status: DONE
