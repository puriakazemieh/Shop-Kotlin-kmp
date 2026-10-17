# P00-PWA-AUTH-WP-021C04B03 — isolated identity safety prototype

- Status: READY; Size: M; Owner: AI; Risk: HIGH
- Depends on: `P00-PWA-AUTH-WP-021C04B02` and its narrowly scoped synthetic feasibility candidate; no production viability is asserted.
- Implement only the isolated identity primitive chosen by B02 and real synthetic MariaDB/WordPress tests for collisions, concurrency and failures. No live route/hook, migration, preserved-site or legacy mobile behavior change. If B02 requires a larger scope, split before implementation.
- Capture reproducible redacted evidence in `docs/evidence/P00-PWA-AUTH-WP-021C04B03/`; a prototype is not proof of production integration.
- Complete source-table INSERT, UPDATE, DELETE, mobile metadata, concurrency and failure coverage is required before this task can advance to migration. If those requirements exceed M, split into smaller sequential proofs before implementing; do not equate an insert-only experiment with the full primitive.
