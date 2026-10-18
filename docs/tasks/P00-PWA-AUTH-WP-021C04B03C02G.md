# P00-PWA-AUTH-WP-021C04B03C02G — inactive, bounded deletion incident storage

- Status: DONE; Size: M; Owner: AI; Risk: HIGH
- Depends on: `P00-PWA-AUTH-WP-021C04B03C02F`. The owner authorized continuing independent Phase 00 work even when the blocked parent is not ready.
- Implement a small inactive server-side storage primitive for the already selected native deletion/incident approach: explicit schema definition only, installation-scoped records, minimal account reference, expiration, capability-guarded retrieval, bounded expiry cleanup and explicit failures. Do not register hooks, routes, notices or migrations, or touch an existing site.
- Exercise a real, disposable WordPress/MariaDB fixture with synthetic accounts for authorized and forbidden reads, site isolation, missing/expired records, write/read errors and expiry cleanup; verify neither records nor diagnostics contain metadata, passwords or contact details. Run syntax and disposable guard checks and record exact scope and results in `docs/evidence/P00-PWA-AUTH-WP-021C04B03C02G/`.
- A numeric account ID is linkable personal data. No production retention policy, durable outage fallback, privacy/legal approval, admin warning, recovery, WordPress deletion fix or parent acceptance is implied. This primitive cannot be activated on its own. If a safe scoped implementation is not feasible, record the blocker rather than claim success.

## Completion — 2026-10-18

- Implemented an inactive, narrowly scoped storage primitive and tested 21 assertions in each of three real synthetic WordPress/PHP environments. Lint and disposable guard passed; exact commands, security/privacy limits and results are in [verification.md](../evidence/P00-PWA-AUTH-WP-021C04B03C02G/verification.md).
- No route, migration, alert or recovery was enabled. The parent remains BLOCKED and the storage failure fallback is not solved.
