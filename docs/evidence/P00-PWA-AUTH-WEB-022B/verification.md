# Browser cookie refresh — partial implementation, awaiting owner QA

2026-10-19. This is **not** completion of the browser-session lifecycle or P00 Gate. No backend, preserved WordPress installation or user account was modified; no integrated PWA build was deployed.

## Change and automated checks

- After a 401 on an approved API URL, the existing browser client can request `/api/auth/web/refresh` with the browser cookie even when JavaScript holds no refresh token. A successful response must contain a nonblank access token and no JSON refresh token. Non-2xx and malformed responses are rejected without logging the exception; cancellation propagates. The non-browser branch still requires, sends and accepts a rotated nonblank refresh token.
- A previously added path-security regex used Java-style `(?i)`. Kotlin/JS could not construct it, so `runCatching` silently treated even approved URLs as unapproved: browser bearer sending and 401 refresh were blocked. Replace the inline flag with portable `RegexOption.IGNORE_CASE`. Avoid rewriting URL segments unnecessarily, and set the encoded path when a WordPress base prefix is required. This is a correction to `022A`, not evidence that all browser security/integration is complete.
- Chrome Headless 153 on Windows: `:core:network:jsBrowserTest --offline`, five tests passed. The added tests use the real compiled Ktor JS client with **substituted Fetch**, not an actual backend or cookie jar. They verify approved bearer delivery, rejection of a different origin/port and encoded traversal, retained WordPress-style base paths, cookie-credential Fetch settings, 401 refresh with no JS refresh token, and rejection of 503, blank access and exposed refresh tokens.
- `:core:network:jvmTest --offline`: seven tests passed. The new test uses a real loopback HTTP server and verifies no refresh request without a native refresh credential, correct credential submission and rotation, and rejection of a response missing the native refresh credential. This is JVM, not Android or iOS.
- `:composeApp:compileKotlinJs :composeApp:compileKotlinJvm --offline` succeeded. Android/iOS and Spring/WordPress live integration were not tested. A compiled source target is not a deployed PWA build.
- The first browser execution failed before tests because generated `skiko.mjs` and `skiko.wasm` were missing. As an existing local workaround, missing files only were copied from the already generated `build/js/packages/kmp-shop-core-data-test/kotlin/` into `build/js/packages/kmp-shop-core-network-test/kotlin/` after `:core:network:jsTestTestDevelopmentExecutableCompileSync`; rerunning the browser test then passed. This does not fix the build configuration, and a new compile-sync may remove those copied files.
- Initial browser tests also failed because the URL guard rejected every JS URL; after the regex correction, the full suites were rerun. No conclusion about real HTTPS, browser redirect behavior, CSRF, cookies or third-party traffic is drawn from substituted Fetch.

## Still open

Initial session restoration after reload, cross-tab single-flight, logout, the WP-specific guard/header, reset routes, server-side session integration, real browser cookie behavior and actual WP/Spring compatibility remain in parent `WEB-022` and `021C07`. The WordPress web refresh route is not ready. The existing legacy OTP race is unchanged. Until integration is ready, these steps cannot be honestly completed against the preserved sites.

Follow-up: [022C](../P00-PWA-AUTH-WEB-022C/verification.md) adds the scoped **client header**, but the server-side guard and real backend compatibility remain unverified.

## Owner manual QA — only after integrated backend and a new test build exist

1. Open the provided isolated synthetic PWA and WordPress environment using a newly built version; sign in with the synthetic test account. In Chrome DevTools → Network, keep the log open, use a protected page, and wait for the short-lived access token to expire according to the test fixture. Repeat the protected request. A 401 may be followed by **one** successful `web/refresh` and then a successful protected retry. Do not share Authorization headers, cookies or their values.
2. Repeat with a synthetic session that the test fixture has revoked: the request must not silently authenticate, protected data must not be shown, and sign-in should be required. Report only status codes and visible behavior, not credentials.
3. Repeat steps 1–2 against a separately configured synthetic Spring backend; verify the expected direct API URLs and no redirect. This is necessary because the JS client shares the cookie-refresh code. If either backend is not ready, record that case as pending rather than passed.
4. In the controlled cross-origin test fixture, verify an unrelated test origin receives no Authorization header. Do not send credentials to an actual third party.

Only the owner can confirm these checks; browser reload, cross-tab and logout require their own later acceptance steps.
