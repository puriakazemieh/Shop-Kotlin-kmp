# P02-QA-CODE-007B1 — PHP 7.4 migration guard compatibility

Baseline commit `67b03824`; repository root `D:\Android\AndroidStudioProjects\kmp-shop`. The scope was limited to `Migration_Config::is_denied_meta_key` and its hermetic smoke. No migration database, network, customer data or production site was touched.

## Failing characterization

After adding mixed-case token/password/secret/api_key assertions, the existing `php:7.4-cli` smoke failed before reaching the new checks:

```text
docker run --rm --network none -v "${PWD}:/app:ro" -w /app/wordpress/carmilla-bridge php:7.4-cli php tests/smoke-p07-seed-migration.php
exit 255: undefined function Carmilla\Bridge\Migration\str_contains() at Migration_Config.php:71
```

`str_contains` was introduced after the project's PHP 7.4 minimum. Each searched needle is nonempty, so `strpos(...) !== false` preserves the existing case-normalized match behavior. The direct live callers are `Migrator.php`, `Export_Command.php`, `Export_Overlay_Command.php` and the smoke test. GitNexus reported CRITICAL upstream risk with many likely generated/cross-language relations; this was treated as a warning, checked against direct source callers, and the edit stayed within the four expressions. No deny rule was removed.

## Executed verification after the fix

For each Docker image `php:7.4-cli`, `php:8.1-cli`, and `php:8.2-cli`, from the repo root with `--rm --network none -v "${PWD}:/app:ro" -w /app/wordpress/carmilla-bridge`:

| Command inside container | PHP 7.4 | PHP 8.1 | PHP 8.2 |
|---|---:|---:|---:|
| `php -l includes/Migration/Migration_Config.php` | 0 | 0 | 0 |
| `php -l tests/smoke-p07-seed-migration.php` | 0 | 0 | 0 |
| `php tests/smoke-p07-seed-migration.php` | 0; 14 PASS | 0; 14 PASS | 0; 14 PASS |

The full 14-file hermetic smoke subset was rerun under `php:7.4-cli`: 14/14 exit 0. The assertions cover existing billing email, provider API key, mixed-case token/password/secret/api_key keys, an allowed public key, and the existing crypto/site checks. Manual QA is N/A for this pure logic compatibility edit. The real WordPress/Woo matrix remains 007B work.
