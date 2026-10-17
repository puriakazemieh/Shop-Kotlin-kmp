# P00-PWA-AUTH-WP-021B — WordPress browser-session infrastructure

- Status: READY; Size: M; Owner: AI; Risk: HIGH
- Depends on: `P00-PWA-AUTH-WP-021A`.
- Implement only the reviewed server-side session storage, expiry, atomic rotation/replay handling and revocation primitives, with automated positive, negative and concurrency tests. Do not expose partially protected public routes. Preserve existing mobile behavior, WordPress installations, data and unrelated changes.
- Follow the approved `021A` contract; if the implementation exceeds M or its safety depends on an unresolved design choice, stop and propose smaller cards. Record reproducible redacted results under `docs/evidence/P00-PWA-AUTH-WP-021B/`.
- This isolated infrastructure card does not demonstrate a working PWA login and does not authorize deployment. Integration and manual QA remain pending.
