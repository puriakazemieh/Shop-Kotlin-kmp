# P00-T05 — PWA authentication QA finding and error-message repair

- **Date:** 2026-10-20
- **Environment:** retained local PWA and disposable local WordPress Docker
  environment
- **Data:** deliberately nonexistent synthetic identifier only; no password,
  OTP, access token, refresh token, or user data is recorded.

## Finding

The WordPress password endpoint returned HTTP `401` with error code
`BAD_CREDENTIALS`. The PWA recognized `INVALID_CREDENTIALS`, but not the
WordPress code, and consequently rendered the generic unknown-error message.

The OTP route is a mobile/JWT route. It can return a successful authentication
response, but the browser-session routes required for a durable PWA session
(`api/auth/web/login` and `api/auth/web/login-with-otp`) currently return HTTP
`404`. Therefore returning to the home screen after OTP does not prove that a
browser profile session exists.

## Repair and automated verification

The shared error mapper now maps the WordPress authentication error family to
localized user messages, including `BAD_CREDENTIALS`, invalid OTP, temporary
OTP unavailability, rate limiting, invalid session, registration failure, and
required authentication. Focused JVM tests and the JS compile passed. The
PWA local deployment was rebuilt from the development distribution with cache
version `v8`; its WordPress API configuration was preserved.

## Scope boundary

This repair does not claim a browser session implementation. The absent secure
cookie routes are the explicit scope of blocked task `P00-PWA-AUTH-WP-021`,
which requires route integration, CSRF/origin enforcement, cookie rotation,
revocation, and regression evidence before it can be enabled.

## Manual QA

After a hard reload, submit an intentionally incorrect password and confirm
the message states that the email or password is incorrect. Do not treat OTP
as a successful PWA login until the profile screen is authenticated and the
secure browser-session task is complete.
