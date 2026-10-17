# P00-PWA-AUTH-WP-021C03 — isolated browser access-token binding

- Status: TODO; Size: M; Owner: AI; Risk: HIGH
- Depends on: `P00-PWA-AUTH-WP-021C02`.
- Implement and test isolated issuance/validation of short-lived, distinctly typed browser access tokens bound to installation, user and active session family, including fresh password-state validation and server-side capability checks as appropriate. Reject malformed, expired, wrong-type, wrong-site, revoked, replayed and password-changed families; storage failure must fail closed. Use the approved `021A` contract.
- Do not alter the live bearer hook, register routes, or change existing mobile JWT behavior in this card. Prove the isolation with tests; test and preserve existing mobile contract. If the scope exceeds M, split before implementation.
- Record redacted evidence in `docs/evidence/P00-PWA-AUTH-WP-021C03/`. Actual bearer integration and owner network QA remain `021C07`.
