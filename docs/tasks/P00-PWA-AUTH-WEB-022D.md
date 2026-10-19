# P00-PWA-AUTH-WEB-022D — isolated browser logout request

- Status: AWAITING_MANUAL_QA; Size: S; Owner: BOTH; Risk: HIGH
- Depends on: `P00-PWA-AUTH-WEB-022C` automated client work; owner authorized independent work ahead of backend integration.
- Add a browser-only API operation for `POST /api/auth/web/logout`, using the HttpOnly cookie and scoped request header. A 401/logout failure must not trigger automatic refresh or be reported as successful server-side revocation. Propagate cancellation and never send a refresh credential in JSON. Keep the existing repository sign-out unwired until its local cleanup and concurrent-refresh lifecycle are separately verified. Preserve native endpoints and retained sites.
- Test compiled browser client with synthetic Fetch for success and server error, credentials/header and no retry/refresh; run JVM regression and JS/JVM app compilers. Record actual limitations and owner steps under `docs/evidence/P00-PWA-AUTH-WEB-022D/`. This is a network API prerequisite, not complete logout or a P00 Gate pass.
- Network changes require `AWAITING_MANUAL_QA` after automated checks. Manual confirmation must use an integrated synthetic backend, not a retained site or a fake success.

## Automated work — 2026-10-19

- Implemented only the browser API operation with 401 refresh blocked and verified it with substituted-Fetch browser tests, JVM guard and app compilation. [Evidence and pending owner steps](../evidence/P00-PWA-AUTH-WEB-022D/verification.md).
- The sign-out UI/repository is not wired; concurrent refresh and cache cleanup remain unverified. No actual revocation or owner confirmation is claimed.
