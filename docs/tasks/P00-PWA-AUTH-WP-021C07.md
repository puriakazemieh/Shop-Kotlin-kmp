# P00-PWA-AUTH-WP-021C07 — integrate and verify protected WordPress routes

- Status: DONE; Size: L; Owner: BOTH; Risk: HIGH
- Depends on: `P00-PWA-AUTH-WP-021C07C`.
- Integrate the complete reviewed adapters, migration, bearer verification, guard, CORS/cookie handling and all seven browser-only routes together. Public handlers must not become reachable unless every required control is active; reject unsafe configuration. Preserve all mobile endpoints and use a separate synthetic WordPress fixture, or first establish a verified backup and explicit recovery path before changing a preserved local QA site. No production deployment.
- Run positive/negative route and real-browser/backend tests specified by `021A`, including origins, CSRF headers, cookies, TLS/local exception, no-store, login/registration/OTP/reset, rotation/replay, revocation, password changes, failures and mobile compatibility. Record redacted reproducible evidence in `docs/evidence/P00-PWA-AUTH-WP-021C07/`. If larger than M, split before implementation.
- This changes network behavior: provide exact URL/environment, synthetic test account or setup, step-by-step owner checks and success criteria. Remain `AWAITING_MANUAL_QA` until the owner confirms. Do not call backend checks successful PWA end-to-end login: client lifecycle and final owner QA remain `P00-PWA-AUTH-WEB-022` and `P00-PWA-AUTH-QA-023`.

## Split — 2026-10-25

The ready card contains activation configuration, seven browser-only endpoints, route/network negatives and owner browser verification. It exceeds M, so it is split without activating any endpoint: `C07A` establishes a fail-closed activation guard and the four session lifecycle routes; `C07B` adds the three recovery routes and their isolated route tests; `C07C` runs the complete synthetic network matrix and records the owner handoff. This parent cannot be closed by a child alone.

## Completion — 2026-10-26

The C07A–C07C split is reconciled in `docs/evidence/P00-PWA-AUTH-WP-021C07/verification.md`. The isolated fixture runs all seven routes; the owner completed the route-network manual QA. This is WordPress backend acceptance only: PWA lifecycle integration remains `WEB-022`.
