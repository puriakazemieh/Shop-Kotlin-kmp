# P00-PWA-AUTH-WP-021B — WordPress browser-session infrastructure

- Status: DONE; Size: M; Owner: AI; Risk: HIGH
- Depends on: `P00-PWA-AUTH-WP-021A`.
- Implement only the reviewed server-side session storage, expiry, atomic rotation/replay handling and revocation primitives, with automated positive, negative and concurrency tests. Do not expose partially protected public routes. Preserve existing mobile behavior, WordPress installations, data and unrelated changes.
- Follow the approved `021A` contract; if the implementation exceeds M or its safety depends on an unresolved design choice, stop and propose smaller cards. Record reproducible redacted results under `docs/evidence/P00-PWA-AUTH-WP-021B/`.
- This isolated infrastructure card does not demonstrate a working PWA login and does not authorize deployment. Integration and manual QA remain pending.

## Completion record — 2026-10-17

- Implemented isolated browser-session storage primitives, with no bootstrap inclusion, public route or WordPress database changes.
- Two real disposable MariaDB runs passed 36/36 checks each, including concurrency, replay and transaction failure cases. PHP lint and legacy Bridge smoke checks passed; see [verification evidence](../evidence/P00-PWA-AUTH-WP-021B/verification.md) for versions, reproduction and limitations.
- No UI, network endpoint, migration or real WordPress deployment changed in this card, so it has no separate manual QA step. The next route-integration card must provide manual QA and cannot claim that PWA login works before the client and end-to-end QA cards.
