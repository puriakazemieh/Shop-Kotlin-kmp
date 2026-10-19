# P00-PWA-AUTH-WP-021C04C02 — inactive atomic OTP consumption primitive

- Status: DONE; Size: M; Owner: AI; Risk: HIGH
- Depends on: `P00-PWA-AUTH-WP-021C04C01`. The owner authorized independent preparatory work while the identity dependency is blocked.
- Implement an **unregistered and unused** atomic consumption primitive for existing database-backed OTP transients. Reject unsupported persistent object caches and storage failures; do not alter the existing mobile or browser endpoints, send SMS or change preserved sites.
- On synthetic real WordPress and MariaDB, verify a correct unexpired code is consumed once, wrong and expired codes fail, concurrent independent processes that have both checked the same code produce at most one success, and failed storage and unsupported cache fail closed. Redact codes and secrets.
- Record limitations, including issuance, failed-attempt accounting, identity ambiguity, noncooperating writers and production integration, under `docs/evidence/P00-PWA-AUTH-WP-021C04C02/`. This child does not complete or activate `021C04C`.

## Completion — 2026-10-19

- Implemented an inactive consumer and exercised it against three real synthetic WordPress/PHP combinations. Both already-verified racing processes never consumed the same OTP; database failures and unsupported persistent cache failed closed. [Evidence and limitations](../evidence/P00-PWA-AUTH-WP-021C04C02/verification.md).
- No existing endpoint was changed; the legacy OTP race remains open. Issuance, limits, identity and integration remain prerequisites. The parent stays TODO.
