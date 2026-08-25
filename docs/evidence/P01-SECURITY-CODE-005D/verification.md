# P01-SECURITY-CODE-005D — Verification evidence

Date: 2026-08-25

`TokenManager` now receives a platform-specific token `Settings` instance. On JVM, `createTokenSettings()` returns a new in-memory implementation for every application process, while profile settings remain in Java Preferences. Therefore access and refresh tokens cannot survive process shutdown.

Command: `.\gradlew.bat :core:data:compileKotlinJvm :core:data:compileKotlinJs :core:data:compileAndroidMain --console=plain`

Result: PASS, exit 0.
