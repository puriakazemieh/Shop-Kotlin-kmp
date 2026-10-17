# P00-PWA-AUTH-WP-021C04A — isolated guarded password login

- Status: DONE; Size: M; Owner: AI; Risk: HIGH
- Depends on: `P00-PWA-AUTH-WP-021C03`.
- Implement and test a non-routed password login service and common session/token issuance handoff. Check `021C02` policy before authentication or mutation; authenticate via real `wp_authenticate` and filters. Return the compatible public user/access-token body but never expose refresh credentials or separate session identifiers in JSON, URLs, diagnostics or logs; keep any cookie handoff isolated and non-serializable.
- Handle incorrect input, hostile requests, WP filter rejection, expired/missing user and session/token/storage failures without issuing usable credentials. If a session was created before a later failure, attempt safe revocation and report storage failure accurately; do not delete user data. Leave mobile endpoints, hooks, live network behavior and both preserved WordPress installations untouched.
- Test with actual WordPress core and isolated synthetic MariaDB, plus negative/failure and response-redaction cases. Record redacted evidence in `docs/evidence/P00-PWA-AUTH-WP-021C04A/`. No public route or owner network QA is performed; that remains `021C07`.

## Completion record — 2026-10-17

- Added isolated, guarded password login and a reusable session/token issuer with a one-time non-serializable cookie handoff; no route, live hook, preserved site or mobile endpoint was changed.
- Actual synthetic WordPress/MariaDB: 19/19 checks each on WordPress 6.4.3/PHP 8.1, 6.6.2/PHP 8.2 and 7.1.3/PHP 8.3. The previous real WordPress token suite also passed 8/8 on each; request-policy regression passed 93/93.
- The authenticated password hash is checked against fresh database state before creating a family. Later token verification still checks that state. On cleanup failure, the service returns an error without claiming revocation succeeded.
- Evidence and remaining limitations: `docs/evidence/P00-PWA-AUTH-WP-021C04A/verification.md`. Browser integration, network QA, and all other authentication operations remain separate tasks.
