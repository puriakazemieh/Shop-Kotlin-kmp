# P00-PWA-AUTH-WP-021C03 — isolated browser access-token evidence

Date: 2026-10-17. Scope: source-only token service; no hook, route, cookie, live network behavior, deployment or persistent WordPress database changes.

## Implemented boundary

- The token service checks the session-store site identity, derives a domain-separated per-site HS256 signing key from a 32-byte server-only master key, and issues a distinct `carmilla_web_access` token only for a currently active user/family. It contains no email, role, password or refresh credential; the visible family reference is not a refresh credential. The access-token lifetime is at most 900 seconds.
- Verification rejects altered/ambiguous/noncanonical tokens, invalid signature/algorithm/type/claims/time, wrong user/site/family, invalid password state, replay or logout, and unavailable store/clock. The session store reads fresh password state on every verification. Failure to issue throws a generic error without credentials.
- The optional authorization helper invokes a **trusted, fresh** server-side capability callback after successful verification; it never trusts a role claim. The real-WP test uses `user_can`. An integration must provide the appropriate capability and any object context and continue to enforce entitlement and operation-specific authorization. This helper is not wired to WordPress.
- `CB_Web_Session_Store::site_id()` is a read-only identity accessor for preventing store/site mismatches. Existing mobile JWT code and bearer hook are unchanged; their current hook will not authenticate these new browser tokens.
- Domain separation was tested even when the synthetic master key and mobile secret have the same value. Deployment must use a genuinely random master key; length checks cannot establish entropy. Deployment must not configure the derived browser signing key itself as the legacy mobile signing secret, and must never expose keys or JWTs in logs.

## Automated verification

The tests use **only** a disposable MariaDB 10.6.28 container, synthetic users, synthetic keys and temporary QA-prefixed tables. The user's WordPress installations and databases were not modified.

| Environment | Result |
|---|---|
| Synthetic session DB; Windows PHP 8.2.12 | 48 checks, 0 failures |
| Synthetic session DB; PHP 8.1 | 48 checks, 0 failures |
| Synthetic session DB; PHP 8.3 | 48 checks, 0 failures |
| Real WordPress 6.4 / PHP 8.1 in an isolated container | 8 checks, 0 failures |
| Real WordPress 6.6 / PHP 8.2 in an isolated container | 8 checks, 0 failures |
| Real WordPress 7.1.3 / PHP 8.3 in an isolated container | 8 checks, 0 failures |

The 48-check suite covers exact identity/type/time checks, modified and malformed claims and signatures, duplicate JSON keys, capability changes/errors, isolated mobile and browser signing, real storage, rotation/replay/logout/password/user deletion, and forced DB/clock/provider failures. The real-core checks include actual `user_can` and role change, `wp_set_password`, mobile isolation, logout and replay with synthetic WordPress tables. Tests pass locally with only fabricated data; these are not a pentest, real-browser login or compatibility verification for every hosting configuration.

PHP 8.2 lint passed for the store, token service and both test files. Existing `smoke.php`, `smoke-security.php` and the preceding 93-check request-policy suite passed. PHP 7.4 and WordPress versions outside the table remain unverified.

## Reproduction — disposable fixtures only

The tests create synthetic session tables or install synthetic WordPress tables and kill their own DB connection. **Never point them at an existing site or database.** Test passwords below are synthetic fixtures.

```powershell
docker run --detach --rm --name carmilla-token-qa-example --env MARIADB_ROOT_PASSWORD=synthetic_root_only_qa --env MARIADB_DATABASE=carmilla_token_qa --env MARIADB_USER=synthetic_qa --env MARIADB_PASSWORD=synthetic_qa_password --publish 127.0.0.1::3306 mariadb:10.6
docker port carmilla-token-qa-example 3306/tcp
# After readiness, set the reported port and use a new random lowercase 12-hex prefix.
$env:CARMILLA_TOKEN_TEST_PORT = '<assigned-loopback-port>'
$env:CARMILLA_TOKEN_TEST_PREFIX = 'qa_<12-lowercase-hex>_'
$env:CARMILLA_TOKEN_DISPOSABLE = 'yes'
& 'C:\xampp\php\php.exe' 'wordpress/carmilla-bridge/tests/smoke-web-access-token.php'
# Use a new prefix and substitute the absolute Bridge path for real WP core:
docker run --rm --network container:carmilla-token-qa-example --env CARMILLA_TOKEN_DISPOSABLE=yes --env CARMILLA_TOKEN_TEST_PREFIX=qa_<12-lowercase-hex>_ --mount 'type=bind,source=<absolute-bridge-path>,target=/work,readonly' --entrypoint php wordpress:6.6-php8.2-apache /work/tests/smoke-web-access-token-real-wp.php
docker stop carmilla-token-qa-example
```

The DB container above is explicitly disposable. Automatic tests do not prove that WordPress network routes, PWA client refresh, ingress/CSRF/TLS, production key configuration, or real device flows work. Migration and integration require `021C06`/`021C07` and owner QA, while the PWA end-to-end test remains `P00-PWA-AUTH-QA-023`.
