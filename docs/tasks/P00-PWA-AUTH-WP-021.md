# P00-PWA-AUTH-WP-021 — WordPress web-session backend

- Status: READY; Size: M; Owner: AI; Risk: HIGH
- Depends on: P00-PWA-AUTH-CODE-020.
- Implement and test the browser-session contract for the local WordPress bridge: login/register and secure cookie-backed session issuance, without exposing refresh tokens to JavaScript. Design scoped rotation, revocation, CSRF and origin enforcement before implementation. Fail closed when secure deployment assumptions are unmet; local HTTP must never be silently treated as production HTTPS.
- Preserve mobile endpoints, WordPress sites, databases and user changes. Add negative authentication/security tests and evidence. If the work exceeds M, split it further before starting.
- UI/network changes require `AWAITING_MANUAL_QA`; integration verification is performed under P00-PWA-AUTH-QA-023. No production deploy.
