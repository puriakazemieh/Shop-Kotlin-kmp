# P00-PWA-AUTH-WP-021C02 — isolated browser request and cookie policy

- Status: TODO; Size: M; Owner: AI; Risk: HIGH
- Depends on: `P00-PWA-AUTH-WP-021C01`.
- Implement and test isolated policy helpers for trusted canonical origin and site identity, exact request-origin and JSON/custom-header checks, guarded HTTPS/proxy/local-QA handling, per-installation cookie naming and secure attributes as specified in `021A`. Fail closed on malformed/missing/untrusted values; do not infer trust from Host or forwarded headers. Include CORS/preflight decision tests without changing the existing CORS behavior yet.
- Do not register routes or hooks, send cookies or change live network behavior. Unit tests must cover hostile and valid origins, malformed/multiple/null headers, proxy spoofing, default-off localhost exception, different installations, and no production HTTP fallback. If the scope exceeds M, split before implementation.
- Record redacted evidence in `docs/evidence/P00-PWA-AUTH-WP-021C02/`. Owner network QA remains mandatory in `021C07`.
