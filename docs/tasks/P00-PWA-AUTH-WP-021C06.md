# P00-PWA-AUTH-WP-021C06 — recoverable WordPress session migration

- Status: AWAITING_MANUAL_QA; Size: M; Owner: BOTH; Risk: HIGH
- Depends on: `P00-PWA-AUTH-WP-021C05`.
- Implement and test a versioned, idempotent and fail-closed session-table migration with explicit recovery and compatibility handling. Test installation, repeated upgrade, incompatible/partial schema, and preservation of pre-existing synthetic WordPress data on an isolated WordPress/MariaDB fixture. No automatic data deletion, blind downgrade, production deployment or modifications to either preserved local WordPress site. Do not activate public web routes in this card.
- Document a concrete backup and recovery procedure before any future modification of the preserved QA sites. Record redacted, reproducible evidence in `docs/evidence/P00-PWA-AUTH-WP-021C06/`. If larger than M, split before implementation.
- Because this is a migration change, provide the owner exact fixture location, access steps, what to verify and success criteria; remain `AWAITING_MANUAL_QA` until confirmed. A disposable fixture must be distinguishable from the user's preserved sites.

## Early isolated execution — 2026-10-18

The owner explicitly authorized independently executable Phase 00 work before READY and requested manual checks together at the end. The incomplete `021C05` remains a dependency for acceptance and route activation. Any early migration work must use an isolated synthetic database, remain inactive and must not bypass the dependency or human confirmation.

## Bounded automated result — 2026-10-18

An inactive, explicit version-1 installer was tested on four synthetic WordPress/PHP combinations, including real partial DDL failure and backup/restore to a separate synthetic database. [Evidence and limitations](../evidence/P00-PWA-AUTH-WP-021C06/verification.md); [recovery procedure](../evidence/P00-PWA-AUTH-WP-021C06/recovery.md). The persistent synthetic fixture and backup are separate named Docker volumes; owner manual QA and the `021C05` dependency remain outstanding. This is not deployment, an upgrade from a previous schema, or permission to touch a retained site.
