# P00-PWA-AUTH-WP-021C05 — isolated refresh, logout and reset handlers

- Status: TODO; Size: M; Owner: AI; Risk: HIGH
- Depends on: `P00-PWA-AUTH-WP-021C04`.
- Implement and test unregistered web refresh, logout and both password-reset handlers using the reviewed session and policy helpers. Match `021A` for atomic rotation/replay, server-side revocation, no credential leakage, reset invalidation, idempotent logout and accurate storage-failure reporting. Keep mobile endpoints and semantics unchanged.
- Do not register routes/hooks, send cookies, migrate or change live HTTP behavior. Exercise only isolated synthetic tests, including replay, expiry, reset/password change and storage failure. If the scope exceeds M, split before implementation.
- Record redacted evidence in `docs/evidence/P00-PWA-AUTH-WP-021C05/`. Network activation and owner manual QA remain `021C07`.

## Preparatory work — 2026-10-18

The owner authorized continuing work not yet READY. The independently testable, inactive refresh/logout part is split into `021C05A`, based only on completed session/store and password-login primitives. It cannot enable routes, bypass `021C04`, change the parent status, or constitute reset/end-to-end acceptance. This parent remains TODO until its stated dependency and all its own acceptance criteria are met.

The isolated `021C05B01` characterization found that the existing legacy email-reset endpoint rejects the WP email-selected key and accepts a first key already invalidated by WP core. See [redacted evidence](../evidence/P00-PWA-AUTH-WP-021C05B01/verification.md). This is an unresolved defect, not a fix or proof of atomic reset safety; no mobile endpoint behavior was changed.
