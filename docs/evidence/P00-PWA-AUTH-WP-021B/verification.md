# P00-PWA-AUTH-WP-021B — isolated session-store verification

Date: 2026-10-17. Scope: Bridge session primitives only; no public route, WordPress migration or deployment.

## Implementation

- `wordpress/carmilla-bridge/includes/class-cb-web-session-store.php` provides explicit DDL, digest-only refresh storage, per-site/user/family binding, fixed absolute and idle expiry, family locking and one-use rotation, replay revocation, logout/user revocation, password-state fingerprint checks and bounded expiry cleanup. The class is not included by the plugin bootstrap.
- The caller must supply a **dedicated, non-persistent mysqli connection with no automatic reconnect**, not a shared `wpdb` connection, and a secure server-only key. The password provider must read the current persisted hash without stale WP caches or transaction snapshots, using a separate fresh connection or an appropriate locking read. Neither the connection factory nor the real WordPress password provider exists yet; those are integration prerequisites, not verified features.
- Table/schema verification fails closed when transactional InnoDB tables, required columns or unique primary keys are unavailable. It requires `mysqli`/mysqlnd and information-schema access; supported hosting and least-privilege requirements must be checked during integration.
- The server returns a replacement only after a successful commit. If a commit result is lost after the server has actually committed, its outcome is inherently ambiguous: no replacement is returned, and a user may need to sign in again. The tests below do not claim that such a commit was rolled back.

## Reproducible automated tests

Tested with XAMPP PHP CLI 8.2.12 and an isolated disposable `mariadb:10.6` container, MariaDB 10.6.28, bound to loopback. The database `carmilla_session_qa` and all identities are synthetic and contain no WordPress data. Do not point the test at an existing site. The test deliberately creates tables and triggers and kills its own database connection; run it only in a fresh disposable container. The initial run used loopback port `58721`; use the actual allocated port when reproducing.

```powershell
docker run --detach --rm --name carmilla-session-qa-example --env MARIADB_ALLOW_EMPTY_ROOT_PASSWORD=1 --env MARIADB_DATABASE=carmilla_session_qa --publish 127.0.0.1::3306 mariadb:10.6
docker port carmilla-session-qa-example 3306/tcp
# Wait until MariaDB is ready and substitute the loopback port reported above.
$env:CARMILLA_TEST_DB_PORT = '<allocated-port>'
# Use a new random 12-character lowercase hexadecimal prefix for every run.
$env:CARMILLA_TEST_TABLE_PREFIX = 'qa_<12-lowercase-hex>_'
& 'C:\xampp\php\php.exe' 'wordpress/carmilla-bridge/tests/smoke-web-session-store.php'
# Remove only the disposable synthetic test container, never a WordPress database.
docker stop carmilla-session-qa-example
```

Two independent final runs with distinct prefixes each returned **36 checks, 0 failures**. These include real concurrent worker processes for refresh/replay, refresh/logout and refresh/password-change races; an actual database connection killed during a transaction; injected insertion and pre-COMMIT connection failures; expiry and clock checks; scoped logout/user revocation; site isolation; digest-only persistence; invalid schema/index/engine refusal; and RNG/key failure. The injected commit fault closes the client connection before COMMIT reaches the server; it does not simulate an ambiguous response after a successful server commit. Credentials used by the concurrency workers are synthetic and passed through pipes, not printed to the test output.

Additional checks:

```text
PHP 8.2.12: php -l class-cb-web-session-store.php  PASS
PHP 8.2.12: php -l smoke-web-session-store.php    PASS
PHP 8.1-cli: php -l both files                    PASS
PHP 8.2.12: tests/smoke.php                       ALL PASSED
PHP 8.2.12: tests/smoke-security.php              ALL PASSED
```

The legacy smoke test covers existing JWT behavior in isolation; it is not a live-device or full WordPress compatibility test. These results are not a pentest and do not prove production readiness, real WordPress schema migration, HTTP/CSRF/CORS/cookie security, browser login, mobile end-to-end behavior, or Theme-only support. The attempt to obtain `php:7.4-cli` for linting failed with a truncated Docker image download; PHP 7.4 remains unverified. Other database/host combinations also require separate verification if not explicitly recorded here.

## Integration and manual QA still pending

`P00-PWA-AUTH-WP-021C` must safely construct and verify the dedicated connection and fresh password provider, implement a reviewed migration with recovery/backup and fail-closed handling, preserve the existing WordPress installations, and implement/test the route and security contract from `021A`. The route card is explicitly required to split if it exceeds size M. Its network changes require owner manual QA. `P00-PWA-AUTH-WEB-022` and `P00-PWA-AUTH-QA-023` remain necessary for a real PWA login and user confirmation. This infrastructure-only change needs no separate owner UI test.
