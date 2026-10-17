# P00-PWA-AUTH-WP-021C01 — WordPress session adapter evidence

Date: 2026-10-17. Isolated adapter only: no bootstrap inclusion, routes, cookies, WordPress migration or deployment. No real user/site database was changed.

## Implemented boundary and restrictions

- `class-cb-web-session-wp-adapter.php` requires an explicit trusted `CB_WEB_SESSION_SITE_URL` matching the WordPress site URL, an explicit 64-lowercase-hex `CB_WEB_SESSION_SECRET_HEX` derived from 32 cryptographically random bytes, a core `wpdb` connection and a normal single-site users table. Configuration must be supplied server-side by trusted deployment code, not HTTP input. The length and rejection of a repeated character cannot establish entropy: generating and protecting a genuinely random secret is a deployment prerequisite.
- It derives a stable canonical URL/blog identity and a domain-separated password-fingerprint key. It creates two dedicated, non-persistent mysqli connections in autocommit mode, verifies the existing WordPress connection is visible on the same DB server/database, and reads the current `user_pass` directly on the second connection rather than through the WP user cache or the store's transaction snapshot. Failures return generic exceptions without DB credentials, SQL values, password hashes or session credentials.
- The intentionally narrow supported DB transport is direct TCP to `127.0.0.1`, optionally with a port; Windows `localhost` is also accepted. A successful process-list identity check is required. Hostnames/remote DBs, IPv6, Unix sockets, persistent hosts, DB drop-ins, custom user tables, non-zero client flags, multisite and unverified TLS configurations fail closed. URLs requiring Unicode/percent-encoded paths, IPv6 literals or other unsupported canonicalization fail closed too. A loopback endpoint alone does not prove the ultimate DB is physically local or that a tunnel is encrypted; secure deployment topology is still required. These restrictions are not a claim that other configurations or production hosting are supported.
- The class is not wired to the Bridge and does not configure WordPress or a key. Shared-core/Theme-only support remains future work. Migration, guarded public routes, mobile/browser compatibility, CSRF/CORS/TLS deployment and end-to-end PWA behavior are not complete.

## Automated verification

The tests used disposable MariaDB 10.6.28 with a synthetic, non-root database account; all tables, triggers and users were test-only. The two existing XAMPP WordPress installations and their databases were not changed.

| Environment | Result |
|---|---|
| Windows XAMPP PHP 8.2.12 + real MariaDB, fake WordPress context, `127.0.0.1` | 33 checks, 0 failures |
| Same environment, Windows `localhost` | 33 checks, 0 failures |
| WordPress 6.4 / PHP 8.1 container + MariaDB | 7 checks, 0 failures |
| WordPress 6.6 / PHP 8.2 container + MariaDB | 7 checks, 0 failures |
| WordPress 7.1.3 / PHP 8.3 container + MariaDB | 7 checks, 0 failures |
| Installed WordPress 7.1.3 core mounted read-only into a PHP 8.2 container, with independent container `wp-content` and synthetic database | 7 checks, 0 failures |

The 33-check suite covers fresh password reads despite an old primary transaction snapshot, user deletion, invalid site/blog/port identity, bad headers, rejected DB hosts/flags/custom user tables/missing or trivial key, process-list/database mismatch, generic errors, killed password/store connections, and database failure. The seven real-core checks include `wp_set_password` invalidation, rotation and revocation. They do not prove a browser flow or that an actual customer's server is configured correctly.

PHP lint passed for the adapter and both test files on Windows PHP 8.2.12 and PHP 8.1 (adapter/test syntax); existing `tests/smoke.php` and `tests/smoke-security.php` passed on Windows PHP 8.2.12. WordPress 6.3, PHP 7.4, MySQL and production/TLS/shared-host/multisite combinations remain unverified.

## Reproduction — disposable fixtures only

The tests create and modify WordPress and session tables, insert synthetic users and deliberately kill their own database connections. **Never point them at an existing WordPress database.** The following root and user passwords are synthetic test fixtures, not real credentials.

```powershell
docker run --detach --rm --name carmilla-adapter-qa-example --env MARIADB_ROOT_PASSWORD=synthetic_root_only_qa --env MARIADB_DATABASE=carmilla_adapter_qa --env MARIADB_USER=synthetic_qa --env MARIADB_PASSWORD=synthetic_qa_password --publish 127.0.0.1::3306 mariadb:10.6
docker port carmilla-adapter-qa-example 3306/tcp
# Wait for MariaDB readiness and substitute its assigned loopback port.
$env:CARMILLA_ADAPTER_TEST_PORT = '<assigned-port>'
# Choose a new random lowercase 12-hex-character prefix for each run.
$env:CARMILLA_ADAPTER_TEST_PREFIX = 'qa_<12-lowercase-hex>_'
& 'C:\xampp\php\php.exe' 'wordpress/carmilla-bridge/tests/smoke-web-session-wp-adapter.php'
# For real WP core, substitute a fresh prefix and the absolute Bridge path.
docker run --rm --network container:carmilla-adapter-qa-example --env CARMILLA_ADAPTER_DISPOSABLE=yes --env CARMILLA_ADAPTER_TEST_PREFIX=qa_<12-lowercase-hex>_ --mount 'type=bind,source=<absolute-bridge-path>,target=/work,readonly' --entrypoint php wordpress:6.6-php8.2-apache /work/tests/smoke-web-session-wp-adapter-real-wp.php
docker stop carmilla-adapter-qa-example
```

The real-core check uses a container-owned WordPress core and independent test DB; the user's installed core was mounted read-only only for the additional explicitly stated version-matching check. WordPress admin email and passwords in the fixture are synthetic and mail is intercepted. No human UI/network/migration change is deployed by this card; `021C06`, `021C07`, `WEB-022` and `QA-023` retain the required migration, network and human end-to-end confirmations.
