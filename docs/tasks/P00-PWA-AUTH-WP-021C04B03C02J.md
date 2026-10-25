# P00-PWA-AUTH-WP-021C04B03C02J — inactive authorized deletion-incident notice

- Status: BLOCKED; Size: M; Owner: BOTH; Risk: HIGH
- Depends on: `P00-PWA-AUTH-WP-021C04B03C02I`. Owner authorized independent Phase 00 work while the deletion parent is blocked.
- Implement an administrator notice over the existing site-scoped incident store. Deny unauthorized callers before access; distinguish storage unavailable from no visible incident, show bounded escaped information, and never imply that detection is complete or recovery succeeded. Wire it only through the Bridge runtime with its fixed schema migration; do not change retained sites.
- Test real synthetic WordPress/MariaDB for authorization, empty list, observed incident, site isolation, bounded listing and failed storage; do not print synthetic account identifiers or raw DB errors. Record limitations and manual QA steps under `docs/evidence/P00-PWA-AUTH-WP-021C04B03C02J/`.
- UI work remains `AWAITING_MANUAL_QA` until a safely integrated synthetic fixture is available and the owner confirms it. The notice does not provide durable fallback, snapshot/recovery, complete detection or safe production identity migration.

## Automated work — 2026-10-19

- Implemented an inactive capability-guarded notice and ran nine checks on each of three synthetic real WordPress/PHP combinations. [Evidence and pending owner steps](../evidence/P00-PWA-AUTH-WP-021C04B03C02J/verification.md).
- The Bridge runtime now wires the observer, fixed incident-schema migration and administrator notice; all three disposable WordPress/PHP combinations passed. No retained site changed. Durable fallback, recovery and complete detection remain unresolved; the parent stays BLOCKED and this UI/network work remains `AWAITING_MANUAL_QA`.

## Status reconciliation — 2026-10-25

This card cannot be owner-tested yet: its required separately integrated synthetic administrator fixture and storage-failure control do not exist. It is therefore BLOCKED, not awaiting a test the owner cannot execute.
