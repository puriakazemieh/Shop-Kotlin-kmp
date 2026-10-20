# P00-PWA-AUTH-WP-021C07 — integrate and verify protected WordPress routes

- Status: BLOCKED; Size: M; Owner: BOTH; Risk: HIGH
- Depends on: `P00-PWA-AUTH-WP-021C06`.
- Integrate the complete reviewed adapters, migration, bearer verification, guard, CORS/cookie handling and all seven browser-only routes together. Public handlers must not become reachable unless every required control is active; reject unsafe configuration. Preserve all mobile endpoints and use a separate synthetic WordPress fixture, or first establish a verified backup and explicit recovery path before changing a preserved local QA site. No production deployment.
- Run positive/negative route and real-browser/backend tests specified by `021A`, including origins, CSRF headers, cookies, TLS/local exception, no-store, login/registration/OTP/reset, rotation/replay, revocation, password changes, failures and mobile compatibility. Record redacted reproducible evidence in `docs/evidence/P00-PWA-AUTH-WP-021C07/`. If larger than M, split before implementation.
- This changes network behavior: provide exact URL/environment, synthetic test account or setup, step-by-step owner checks and success criteria. Remain `AWAITING_MANUAL_QA` until the owner confirms. Do not call backend checks successful PWA end-to-end login: client lifecycle and final owner QA remain `P00-PWA-AUTH-WEB-022` and `P00-PWA-AUTH-QA-023`.
