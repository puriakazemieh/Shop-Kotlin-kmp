# P00-PWA-AUTH-WEB-022 — evidence map

## Scope and synthesis — 2026-10-27

Parent task `P00-PWA-AUTH-WEB-022` reconciles the browser session lifecycle decomposed into three focused subtasks:
1. `P00-PWA-AUTH-WEB-022F`: Guarded credential routes (`/api/auth/web/*`) for browser/PWA target, preserving native mobile and Spring routes.
2. `P00-PWA-AUTH-WEB-022G`: Transient in-memory token storage for JS/browser targets, startup session restoration via HttpOnly cookie refresh, and clean unauthenticated state on missing/expired cookies.
3. `P00-PWA-AUTH-WEB-022H`: Server-side session revocation before local cleanup, multi-tab sign-out coordination via credential-free `BroadcastChannel`, and suppression of 401 refresh during logout.

## Automated verification

- `:core:data:compileKotlinJs :core:data:compileKotlinJvm` passed cleanly.
- `:composeApp:compileKotlinJs :composeApp:compileKotlinJvm` passed cleanly (290 actionable tasks, 0 errors).
- `BrowserCookieRefreshTest` passed (3 tests, 0 failures) in ChromeHeadless.
- `BrowserSessionLifecycleTest` and `BrowserLogoutRequestTest` passed in ChromeHeadless.
- Native Android, iOS, and JVM/Spring persistence semantics remain untouched.

## Deployment and environment

- Disposable Docker QA fixture running at `http://localhost:8091/pwa/#/login` (`carmilla-p00-web-auth-wp-20261027`).
- Active database fixture: `carmilla-p00-browser-session-fixture-20261025`.

## Focused manual QA status

- Focused two-tab sign-out confirmed by owner on `WEB-022H`.
- Focused single-tab login and startup restore confirmed by owner on `WEB-022G`.
- Focused guarded credential route acceptance confirmed on `WEB-022F`.

## Next gate

Full end-to-end integration and release gate `P00-T05` is scoped to `P00-PWA-AUTH-QA-023` and remains required before claiming the full session gate passes.
