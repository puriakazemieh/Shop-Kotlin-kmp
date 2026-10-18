# P00-PWA-AUTH-WP-021C04B03C02 — synthetic mobile identity concurrency and failure

- Status: TODO; Size: M; Owner: AI; Risk: HIGH
- Depends on: `P00-PWA-AUTH-WP-021C04B03C01`.
- Exercise the shared-reference prototype with real synthetic WordPress and independent SQL: conflicting owners across login/email/mobile, metadata value and user reassignment, duplicate meta rows, missing or invalid users, unsupported values, rollback, simultaneous add/update/delete and safe user deletion. Verify that a claim is not released while any source remains, transferred to the wrong owner or left inconsistent after failure. Capture both successful and rejected/deadlocked outcomes and resulting ownership.
- Account explicitly for WP return values, callbacks and side effects without claiming database rollback reverses external actions. Test only synthetic exact-binary values; do not treat `billing_phone` as a login, claim OTP replay protection, activate a route or migrate a real site.
- Evidence: `docs/evidence/P00-PWA-AUTH-WP-021C04B03C02/`. If the model cannot meet the original C acceptance or its coverage exceeds M, record the blocker rather than asserting C is complete.
