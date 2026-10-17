# P00-PWA-AUTH-WP-021C — WordPress browser-session routes

- Status: BLOCKED; Size: >M after review; Owner: BOTH; Risk: HIGH
- Depends on: `P00-PWA-AUTH-WP-021C08`, which reconciles this parent after its children and owner QA.
- Integrate the reviewed secure session infrastructure with web login, registration, refresh and logout; address OTP where required by the reviewed contract. Enforce the origin/CSRF and secure-deployment rules, preserve mobile endpoints, and add positive and negative automated tests. If larger than M, stop and propose smaller cards.
- Do not deploy to production or modify the preserved local QA site without an appropriate backup. Record redacted evidence under `docs/evidence/P00-PWA-AUTH-WP-021C/`.
- Network changes require explicit manual QA: provide the owner the environment, exact steps and success criteria and keep `AWAITING_MANUAL_QA` until confirmed. Backend-only QA must not be presented as end-to-end PWA verification; that remains `P00-PWA-AUTH-QA-023` after the client lifecycle card.
- After evidence and required confirmation, reconcile the parent `P00-PWA-AUTH-WP-021` with the actual child outcomes before moving to `P00-PWA-AUTH-WEB-022`.

## Scope review — 2026-10-17

The card combines a real WordPress database adapter and migration, guarded cookie/origin/TLS policy, family-bound JWT authorization, seven browser routes including OTP and password reset, legacy-mobile compatibility, and human network/migration QA. The existing session class is not loaded by the plugin and has no WordPress connector. This exceeds M and must not be implemented as a single card or by prematurely exposing some routes.

The sequential child cards `021C01`–`021C08` split those responsibilities; only `021C01` is READY. `021C01`–`021C05` prepare isolated components without registering public endpoints. `021C06` handles the migration separately and requires human QA; `021C07` integrates the complete security boundary and requires human network QA. `021C08` reconciles this parent and `021` only after all required evidence and confirmations. The PWA client and end-to-end owner QA remain separate. See [scope evidence](../evidence/P00-PWA-AUTH-WP-021C/scope-review.md).
