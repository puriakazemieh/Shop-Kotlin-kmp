# P02-QA-CODE-007A — smoke inventory and disposable fixture contract

Baseline: `077e1ebf`, 2026-10-31, repo root `D:\Android\AndroidStudioProjects\kmp-shop`. This inventory covers exactly the 37 files matching `wordpress/carmilla-bridge/tests/smoke*.php`. It does not claim that the parent WordPress/Woo/PHP CI matrix has run.

## Invocation rule and classifications

For the tables below, `T` is the filename in the first column. Unless a row says `wp eval-file`, the exact test invocation **inside the declared container/fixture** is `php tests/T` from `/app/wordpress/carmilla-bridge` (or the equivalent mounted Bridge root). Expected exit is **0** and test-specific PASS output. An unmet disposable guard exits **2**, and an assertion failure must return nonzero. The 14 hermetic tests below were actually run with:

```powershell
docker run --rm --network none -v "${PWD}:/app:ro" -w /app/wordpress/carmilla-bridge php:8.1-cli php tests/T
```

Here `T` must be replaced by exactly one filename below. `--network none` and a read-only source mount prevent accidental access to a live site. No real-WP or database test may be launched with this hermetic command plus opt-in flags: it needs the isolated fixtures described below.

### Hermetic PHP smoke tests — no WordPress or database (14)

| T (run `php tests/T`) | Reason / fixture |
|---|---|
| `smoke.php` | WP stubs; JWT and block mapping |
| `smoke-legacy-migration.php` | in-memory option/theme-mod stubs |
| `smoke-manifest-security.php` | fake request and feature-off guard |
| `smoke-manifest.php` | manifest resolver and dependency validation |
| `smoke-messaging.php` | messaging stubs |
| `smoke-p07-seed-migration.php` | self-contained migration primitives |
| `smoke-phase2.php` | WP/Woo stubs, pure commerce helpers |
| `smoke-phase3.php` | pure academy helpers |
| `smoke-phase4.php` | pure clinic/psych helpers |
| `smoke-phase5.php` | pure membership/referral helpers |
| `smoke-phase6.php` | admin content stubs |
| `smoke-phase7.php` | option stub and shared logic |
| `smoke-security.php` | synthetic security stubs |
| `smoke-web-request-policy.php` | pure policy decisions |

### Disposable MariaDB tests without a WordPress bootstrap (4)

| T (run `php tests/T`) | Required environment and database | Side effects / constraints |
|---|---|---|
| `smoke-session-migration-ddl-fault.php` | `CARMILLA_ADAPTER_DISPOSABLE=yes`, `CARMILLA_ADAPTER_TEST_PREFIX=qa_<12 hex>_`; `carmilla_adapter_qa`, PHP `mysqli`, `/work/includes`, DB at `127.0.0.1:3306` | deliberately exercises DDL failure with limited `synthetic_fault` user; fixture must be discarded |
| `smoke-web-access-token.php` | `CARMILLA_TOKEN_DISPOSABLE=yes`, `CARMILLA_TOKEN_TEST_PREFIX=qa_<12 hex>_`, `CARMILLA_TOKEN_TEST_PORT=<loopback port>`; `carmilla_token_qa`, `mysqli` | creates/revokes synthetic token rows; never use a retained DB |
| `smoke-web-session-store.php` | `CARMILLA_TEST_TABLE_PREFIX=qa_<12 hex>_`, `CARMILLA_TEST_DB_PORT=<loopback port>`; `carmilla_session_qa`, `mysqli` | concurrency/data writes; uses an empty local root DB user, so container isolation is mandatory |
| `smoke-web-session-wp-adapter.php` | `CARMILLA_ADAPTER_TEST_PREFIX=qa_<12 hex>_`, `CARMILLA_ADAPTER_TEST_PORT=<loopback port>`; `carmilla_adapter_qa`, `mysqli` | synthetic adapter tables; do not point at WP data |

### WordPress or WordPress+Woo smoke tests (19)

The command is `php tests/T` in an isolated PHP/WP container except the two `wp eval-file` rows. `LOGIN` means `CARMILLA_LOGIN_DISPOSABLE=yes` and `CARMILLA_LOGIN_TEST_PREFIX=qa_<12 hex>_`; `ADAPTER` means `CARMILLA_ADAPTER_DISPOSABLE=yes` and `CARMILLA_ADAPTER_TEST_PREFIX=qa_<12 hex>_`; `TOKEN` means `CARMILLA_TOKEN_DISPOSABLE=yes` and `CARMILLA_TOKEN_TEST_PREFIX=qa_<12 hex>_`. Each abbreviation is an exact environment assignment, not a production default.

| T | Required environment / bootstrap | Side effects or special command |
|---|---|---|
| `smoke-auth-error-contract-real-wp.php` | `CARMILLA_LOGIN_DISPOSABLE=yes`; `/var/www/html/wp-load.php` | creates synthetic identity; separate disposable install |
| `smoke-deletion-incident-notice-real-wp.php` | LOGIN; `/usr/src/wordpress/`, `carmilla_login_qa`, `/work/includes` | installs QA tables/users and tests incident mail interception |
| `smoke-deletion-incident-observer-real-wp.php` | LOGIN through `experiment-identity-shared-references.php`; `/usr/src/wordpress/`, `carmilla_login_qa`, `/bridge/includes` | installs QA tables/users and a temporary delete-fault trigger |
| `smoke-deletion-incident-runtime-real-wp.php` | LOGIN through the shared fixture; `/usr/src/wordpress/`, `carmilla_login_qa`, `/work/includes` | runs bounded schema migration and fault trigger |
| `smoke-deletion-incident-store-real-wp.php` | LOGIN; `/usr/src/wordpress/`, `carmilla_login_qa`, `/work/includes` | installs QA tables/users |
| `smoke-identity-exact-audit-real-wp.php` | LOGIN; `/usr/src/wordpress/`, `carmilla_login_qa`, `/work/includes` | creates conflicting synthetic users/identity rows |
| `smoke-identity-supported-writer-real-wp.php` | LOGIN; `/usr/src/wordpress/` or `CARMILLA_LOGIN_WP_ROOT=<installed WP root>`, `carmilla_login_qa` | creates synthetic users/writer schema |
| `smoke-identity-synthetic-migration-real-wp.php` | `CARMILLA_LOGIN_DISPOSABLE=yes`; `/var/www/html/wp-load.php` | deletes only its synthetic prior users, then writes migration fixture |
| `smoke-local-otp-sink-real-wp.php` | `CARMILLA_LOGIN_DISPOSABLE=yes`; `/var/www/html/wp-load.php` | toggles local QA option and creates synthetic login flow |
| `smoke-managed-user-deletion-real-wp.php` | LOGIN through shared fixture; `/usr/src/wordpress/`, `carmilla_login_qa` | runs synthetic user deletion cases |
| `smoke-p07-wordpress.php` | installed disposable WP with Bridge; WP CLI | **`wp eval-file wordpress/carmilla-bridge/tests/smoke-p07-wordpress.php`** from repository root; writes/deletes synthetic posts/mapping; has no disposable env guard, so never run against a retained site |
| `smoke-web-access-token-real-wp.php` | TOKEN; `/usr/src/wordpress/`, `carmilla_token_qa`, `/work/includes` | creates token/session tables |
| `smoke-web-auth-route-activation-real-wp.php` | LOGIN; `/usr/src/wordpress/`, `carmilla_login_qa`, `/work/includes` | creates schema and checks activation; no HTTP server |
| `smoke-web-login-real-wp.php` | LOGIN; `/usr/src/wordpress/`, `carmilla_login_qa`, `/work/includes` | creates synthetic login schema/users |
| `smoke-web-refresh-logout-real-wp.php` | LOGIN; `/usr/src/wordpress/`, `carmilla_login_qa`, `/work/includes` | creates refresh family and logout fixture |
| `smoke-web-registration-real-wp.php` | `CARMILLA_LOGIN_DISPOSABLE=yes`; `/var/www/html/wp-load.php` | creates synthetic registration user |
| `smoke-web-session-migration-real-wp.php` | ADAPTER; `/usr/src/wordpress/`, `carmilla_adapter_qa`, `/work/includes` | creates schema and synthetic preservation sentinel |
| `smoke-web-session-wp-adapter-real-wp.php` | ADAPTER; `/usr/src/wordpress/`, `carmilla_adapter_qa`, `/work/includes` | installs synthetic WP tables |
| `smoke-woo-reference.php` | `CARMILLA_REFERENCE_DISPOSABLE=yes`, `CARMILLA_REFERENCE_PREFIX=qa_<12 hex>_`; installed WP+Theme+Bridge+Woo | **`wp eval-file wordpress/carmilla-bridge/tests/smoke-woo-reference.php`** from repository root; creates a synthetic Woo product and requires `WC_VERSION === 8.5.2`, so it cannot serve as an unrestricted Woo-latest cell |

## Fixture and cleanup contract for 007B/007C

1. Use a new disposable container/network and database per matrix cell; never point any test at a retained WordPress site. Provision only `carmilla_login_qa`, `carmilla_adapter_qa`, `carmilla_token_qa`, `carmilla_session_qa` as needed. A test needing `127.0.0.1:3306` must share that network namespace with its database or be changed in its own scoped child after a characterization test.
2. Generate a fresh `qa_` prefix with exactly 12 lowercase hex characters and a trailing underscore per test or isolated fixture. Set only that test's required `*_DISPOSABLE=yes` flag. Do not set all flags globally. Use synthetic `example.test` accounts and documentation-only IP ranges. Intercept mail/payment/OTP outputs; no external network destination.
3. Mount the Bridge at `/work` or `/bridge` as required and WordPress at the literal path each test expects. The split between `/usr/src/wordpress/` and `/var/www/html/` is a current incompatibility; 007B/007C must resolve it deliberately, not silently skip tests.
4. On success or failure, capture command, PHP/WP/Woo versions, prefix shape (not identifying values), exit code and redacted log. Stop on nonzero. Destroy only the disposable cell's containers, volumes and databases after evidence collection; do not run cleanup against any host DB. The unguarded `smoke-p07-wordpress.php` must be isolated by orchestration.
5. Run the four guard-negative probes without flags first. Expected exit 2. This prevents a CI change from accidentally running a write-capable smoke against an unqualified environment.

## Executed verification

- `docker info --format '{{.ServerVersion}}'`: exit 0, Docker 29.2.1.
- `docker image inspect php:8.1-cli`: exit 0, image present.
- All 37 `smoke*.php` files: `docker run --rm --network none -v "${PWD}:/app:ro" -w /app/wordpress/carmilla-bridge php:8.1-cli php -l tests/T`; 37/37 exit 0.
- All 14 hermetic files in the first table: same container command with `php tests/T`; 14/14 exit 0, PASS output. `smoke-web-request-policy.php` reported 97 checks and 0 failures.
- Negative unqualified invocation of `smoke-web-login-real-wp.php`, `smoke-web-session-wp-adapter-real-wp.php`, `smoke-web-access-token.php` and `smoke-session-migration-ddl-fault.php`: each exit 2 with no database or network attached.
- No real-WP, MariaDB, WP/Woo matrix or CI job was run in 007A. These remain mandatory for 007B/007C and the parent.
