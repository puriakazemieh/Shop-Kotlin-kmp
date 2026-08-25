# P01-SECURITY-CODE-005D — JVM token persistence policy

- Status: DONE
- Owner: AI
- Depends on: P01-SECURITY-CODE-005C
- Goal: refresh token persistent روی JVM ذخیره نشود و بسته‌شدن برنامه logout کند.

## Completion record

- Implemented at: 2026-08-25
- Changed files: `TokenSettingsFactory` platform actuals and data DI.
- Verification: `.\gradlew.bat :core:data:compileKotlinJvm :core:data:compileKotlinJs :core:data:compileAndroidMain --console=plain` exit 0.
- Evidence: `docs/evidence/P01-SECURITY-CODE-005D/verification.md`
- Final status: DONE
