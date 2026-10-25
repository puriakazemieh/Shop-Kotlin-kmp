# P00-PWA-AUTH-WP-021C07B — guarded recovery routes

Date: 2026-10-25. The browser recovery endpoints are gated by the same explicit runtime configuration and request policy as the browser session endpoints. They delegate to the reviewed WordPress reset and OTP services; they add `Cache-Control: no-store` and never create a cookie, access token, refresh token or credential in their response.

On disposable WordPress 6.4.3/PHP 8.1.27 and isolated MariaDB, the route fixture passed 16 checks, including all seven route registrations, foreign-origin rejection, generic unknown recovery, invalid reset, invalid OTP reset, refresh/logout behavior and mobile-controller independence. The emailed-key lifecycle fixture was rerun successfully: one WordPress mail key, core validation, one-time consume, generic unknown account and credential-free reset/replay rejection. The latter is the success-path proof; the route fixture intentionally does not print or retain a reset key.

Real browser cookies, ingress duplicate-header behavior and the owner-visible network matrix remain C07C.
