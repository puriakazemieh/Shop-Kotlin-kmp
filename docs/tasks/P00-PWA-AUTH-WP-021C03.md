# P00-PWA-AUTH-WP-021C03 — isolated browser access-token binding

- Status: DONE; Size: M; Owner: AI; Risk: HIGH
- Depends on: `P00-PWA-AUTH-WP-021C02`.
- Implement and test isolated issuance/validation of short-lived, distinctly typed browser access tokens bound to installation, user and active session family, including fresh password-state validation and server-side capability checks as appropriate. Reject malformed, expired, wrong-type, wrong-site, revoked, replayed and password-changed families; storage failure must fail closed. Use the approved `021A` contract.
- Do not alter the live bearer hook, register routes, or change existing mobile JWT behavior in this card. Prove the isolation with tests; test and preserve existing mobile contract. If the scope exceeds M, split before implementation.
- Record redacted evidence in `docs/evidence/P00-PWA-AUTH-WP-021C03/`. Actual bearer integration and owner network QA remain `021C07`.

## Completion record — 2026-10-17

- Added isolated web-token issuance/verification, store/site matching and fresh capability checking without changing the live bearer hook, mobile JWT or public routes.
- Synthetic real-database suite: 48/48 on PHP 8.1, 8.2 and 8.3. Synthetic WordPress core and database: 8/8 on each tested WordPress 6.4, 6.6 and 7.1.3 environment. Mobile isolation, real role/password change, replay, logout and failure paths are included.
- Evidence and deployment limitations: `docs/evidence/P00-PWA-AUTH-WP-021C03/verification.md`. Integration and owner network QA are still required in `021C07`; this does not establish working PWA login.
