# P00-PWA-AUTH-WEB-022G — transient browser credentials and startup restoration

- Status: READY; Size: M; Owner: AI; Risk: HIGH
- Depends on: `P00-PWA-AUTH-WEB-022F`.
- Store browser access credentials only in memory, never in persistent browser settings. On PWA startup, restore a valid session solely by calling the guarded cookie-backed refresh endpoint; a missing, expired or malformed cookie must leave the user unauthenticated without a misleading error.
- Preserve persistent token storage and refresh behavior for Android, iOS and JVM/Spring clients. Add positive and negative browser tests, including reload after login and expired/no-cookie startup.
- Requires manual browser QA after automated verification.
