# P01-SECURITY-CODE-005E — Verification evidence

Date: 2026-08-25

`HttpClientFactory` now evaluates `ApiConfig.isApprovedApiHost(request.url.host)` before attaching Bearer credentials. The JVM test verifies that `api.example.test` is accepted while `attacker.example.test` is rejected.

Command: `.\gradlew.bat :core:network:jvmTest :core:network:compileKotlinJs :core:network:compileAndroidMain --console=plain`

Result: PASS, exit 0.
