# P00-PWA-AUTH-WEB-022H — browser sign-out and multi-tab lifecycle

- Status: AWAITING_MANUAL_QA; Size: M; Owner: AI; Risk: HIGH
- Depends on: `P00-PWA-AUTH-WEB-022G`.
- Wire browser sign-out to the guarded server logout before local state is cleared, fail safely when server revocation cannot be confirmed, and coordinate auth-state changes across tabs without exposing credentials. Verify logout does not trigger a refresh retry and leaves protected routes inaccessible after reload.
- Preserve the existing native sign-out behavior. Add automated coverage and a focused manual QA step.

## Implementation and automated evidence — 2026-10-27

- Browser sign-out first calls the guarded server logout. Local credentials and
  the cached profile are cleared only after that call succeeds; a rejected
  server revocation leaves the local state intact.
- JavaScript tabs use `BroadcastChannel` with one fixed, credential-free
  `carmilla-browser-sign-out-v1` signal. The receiver clears its own in-memory
  token and cached profile; it never receives or persists a token, email, or
  cookie value. Android, iOS, and JVM use a no-op implementation and retain
  their existing sign-out behavior.
- `:core:data:compileKotlinJs :core:data:compileKotlinJvm` passed.
- `BrowserSessionLifecycleTest` and
  `BrowserLogoutRequestTest` passed in ChromeHeadless. The latter verifies a
  logout request carries no bearer/body and does not invoke refresh after 401
  or 503 failures. The known generated Skiko test-runtime asset omission was
  repaired only in ignored build output before the test run.
- A fresh PWA bundle was built and deployed to the disposable WordPress QA
  site at `http://localhost:8091/pwa/#/login`; cache references are version 12.

## Manual QA required

1. Open the PWA URL above in two normal browser tabs (do not use incognito).
   Sign in in the first tab with the isolated QA account.
2. In the second tab, refresh once and open Profile. The same signed-in account
   must be shown.
3. In the first tab, use the application sign-out. Without refreshing the
   second tab, open Profile there. It must immediately show the guest/login
   state and must not show the old account.
4. Refresh the second tab and try opening Profile again. It must remain guest.
   Return to the first tab and refresh it too; it must also remain guest.

Success criterion: both tabs become and remain signed out after a single
application sign-out, with no stale profile and no generic error. Report the
observed result; this card must remain `AWAITING_MANUAL_QA` until then.
