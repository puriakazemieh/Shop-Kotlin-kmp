# P00-PWA-AUTH-WP-021A — WordPress browser-session security contract

- Status: DONE; Size: S; Owner: AI; Risk: HIGH
- Depends on: `P00-PWA-AUTH-CODE-020`.
- Scope: document the browser session contract for the WordPress bridge: cookie attributes, origin and CSRF enforcement, session storage and expiry, atomic rotation and replay handling, revocation, logout, reset-password behavior, and failure modes. Preserve existing mobile and Spring contracts.
- Specify HTTPS and reverse-proxy trust assumptions, and a safe, explicitly bounded local QA approach. Never silently treat ordinary local HTTP as production HTTPS. Identify exact test cases including concurrent refresh, invalid origins, replay, missing or invalid sessions, and absence of refresh tokens in response bodies and browser storage.
- Review the local controller and clients and `P01-SECURITY-CODE-005C`; resolve compatibility requirements before implementation. Do not change code, expose endpoints, install certificates or deploy. If the work exceeds M, stop and propose smaller cards.
- Evidence: put the resulting design and review under `docs/evidence/P00-PWA-AUTH-WP-021A/`; design alone does not prove security or login success. No user manual test is requested for this design-only card.
- Completion: update this card and queue with evidence; only the next eligible card may become READY.

## Completion record — 2026-10-17

- Reviewed the Bridge auth/JWT/CORS code, JS client request and token flows, the Spring web-session controller, and the earlier P01 contract. Identified the reset-response leak risk, refresh DTO/lifecycle gaps and hostname-only bearer scoping as follow-up requirements.
- Defined the WordPress browser-session, per-installation cookie naming, transaction/replay, origin/CSRF, password-reset, proxy/TLS and explicitly bounded localhost QA contract, plus concrete positive/negative/integration tests.
- Evidence and reference links: `docs/evidence/P00-PWA-AUTH-WP-021A/security-contract.md`.
- This is documentation/design only. No security implementation, login test, site/database change or deployment was performed; no manual test is due for this card. Parent task and P00-T05 remain open.
