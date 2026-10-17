# P00-PWA-AUTH-WP-021C04B02 — identity safety contract

- Status: READY; Size: M; Owner: AI; Risk: HIGH
- Depends on: `P00-PWA-AUTH-WP-021C04B01`.
- Using B01 evidence, specify a concrete, reviewable registration/identity safety contract including supported and unsupported writer paths, canonicalization, conflict handling, atomicity, partial creation, credential lifecycle, duplicate remediation, compatibility, rollback and deployment prerequisites. Do not assume uncooperative third-party writes are covered by a local lock.
- Compare alternatives, state their limits and select or explicitly block the narrowest verifiable approach; do not claim production readiness or make a schema change here. Respect future Theme/Plugin independence and preserve existing installations and accounts.
- Evidence in `docs/evidence/P00-PWA-AUTH-WP-021C04B02/`. If the architecture requires unresolved product authority or scope exceeds M, stop and report rather than guessing.
