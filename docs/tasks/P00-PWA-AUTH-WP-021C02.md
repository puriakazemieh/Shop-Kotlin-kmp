# P00-PWA-AUTH-WP-021C02 — isolated browser request and cookie policy

- Status: DONE; Size: M; Owner: AI; Risk: HIGH
- Depends on: `P00-PWA-AUTH-WP-021C01`.
- Implement and test isolated policy helpers for trusted canonical origin and site identity, exact request-origin and JSON/custom-header checks, guarded HTTPS/proxy/local-QA handling, per-installation cookie naming and secure attributes as specified in `021A`. Fail closed on malformed/missing/untrusted values; do not infer trust from Host or forwarded headers. Include CORS/preflight decision tests without changing the existing CORS behavior yet.
- Do not register routes or hooks, send cookies or change live network behavior. Unit tests must cover hostile and valid origins, malformed/multiple/null headers, proxy spoofing, default-off localhost exception, different installations, and no production HTTP fallback. If the scope exceeds M, split before implementation.
- Record redacted evidence in `docs/evidence/P00-PWA-AUTH-WP-021C02/`. Owner network QA remains mandatory in `021C07`.

## Completion record — 2026-10-17

- Added isolated origin, request, direct TLS/proxy, bounded local QA, CORS/preflight and cookie-option decisions. No route, hook, actual header or cookie behavior was changed.
- Policy smoke tests: 93/93 passed on PHP 8.1, 8.2 and 8.3; existing Bridge smoke tests also passed on 8.2. Evidence and integration limitations: `docs/evidence/P00-PWA-AUTH-WP-021C02/verification.md`.
- Real proxy/TLS, full duplicate-header rejection at ingress, collision detection across the authoritative installation namespace, browser cookie acceptance and owner network QA remain integration prerequisites; this card does not claim a working PWA login or production readiness.
