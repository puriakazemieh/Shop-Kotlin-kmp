# Isolated browser logout request — not yet connected to sign-out

2026-10-19. Status: `AWAITING_MANUAL_QA`. No retained WordPress site, Spring installation or user account was modified. `AuthApi.logoutWeb()` is a browser-only network primitive; the app's repository and logout button **do not call it yet**.

It posts to the web logout endpoint with browser credentials and the scoped custom header, without JSON refresh token or Authorization header. It uses Ktor's auth circuit breaker so a 401 does not invoke refresh and returns an error for any non-success response. The method does not catch cancellation, though cancellation was not independently simulated. A server error is not represented as successful revocation. Native callers are rejected before making this browser request.

The compiled Ktor JS client passed seven Chrome Headless 153 tests with substituted Fetch, including 200, 401 and 503 logout cases, absence of bearer/body, cookie credential mode, scoped header and exactly one request per call. Seven JVM tests passed, including a native guard. Both `:composeApp:compileKotlinJs` and `:composeApp:compileKotlinJvm` passed offline. The initial compilation caught a test-only Kotlin generic inference error; after correcting it all suites were rerun. The same local `skiko` generated-output workaround described in [022B](../P00-PWA-AUTH-WEB-022B/verification.md) was needed for the browser test. No real browser cookie jar, server-side revocation, cancellation, Android/iOS or deployed backend was tested.

## Remaining safety work

Before wiring this primitive to sign-out, define and test local token/profile/cache cleanup, failure messaging, Ktor bearer cache invalidation and races with in-flight refresh/logout and multiple tabs. A local token clear alone cannot prove server-side revocation. WP's backend route and Spring compatibility also remain unverified. The existing sign-out behavior is unchanged; this child is not full session/logout completion.

## Owner QA after integration and a new build

1. On an isolated synthetic WP/PWA instance, log in as the provided test user, then use the actual sign-out button after the operation is wired. The UI must leave authenticated screens and a new protected request must be rejected; in DevTools Network confirm one successful `web/logout` request, without exposing or sharing cookie or Authorization values.
2. With the synthetic server configured to fail logout, repeat. The UI must not report server revocation as successful; check that local sign-out behavior and the warning match the final integrated design. Confirm a 401 logout did not cause a `web/refresh` request. The setup for controlled failure must be supplied with the integrated fixture.
3. Repeat the successful flow against a separate synthetic Spring server and verify its cookie is revoked. If the backend or button is not yet ready, leave the check pending.

Only the owner may confirm these checks. No claim is made that they can be run against the current retained sites today.
