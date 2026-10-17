# P00-PWA-AUTH-WP-021C06 — recoverable WordPress session migration

- Status: TODO; Size: M; Owner: BOTH; Risk: HIGH
- Depends on: `P00-PWA-AUTH-WP-021C05`.
- Implement and test a versioned, idempotent and fail-closed session-table migration with explicit recovery and compatibility handling. Test installation, repeated upgrade, incompatible/partial schema, and preservation of pre-existing synthetic WordPress data on an isolated WordPress/MariaDB fixture. No automatic data deletion, blind downgrade, production deployment or modifications to either preserved local WordPress site. Do not activate public web routes in this card.
- Document a concrete backup and recovery procedure before any future modification of the preserved QA sites. Record redacted, reproducible evidence in `docs/evidence/P00-PWA-AUTH-WP-021C06/`. If larger than M, split before implementation.
- Because this is a migration change, provide the owner exact fixture location, access steps, what to verify and success criteria; remain `AWAITING_MANUAL_QA` until confirmed. A disposable fixture must be distinguishable from the user's preserved sites.
