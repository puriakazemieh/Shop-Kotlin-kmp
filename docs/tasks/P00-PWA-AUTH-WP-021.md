# P00-PWA-AUTH-WP-021 — WordPress web-session backend

- Status: BLOCKED; Size: >M after review; Owner: AI; Risk: HIGH
- Depends on: P00-PWA-AUTH-CODE-020.
- Implement and test the browser-session contract for the local WordPress bridge: login/register and secure cookie-backed session issuance, without exposing refresh tokens to JavaScript. Design scoped rotation, revocation, CSRF and origin enforcement before implementation. Fail closed when secure deployment assumptions are unmet; local HTTP must never be silently treated as production HTTPS.
- Preserve mobile endpoints, WordPress sites, databases and user changes. Add negative authentication/security tests and evidence. If the work exceeds M, split it further before starting.
- UI/network changes require `AWAITING_MANUAL_QA`; integration verification is performed under P00-PWA-AUTH-QA-023. No production deploy.

## Scope review — 2026-10-17

The local WordPress web-login route returned HTTP 404. Existing mobile authentication returns refresh tokens in JSON, and the existing JWT revocation is user-wide rather than a per-browser-session rotation/replay mechanism. The work therefore includes a new session lifecycle, origin/CSRF policy, storage and concurrency controls, route integration, and security tests. It is larger than M; no implementation or deployment was performed in this review.

Proposed child tasks, in dependency order:

1. **Security contract and local QA design:** define the browser cookie, origin/CSRF checks, session storage, atomic rotation, replay and revocation behavior, HTTPS requirements, and a safe local test approach. Specify negative tests without changing production behavior.
2. **Session infrastructure:** implement and unit-test the agreed storage, expiry, rotation/replay and revocation controls without exposing partially protected public routes.
3. **WordPress route integration:** implement web login/register/refresh/logout against the reviewed infrastructure, preserve existing mobile routes, and run positive and negative tests. Provide explicit manual QA and keep the task awaiting human confirmation where required.

The proposals are recorded as child cards `P00-PWA-AUTH-WP-021A` through `021C`, with only the first ready. Then complete the existing PWA lifecycle card `P00-PWA-AUTH-WEB-022` and integrated owner QA `P00-PWA-AUTH-QA-023`. Creating the cards does not complete the work or constitute security approval.

Evidence: `docs/evidence/P00-PWA-AUTH-WP-021/scope-review.md`.
