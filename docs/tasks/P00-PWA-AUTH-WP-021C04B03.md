# P00-PWA-AUTH-WP-021C04B03 — isolated identity safety prototype

- Status: BLOCKED; Size: exceeds M after scope review; Owner: AI; Risk: HIGH
- Depends on: `P00-PWA-AUTH-WP-021C04B03D`; its child chain begins after `021C04B02` and has only a narrowly scoped synthetic feasibility candidate, not a production design.
- Implement only the isolated identity primitive chosen by B02 and real synthetic MariaDB/WordPress tests for collisions, concurrency and failures. No live route/hook, migration, preserved-site or legacy mobile behavior change. If B02 requires a larger scope, split before implementation.
- Capture reproducible redacted evidence in `docs/evidence/P00-PWA-AUTH-WP-021C04B03/`; a prototype is not proof of production integration.
- Complete source-table INSERT, UPDATE, DELETE, mobile metadata, concurrency and failure coverage is required before this task can advance to migration. If those requirements exceed M, split into smaller sequential proofs before implementing; do not equate an insert-only experiment with the full primitive.

## Scope review — 2026-10-17

The B02 candidate requires independent proof of user insertion, user updates/deletions, mobile metadata and combined WordPress concurrency/failure behavior. These are distinct transactional mechanisms and cannot safely be treated as one M task. Proposed children `021C04B03A`–`021C04B03D` are recorded in sequence; the parent stays BLOCKED and no migration is authorized by the split. Evidence: `docs/evidence/P00-PWA-AUTH-WP-021C04B03/scope-review.md`.
