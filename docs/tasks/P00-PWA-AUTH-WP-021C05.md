# P00-PWA-AUTH-WP-021C05 — isolated refresh, logout and reset handlers

- Status: TODO; Size: M; Owner: AI; Risk: HIGH
- Depends on: `P00-PWA-AUTH-WP-021C04`.
- Implement and test unregistered web refresh, logout and both password-reset handlers using the reviewed session and policy helpers. Match `021A` for atomic rotation/replay, server-side revocation, no credential leakage, reset invalidation, idempotent logout and accurate storage-failure reporting. Keep mobile endpoints and semantics unchanged.
- Do not register routes/hooks, send cookies, migrate or change live HTTP behavior. Exercise only isolated synthetic tests, including replay, expiry, reset/password change and storage failure. If the scope exceeds M, split before implementation.
- Record redacted evidence in `docs/evidence/P00-PWA-AUTH-WP-021C05/`. Network activation and owner manual QA remain `021C07`.
