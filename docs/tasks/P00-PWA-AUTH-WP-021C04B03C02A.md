# P00-PWA-AUTH-WP-021C04B03C02A — scoped synthetic deletion failure mitigation

- Status: DONE; Size: M; Owner: AI; Risk: HIGH
- Depends on: `P00-PWA-AUTH-WP-021C04B03C01`; starting evidence: `docs/evidence/P00-PWA-AUTH-WP-021C04B03C02/concurrency-and-failure.md`.
- Investigate and test a **test-only**, narrowly controlled deletion path on disposable real WordPress/MariaDB that addresses the reproduced metadata-delete failure. Explicitly map the path's supported and unsupported writers, transactional tables, return values, cache handling and callback/side-effect boundaries before choosing a prototype.
- On a synthetic user with no posts or reassignment, demonstrate both success and injected metadata-delete failure. For a claimed safe failure, verify the persisted user, metadata and identity references, the result reported to the controlled caller, and controlled notifications; record actual callbacks and any effects that cannot be reversed. Compare against the existing `wp_delete_user` failure baseline. Fail closed or retain a documented blocker when a case is unsupported; do not equate a SQL rollback with reversing external side effects.
- Include any necessary isolated concurrency or retry test within this narrow case. Do not disable arbitrary third-party callbacks globally or claim safety for uncooperative plugins, direct SQL writers, multisite, unrelated post deletion/reassignment, external services, or every WordPress deletion path. Do not patch WordPress core or deploy a route, schema, trigger, or change to a user's preserved sites.
- Evidence: `docs/evidence/P00-PWA-AUTH-WP-021C04B03C02A/`; include reproducible redacted commands, versions, findings and limitations. Passing this card does not by itself complete `021C04B03C02` or establish production readiness; the blocked card must be reassessed and its complete acceptance rerun separately. If a safe narrow prototype is not feasible within M, record the blocker rather than a false success.

## Result — 2026-10-18

- A controlled disposable-only prototype and native comparison passed on three real WordPress/PHP versions. The successful, failed, retry, unsupported-post and notification-failure cases are documented in [the evidence](../evidence/P00-PWA-AUTH-WP-021C04B03C02A/managed-deletion.md).
- This is not the WordPress native deletion lifecycle and does not fix it. Plugin hooks, unrelated writers, persistent caches, reassignment and external effects remain outside the experiment. No preserved site was changed.
