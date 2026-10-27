# P00-PWA-AUTH-WEB-022G — evidence map

## Automated verification — 2026-10-26

- `:core:data:compileKotlinJs :core:network:compileTestKotlinJs :composeApp:compileKotlinJs` passed with JDK 17.
- `:core:network:jsBrowserTest --tests com.kazemieh.network.common.BrowserCookieRefreshTest` passed: 3 tests, 0 failures.
  The generated `skiko.mjs` and `skiko.wasm` test-runtime assets were copied only into the ignored generated network-test directory to work around the existing Kotlin/JS test packaging omission; no source or tracked generated artifact was changed.
- The browser test proves the guarded startup call sends the browser session cookie (`credentials: include`), sends the required web-request header, does not send a body or JavaScript refresh token, and rejects invalid/exposed refresh responses.
- Existing `TokenSettingsSecurityTest` remains the proof that JS token settings are memory-only; persistent settings remain unchanged for Android, iOS, JVM and Spring clients.

## Implementation evidence

- `AuthRepository.restoreSession()` is browser-only and exchanges the HttpOnly cookie for an in-memory access token.
- A missing, expired, malformed, or rejected cookie leaves the app unauthenticated and clears only the local profile namespace. It does not clear unrelated user preferences.
- App navigation waits for this one guarded restoration attempt before rendering protected state.

## Manual QA required

1. In the WordPress QA deployment, sign in, refresh the PWA tab, and open Profile. The same signed-in account must remain visible.
2. Use the application logout, refresh the tab, then open Profile. The app must remain signed out; it must not show a previous profile or a generic error.
3. Close the PWA tab, reopen it at the same URL, and verify the result matches the server cookie state: signed in only while the valid cookie remains; otherwise signed out.

## Disposable Docker QA preparation — 2026-10-27

- A fresh WordPress 6.4/PHP 8.1 fixture at `http://localhost:8091` was configured with the isolated browser-session schema and synthetic account only.
- Browser-session route policy smoke test passed: 97 checks, 0 failures. The additional Docker Desktop gateway is accepted only when the explicit local-QA configuration supplies its exact IP; production and non-local configurations reject it.
- Real route sequence passed over the WordPress REST endpoint: password login `200`, logout `200`, and refresh after logout `401 INVALID_SESSION`.
- The PWA development distribution was rebuilt and deployed with a bumped worker/bundle version. The visible login form and its Persian labels load from the deployed resource bundle.
- The remaining manual check is intentionally browser-specific: a Secure, HttpOnly cookie must be observed across a real Chromium reload and cleared by application logout.
