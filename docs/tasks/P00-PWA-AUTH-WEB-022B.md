# P00-PWA-AUTH-WEB-022B — browser cookie refresh on a 401

- Status: BLOCKED; Size: M; Owner: BOTH; Risk: HIGH
- Depends on: `P00-PWA-AUTH-WEB-022A` automated security work. The owner authorized independent Phase 00 work ahead of the blocked WordPress integration.
- Correct only the existing Ktor 401 refresh path: a browser must use its HttpOnly cookie even when it has no JavaScript refresh token; a mobile client must still require and send its own refresh token. Reject failed responses, blank access tokens and a browser response that exposes a refresh credential; preserve cancellation and avoid logging credential-bearing errors. Do not claim startup restoration, cross-tab coordination, logout, WordPress request guards or a complete browser session.
- Add browser-engine and JVM regression tests. Network behavior changes require reproducible evidence and `AWAITING_MANUAL_QA`; owner verification follows an integrated backend and build, not an inactive fixture. Do not alter or deploy the retained WordPress sites or the Spring server.

## Automated work — 2026-10-19

- Implemented the scoped 401 refresh correction and fixed the browser-incompatible URL-security regex found during testing. Five browser-engine tests with substituted Fetch, seven JVM tests including loopback HTTP, and both app compilation targets passed. [Evidence, limits and owner steps](../evidence/P00-PWA-AUTH-WEB-022B/verification.md).
- No real backend integration or owner signoff has occurred. `WEB-022`, the WP guard/header, reload, logout and the P00 Gate remain open.
