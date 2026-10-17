# P00-PWA-AUTH-WP-021C — scope review and safe split

Date: 2026-10-17. This is a static scope review, not implementation, manual QA or a security approval.

## Evidence for the size and dependencies

- `docs/evidence/P00-PWA-AUTH-WP-021A/security-contract.md` calls for seven browser-only routes, server-side origin/CSRF/TLS checks, cookie lifecycle, password-state invalidation, family-bound access tokens, concurrency, compatibility and real-browser QA.
- `docs/evidence/P00-PWA-AUTH-WP-021B/verification.md` says the tested store is isolated and not loaded; its real WordPress dedicated connection, fresh password provider, migration and integration are still missing. PHP 7.4 was not verified.
- `wordpress/carmilla-bridge/includes/class-cb-auth-controller.php` registers separate mobile routes; its existing auth response contains a refresh token. Its OTP and reset methods have existing behavior that must not simply be reused as browser responses.
- `wordpress/carmilla-bridge/includes/class-cb-plugin.php` owns bearer authentication and CORS; its existing bearer path accepts the existing mobile access type and its CORS behavior does not itself provide the reviewed server-side browser-request guard.
- `wordpress/carmilla-bridge/includes/class-cb-jwt.php` has the existing mobile access/refresh JWT types and user-wide timestamp revocation, not the required browser family check.

## Decision

The integrated card exceeds M and explicitly requires a split. Eight dependent child cards cover: (1) safe WordPress store adapter; (2) request/cookie policy; (3) browser access-token binding; (4) isolated login/registration/OTP handlers; (5) isolated refresh/logout/reset handlers; (6) recoverable migration and owner QA; (7) complete protected integration and owner network QA; (8) parent reconciliation. Only child 01 is initially READY. A child that itself exceeds M must also be split.

No partially protected routes may be exposed while building children 01–06. No site, database, plugin, source code or secret was modified by this review. The two existing WordPress installations remain preserved. Child 07 tests the backend in an isolated fixture or after verified backup and is not end-to-end PWA evidence. The WEB-022 client lifecycle and QA-023 owner confirmation remain required before claiming PWA login works.
