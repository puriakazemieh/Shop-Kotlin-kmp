# Browser web-auth header — scoped client prerequisite

2026-10-19. Status: `AWAITING_MANUAL_QA`. The client adds `X-Carmilla-Web-Request: 1` to browser POST requests only within the configured API origin and exact `/api/auth/web/` subtree. It removes that header on other browser requests; native clients do not receive it automatically. The check runs on each Ktor send and uses the approved scheme, host, port and base path. It is not authorization or server-side CSRF protection: the server must independently enforce the complete Origin, transport and request policy.

Six Chrome Headless 153 browser tests passed, using the compiled Ktor JS client with substituted Fetch. Header tests covered relative and absolute approved web-auth POSTs, refresh, GET, a non-web route, a similarly named route, a foreign origin and encoded path ambiguity. Existing tests also covered bearer scope and refresh. Seven JVM tests passed; a loopback native refresh confirmed the web-specific header was absent. `:composeApp:compileKotlinJs :composeApp:compileKotlinJvm --offline` succeeded. As described in [022B](../P00-PWA-AUTH-WEB-022B/verification.md), the browser test requires a local generated-output `skiko` workaround. No live server, real CORS preflight or cookie jar was tested; Android/iOS and actual Spring/WP compatibility are unverified. No existing site or account was changed.

The current Spring server's source config allows arbitrary request header names in CORS, but this static observation is not an actual deployment or browser compatibility test. On WordPress the complete browser routes and enforcement are not yet integrated. Do not deploy or claim the Phase 00 gate passed from this client-only change.

## Owner QA after integration and a new build

1. Open the isolated synthetic PWA and the integrated synthetic WordPress site. In Chrome DevTools → Network, sign in with the supplied synthetic user and select the `web/login` request. Under Request Headers verify `X-Carmilla-Web-Request` is `1`; confirm the login succeeds. Do not copy or share cookies, credentials or Authorization values.
2. In a separately prepared synthetic negative test, repeat the request without the custom header or with a disallowed origin; the **server** must reject it and issue no session. The test fixture must provide a safe way to perform this check; do not modify the preserved live sites.
3. Repeat the normal sign-in and refresh flow against the synthetic Spring backend to check its CORS preflight and response. A preflight or login failure is not a pass. Record status codes only.

These tests depend on backend readiness and a new build; none has yet been performed or confirmed by the owner.
