# P00-PWA-AUTH-WP-021C04B03C02E — durable synthetic deletion incident

- Status: DONE; Size: M; Owner: AI; Risk: HIGH
- Depends on: `P00-PWA-AUTH-WP-021C04B03C02C` and `P00-PWA-AUTH-WP-021C04B03C02D`. Preserve native WordPress deletion and do not claim its false-success message or external effects are fixed.
- On disposable WordPress/MariaDB with synthetic accounts only, investigate a narrowly scoped persistent, redacted incident record for an observed partial native deletion. Test a successful detection, clean deletion, duplicate/retry handling, and readback from an independent process after the detecting process ends. Explicitly test failed record writing or inaccessible database and report inability to record as unverified; never classify it as clean. Do not store user metadata, credentials, phone, email, tokens, or recovery snapshots in the incident record or evidence.
- Establish exactly when this record can and cannot be persisted and distinguish observed synthetic process termination from an untested crash during a hook. Do not claim delivery of an administrator warning, automatic recovery, plugin compatibility, all-writer coverage or crash durability without evidence. Do not alter preserved sites, install a production table, migrate a schema, expose a route or send external notifications.
- Evidence: `docs/evidence/P00-PWA-AUTH-WP-021C04B03C02E/`. Authorized operator access, retention/privacy, durable recovery snapshots, alert delivery and production integration are separately gated. C02 and its parent remain BLOCKED until their complete acceptance matrix passes.

## Result — 2026-10-18

- Real synthetic WordPress on three PHP/WP combinations recorded redacted partial-deletion incidents and independently read them after the writer exited; duplicate and injected write-failure cases were checked. [Evidence](../evidence/P00-PWA-AUTH-WP-021C04B03C02E/synthetic-incident.md) distinguishes process persistence from crash/database-restart durability.
- Write failure leaves `UNVERIFIED` only in memory; no durable fallback, administrator warning, account correlation, authorization or real recovery exists. Native WP false success and external effects remain. The parent stays BLOCKED and this fixture has no human UI QA step.
