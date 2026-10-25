# P00-PWA-AUTH-WP-021C07B — guarded browser recovery routes

- Status: DONE; Size: M; Owner: AI; Risk: HIGH
- Depends on: `P00-PWA-AUTH-WP-021C07A`.
- Add `POST /api/auth/web/forgot-password`, `reset-password`, and `reset-password-with-otp` through the same activation guard and request policy. Reuse the reviewed WordPress emailed-key and OTP helpers without changing mobile semantics. All error responses must use the API error contract; reset success must return no credentials and invalidate browser sessions.
- Exercise only disposable WordPress/MariaDB fixtures: unknown account generic behavior, invalid/replayed/expired reset and OTP input, password policy, session invalidation, object-cache/storage failure and no token/cookie leakage. Record evidence in `docs/evidence/P00-PWA-AUTH-WP-021C07B/`. No owner browser QA or production activation occurs in this card.

## Completion — 2026-10-25

- The three recovery routes use the same explicit activation gate and request policy as the session routes, then delegate to the reviewed WordPress reset/OTP handlers. They always add `Cache-Control: no-store`; they do not issue a browser session or return credentials.
- Fresh disposable checks passed for all seven route registrations, generic unknown recovery, invalid reset, invalid OTP reset, rejected origin and mobile-controller independence. The existing real emailed-key lifecycle suite was rerun and passed, covering WordPress-core key validation, one-time consumption, credential-free reset and replay rejection. Storage/fingerprint invalidation behavior remains covered by C05's refresh/session suite.
