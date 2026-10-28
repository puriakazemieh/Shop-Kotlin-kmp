# Evidence for P01-SECURITY-CODE-006

- **Commands Executed:**
  - `.\gradlew.bat :core:network:jvmTest --tests "com.kazemieh.network.common.ApiOriginValidatorTest"` (Exit Code: 0)
  - `.\gradlew.bat :core:network:jsBrowserTest --tests "com.kazemieh.network.common.ApiOriginValidatorJsTest"` (Exit Code: 0)
  - `.\gradlew.bat :composeApp:compileKotlinJs` (Exit Code: 0)
  - `.\gradlew.bat :composeApp:compileKotlinJvm` (Exit Code: 0)
- **Changes:**
  - Created `ApiOriginValidator` in `core:network` common source set with strict validation of candidate overrides: rejects HTTP, localhost, private IPs (RFC 1918), and foreign origins not in `allowedAuthHosts` when in release mode.
  - Added `PlatformConfig.isReleaseBuild` across android, ios, jvm, and js targets to accurately determine release vs local dev fixture.
  - Enforced `ApiOriginValidator.validateOriginOverride` in `App.kt` before applying `baseUrlOverride`.
  - Hardened `pwa-bootstrap.js` with `validateUrlOverride` rejecting foreign origins even on dev fixture and sanitizing candidate URLs.
  - Updated PWA cache versions (`v=14`, cache namespace `v10`).
- **Manual Verification (Human QA):**
  - **Date:** 2026-10-28
  - **Environment:** WordPress 6.4 Docker fixture (`http://localhost:8091/pwa/`), Chrome browser with DevTools Network tab.
  - **Scenario 1 (Foreign Host Injection):** `http://localhost:8091/pwa/?wpApi=https://evil-attacker.example.com/api/` -> 0 requests sent to `evil-attacker.example.com`. Override rejected.
  - **Scenario 2 (Private IP Injection):** `http://localhost:8091/pwa/?wpApi=https://192.168.1.150/api/` -> Override rejected.
  - **Scenario 3 (Local Fixture Normal Operation):** `http://localhost:8091/pwa/#/login` -> Login page loads cleanly in Persian and connects to local fixture without network error.
- **Status:** DONE
