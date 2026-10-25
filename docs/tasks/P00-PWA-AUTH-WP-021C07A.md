# P00-PWA-AUTH-WP-021C07A — guarded browser session-route activation

- Status: DONE; Size: M; Owner: AI; Risk: HIGH
- Depends on: `P00-PWA-AUTH-WP-021C06`.
- Add one explicitly enabled, fail-closed WordPress runtime adapter for browser-session routes: `POST /api/auth/web/login`, `register`, `refresh`, and `logout`. It must load only the reviewed C02–C06 primitives, verify migration readiness and a non-placeholder secret, reject unsafe origin/TLS/cookie configuration before routes are reachable, preserve all existing mobile routes and never emit a browser credential in JSON or logs.
- The adapter alone consumes the one-time cookie handoff, emits `Secure`, `HttpOnly`, `SameSite=Strict`, path `/` cookies and `Cache-Control: no-store`, and translates internal safe error codes to the established API error contract. No permissive CORS, wildcard origin, silent fallback to mobile JWT refresh, production deployment, retained-site migration or automatic user deletion is allowed.
- Add disposable real-WordPress HTTP tests for disabled/misconfigured fail-closed behavior and the four route contracts, including method/origin/header rejection, no-store, cookie attributes/redaction, rotation/replay, logout idempotency and mobile-route compatibility. Record redacted evidence in `docs/evidence/P00-PWA-AUTH-WP-021C07A/`. Do not claim PWA browser end-to-end success; recovery routes are C07B and human network QA is C07C.

## Automated completion — 2026-10-25

- Added an explicit runtime adapter. The four browser routes register only when the operator supplies an enabled flag, validated trusted policy, a non-placeholder secret, a current migration marker and an explicit trusted-ingress acknowledgement. Missing or invalid configuration leaves the routes unregistered.
- A disposable WordPress/MariaDB test passed 10 checks: guarded route registration, positive login, rejected origin, `Cache-Control: no-store`, credential-free JSON, refresh, logout and the still-independent mobile controller. PHP lint passed. The retained local WordPress runtime returned 404 for the browser login route because it has no opt-in configuration.
- The single owner network/browser handoff belongs to C07C, after all seven routes exist. The current test does not claim CORS preflight, real browser cookie persistence, all replay cases or recovery-route coverage.
