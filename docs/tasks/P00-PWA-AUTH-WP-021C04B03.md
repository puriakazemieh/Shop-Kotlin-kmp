# P00-PWA-AUTH-WP-021C04B03 — isolated identity safety prototype

- Status: TODO; Size: M; Owner: AI; Risk: HIGH
- Depends on: `P00-PWA-AUTH-WP-021C04B02` and a demonstrably viable, sufficiently narrow B02 contract.
- Implement only the isolated identity primitive chosen by B02 and real synthetic MariaDB/WordPress tests for collisions, concurrency and failures. No live route/hook, migration, preserved-site or legacy mobile behavior change. If B02 requires a larger scope, split before implementation.
- Capture reproducible redacted evidence in `docs/evidence/P00-PWA-AUTH-WP-021C04B03/`; a prototype is not proof of production integration.
