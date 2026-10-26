# P00-PWA-AUTH-WEB-022G — transient browser credentials and startup restoration

- Status: AWAITING_MANUAL_QA; Size: M; Owner: AI; Risk: HIGH
- Depends on: `P00-PWA-AUTH-WEB-022F`.
- Store browser access credentials only in memory, never in persistent browser settings. On PWA startup, restore a valid session solely by calling the guarded cookie-backed refresh endpoint; a missing, expired or malformed cookie must leave the user unauthenticated without a misleading error.
- Preserve persistent token storage and refresh behavior for Android, iOS and JVM/Spring clients. Add positive and negative browser tests, including reload after login and expired/no-cookie startup.
- Requires manual browser QA after automated verification.

## Automated evidence — 2026-10-26

- Kotlin/JS compilation passed for data, network-test and Compose app modules.
- `BrowserCookieRefreshTest` passed in ChromeHeadless: 3 tests, 0 failures.
- Evidence: [`docs/evidence/P00-PWA-AUTH-WEB-022G/evidence-map.md`](../evidence/P00-PWA-AUTH-WEB-022G/evidence-map.md).

## Manual QA step

Open the WordPress QA PWA, log in, reload, and confirm Profile still shows the signed-in account. Then log out, reload, and confirm Profile remains signed out with no stale profile. This task stays awaiting manual QA until the result is confirmed.
