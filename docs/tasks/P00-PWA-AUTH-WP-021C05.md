# P00-PWA-AUTH-WP-021C05 — isolated refresh, logout and reset handlers

- Status: BLOCKED; Size: M; Owner: AI; Risk: HIGH
- Depends on: `P00-PWA-AUTH-WP-021C04`.
- Implement and test unregistered web refresh, logout and both password-reset handlers using the reviewed session and policy helpers. Match `021A` for atomic rotation/replay, server-side revocation, no credential leakage, reset invalidation, idempotent logout and accurate storage-failure reporting. Keep mobile endpoints and semantics unchanged.
- Do not register routes/hooks, send cookies, migrate or change live HTTP behavior. Exercise only isolated synthetic tests, including replay, expiry, reset/password change and storage failure. If the scope exceeds M, split before implementation.
- Record redacted evidence in `docs/evidence/P00-PWA-AUTH-WP-021C05/`. Network activation and owner manual QA remain `021C07`.

## Preparatory work — 2026-10-18

The owner authorized continuing work not yet READY. The independently testable, inactive refresh/logout part is split into `021C05A`, based only on completed session/store and password-login primitives. It cannot enable routes, bypass `021C04`, change the parent status, or constitute reset/end-to-end acceptance. This parent remains BLOCKED until its stated dependency and all its own acceptance criteria are met.

The isolated `021C05B01` characterization found a legacy email-reset defect. It is now repaired in the limited email handler: [synthetic evidence](../evidence/P00-PWA-AUTH-WP-021C05/verification.md) proves that the endpoint accepts the actual WP emailed key, validates it in WP core, consumes it once and emits no credentials. This does not complete the parent: C04 remains blocked and refresh/logout integration remains outstanding.
