# P00-PWA-AUTH-WP-021C04B03C02H — inactive native-deletion incident observer

- Status: DONE; Size: M; Owner: AI; Risk: HIGH
- Depends on: `P00-PWA-AUTH-WP-021C04B03C02G`, `P00-PWA-AUTH-WP-021C04B03C02C`. The owner authorized independent Phase 00 work even if the parent is not ready.
- Implement an inactive observer that can be called around native WordPress deletion without changing native hooks, core or return values. In trusted process memory, compare a keyed digest of pre/post synthetic user metadata without persisting raw metadata. If the account survives, record an accurately classified incident using G's store; distinguish partial metadata change from a surviving unchanged account. No hooks, routes, UI or migration may be registered by the product class.
- Test the class by attaching it only in disposable synthetic real WordPress fixtures to native hooks: ordinary deletion, injected partial deletion, surviving unchanged user, missing begin event, storage/query failure, and existing plugin-like listener behavior. Record limitations and exact tests under `docs/evidence/P00-PWA-AUTH-WP-021C04B03C02H/`.
- Do not claim durable fallback, complete detection under crashes/concurrency/direct SQL/multisite, privacy policy, notification, recovery or parent acceptance. Retain native WordPress behavior and all preserved sites. If the scope or correctness cannot be demonstrated, record a blocker instead of declaring success.

## Completion — 2026-10-18

- Inactive observer and fixed survivor categories passed 13 observer assertions in each of three isolated real WordPress/PHP combinations, with the base synthetic identity fixture and a 21-check storage regression. PHP 7.4 lint and disposable guard passed. [Evidence and limitations](../evidence/P00-PWA-AUTH-WP-021C04B03C02H/verification.md).
- The fixture alone registers hooks; the product does not. No alert, fallback, recovery or complete deletion safety exists. Parent and P00 Gate remain blocked.
