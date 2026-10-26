# P00-PWA-AUTH-WEB-022F evidence

## Code and automated coverage

- `core/network/src/commonMain/kotlin/com/kazemieh/network/auth/AuthApiImpl.kt` selects `/api/auth/web/` for browser password login, registration, forgotten-password and password-reset operations. Native routes and browser OTP remain intentionally unchanged.
- `core/network/src/jsTest/kotlin/com/kazemieh/network/common/BrowserLoginWithOtpRequestTest.kt` asserts the two guarded credentials endpoints and the unchanged mobile OTP endpoint.
- `./gradlew.bat --no-daemon :core:network:compileKotlinJs :composeApp:jsBrowserDevelopmentWebpack --console=plain -q` exited successfully on 2026-10-26.

## Runner limitation

- `:core:network:jsBrowserTest` could not start because Windows denied replacement of the generated file `core/network/build/test-results/jsBrowserTest/binary/output.bin`; this is a local file-lock condition, not a test assertion failure. No source or user-owned file was removed to bypass the lock.

## Owner QA required

1. Open the PWA against the WordPress QA fixture.
2. Use password login with the synthetic test account, then reload once and open Profile.
3. Expected for this focused card: login itself must complete and the request must not produce a generic error. Reload/session restoration is covered by the next card, so it is not an acceptance criterion here.

## Local browser fixture repair

- The initial standalone PWA URL selected the Android-emulator WordPress address (`10.0.2.2`) and therefore could not fetch the remote manifest from a desktop browser.
- `pwa-bootstrap.js` now accepts an explicit WordPress-only `wpApi` query parameter without changing the selected SKU or turning on the Spring bundle.
- The rebuilt development PWA is deployed beneath the isolated WordPress QA fixture. Its PWA document and `/wp-json/carmilla/v1/client-manifest` both returned HTTP 200 on 2026-10-26.
- The fixture initially emitted tenant `1`, while the generated `carmila` product specification expects `local-default`. The isolated fixture option was corrected to `local-default`; the verified manifest now returns that exact tenant id. This preserves the PWA's tenant fail-closed rule.
- Hash-route navigation could drop the one-off `wpApi` query used by the fixture. The bootstrap now derives the same-origin WordPress API only for the local `/pwa/` fixture; it leaves generated non-local deployments unchanged. The bootstrap URL was cache-versioned to `v=9`.
- On 2026-10-26, a clean browser navigation to `http://localhost:8091/pwa/#/login` showed no remote-config safety banner, and password login with the fixture account completed to the application home route. This is focused endpoint-selection evidence; reload/session restoration remains the next lifecycle card.
