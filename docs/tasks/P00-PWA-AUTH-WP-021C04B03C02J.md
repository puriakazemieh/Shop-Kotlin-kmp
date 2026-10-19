# P00-PWA-AUTH-WP-021C04B03C02J — inactive authorized deletion-incident notice

- Status: AWAITING_MANUAL_QA; Size: M; Owner: BOTH; Risk: HIGH
- Depends on: `P00-PWA-AUTH-WP-021C04B03C02I`. Owner authorized independent Phase 00 work while the deletion parent is blocked.
- Implement an inactive, explicitly callable administrator notice over the existing site-scoped incident store. Deny unauthorized callers before access; distinguish storage unavailable from no visible incident, show bounded escaped information, and never imply that detection is complete or recovery succeeded. Register no hooks, routes, migrations or changes on retained sites.
- Test real synthetic WordPress/MariaDB for authorization, empty list, observed incident, site isolation, bounded listing and failed storage; do not print synthetic account identifiers or raw DB errors. Record limitations and manual QA steps under `docs/evidence/P00-PWA-AUTH-WP-021C04B03C02J/`.
- UI work remains `AWAITING_MANUAL_QA` until a safely integrated synthetic fixture is available and the owner confirms it. The notice does not provide durable fallback, snapshot/recovery, complete detection or safe production identity migration.

## Automated work — 2026-10-19

- Implemented an inactive capability-guarded notice and ran nine checks on each of three synthetic real WordPress/PHP combinations. [Evidence and pending owner steps](../evidence/P00-PWA-AUTH-WP-021C04B03C02J/verification.md).
- No hook, dashboard or real site changed. Durable fallback, recovery and complete detection remain unresolved; the parent stays BLOCKED.
