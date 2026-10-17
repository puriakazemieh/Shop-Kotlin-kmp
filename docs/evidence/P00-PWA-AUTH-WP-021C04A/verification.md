# P00-PWA-AUTH-WP-021C04A — verification

Date: 2026-10-17. Scope: isolated service, not a deployed endpoint or browser test. Synthetic WordPress installations and accounts were created only in a separate disposable MariaDB container; the preserved local sites and databases were not modified.

| Actual synthetic environment | Guarded login | Existing real-WP access-token regression |
|---|---:|---:|
| WordPress 6.4.3 / PHP 8.1.27 / MariaDB 10.6 | 19/19 | 8/8 |
| WordPress 6.6.2 / PHP 8.2.25 / MariaDB 10.6 | 19/19 | 8/8 |
| WordPress 7.1.3 / PHP 8.3.35 / MariaDB 10.6 | 19/19 | 8/8 |

`tests/smoke-web-login-real-wp.php` exercises actual `wp_authenticate` filters, guard-before-authentication, incorrect/ambiguous identities, changed password during authentication, family creation, cookie handoff and redaction, inaccessible user, changed installation, storage failure, post-commit failure and failed revocation. `tests/smoke-web-access-token-real-wp.php` covers existing token behavior. `tests/smoke-web-request-policy.php` passed 93/93 on PHP 8.1. PHP 8.1 lint passed for all three changed PHP implementation files. Test output contains names and counts only, no session values.

The handoff is a trusted internal object: JSON exposes no private properties; PHP serialization is refused, debug output is redacted and only an explicit one-time call retrieves the cookie for a future adapter. Trusted adapter code must not log or otherwise expose the returned value. If creation committed but cleanup cannot reach storage, an unreachable orphan family can remain; the result is a generic error and does not claim revocation. Any future cleanup must follow the bounded retention contract. Duplicate mobile identities fail closed rather than choosing an arbitrary account.

No route or live HTTP behavior was enabled. No real-browser test, public deployment, human network QA, PWA login or independent Theme-only functionality is claimed here. Those require later cards and explicit owner confirmation. Password/identifier handling and session safety remain subject to integration and independent security review.
