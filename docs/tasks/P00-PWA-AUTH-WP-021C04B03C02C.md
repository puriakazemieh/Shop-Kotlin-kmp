# P00-PWA-AUTH-WP-021C04B03C02C — native deletion anomaly detection feasibility

- Status: DONE; Size: M; Owner: AI; Risk: HIGH
- Depends on: `P00-PWA-AUTH-WP-021C04B03C02B`. The owner selected retaining standard WordPress deletion and plugin compatibility, with failure detection and recovery, while acknowledging WP may show false success on database failure.
- In disposable real WordPress/MariaDB only, test a narrowly scoped observer around the **native** `wp_delete_user` lifecycle without modifying core or suppressing hooks. Demonstrate the existing synthetic metadata-delete failure is detectable as a surviving partially changed user even when WP returns true; demonstrate a successful eligible deletion produces no false incident. Record callback ordering and persisted state with redacted evidence. Distinguish DB/read errors or observer failures from a confirmed clean deletion; no credentials, personal information or real account metadata in evidence.
- Explicitly test/describe limitations: hook ordering and missed hooks, direct SQL, worker/process crash, concurrency, multisite, third-party callbacks, posts/reassignment, caches, DB availability, persistence and no guarantee of immediate detection. Do not claim observer callbacks or SQL rollback reverse external side effects. Do not deploy code, schema, routes or triggers on preserved sites.
- This card is detection feasibility only, **not** a completed recovery workflow, production integration, safe registration, or full parent acceptance. Based on results, record a concrete next bounded recovery task and leave the parent blocked until its full validation succeeds. Evidence: `docs/evidence/P00-PWA-AUTH-WP-021C04B03C02C/`. If the narrow observer cannot meet its test claims, record the blocker rather than false success.

## Result — 2026-10-18

- Native deletion observation, injected failure, successful delete, query failure, caught exception, missing before-event and direct SQL bypass were tested against three real WP versions in isolated synthetic databases. Evidence: [native-deletion-observer.md](../evidence/P00-PWA-AUTH-WP-021C04B03C02C/native-deletion-observer.md).
- This test-only observer has no durable alert or recovery and was not deployed. The next bounded recovery experiment is `021C04B03C02D`; parent remains blocked.
