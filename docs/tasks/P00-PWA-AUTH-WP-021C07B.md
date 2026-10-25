# P00-PWA-AUTH-WP-021C07B — guarded browser recovery routes

- Status: BLOCKED; Size: M; Owner: AI; Risk: HIGH
- Depends on: `P00-PWA-AUTH-WP-021C07A`.
- Add `POST /api/auth/web/forgot-password`, `reset-password`, and `reset-password-with-otp` through the same activation guard and request policy. Reuse the reviewed WordPress emailed-key and OTP helpers without changing mobile semantics. All error responses must use the API error contract; reset success must return no credentials and invalidate browser sessions.
- Exercise only disposable WordPress/MariaDB fixtures: unknown account generic behavior, invalid/replayed/expired reset and OTP input, password policy, session invalidation, object-cache/storage failure and no token/cookie leakage. Record evidence in `docs/evidence/P00-PWA-AUTH-WP-021C07B/`. No owner browser QA or production activation occurs in this card.
