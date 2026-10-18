# P00-QA-COMPAT-025 — declared minimum combination, isolated probe

2026-10-18. This is one patch-level test, not a change to the support contract or an assertion that all WP 6.3/PHP 7.4 versions and hosting configurations work. P00-T07 and owner UI acceptance remain open. No retained site or database was changed.

## Inputs and observations

- Official WordPress `https://wordpress.org/wordpress-6.3.5.zip`, SHA-256 `56cc2c70412b5e6a10102a31ffec15d6ed2297f10d9e2361248915f7ed56bc0a`; the archive and extracted copy remain under `D:\Carmilla-QA\compatibility-fixtures-20261018`. Hash identifies the downloaded input; no independent signature verification was performed.
- Official WooCommerce 8.5.2 and WP-CLI 2.12.0 inputs, hashes and limits are recorded in [P00-QA-COMPAT-024](../P00-QA-COMPAT-024/verification.md). PHP image `php:7.4-cli` was pulled with digest `sha256:620a6b9f4d4feef2210026172570465e9d0c1de79766418d3affd09190a7fda5`; mysqli was compiled inside the disposable container.
- Observed runtime: WordPress `6.3.5`, PHP `7.4.33`, WooCommerce `8.5.2`, MariaDB `10.6.28`. Theme, Bridge and shared core were source copies in the ephemeral container, not installed ZIP artifacts. WordPress outbound HTTP was blocked; all inputs and data were synthetic or read-only.

The initial `wordpress:6.3-php7.4-apache` Docker tag was unavailable; the official WordPress archive and official PHP CLI image supplied the actual combination instead. Fresh WP installation, Woo/Bridge activation and Theme activation succeeded. The same seven checks as P00-QA-COMPAT-024 passed: shared core, product, cart quantity/subtotal, Woo Store API and Carmilla manifest/catalog via in-process REST. PHP 7.4 syntax checks passed for Theme PHP, Bridge includes and shared core excluding tests. The shell syntax and missing-disposable-flag refusal also passed.

Not tested: HTTP server/browser, checkout/payment, HPOS behavior, packaged ZIPs, PHP extensions across hosts, WP 6.3.0 specifically, upgrade/rollback, mobile or independent product modes. One combination and CLI checks do not prove a minimum support policy.

## Reproduction

Check the exact temporary container name is unused. The following DB uses tmpfs, synthetic credentials and no host port; wait for its final MariaDB readiness log on port 3306. Inputs are mounted read-only; the wrapper copies WP into the throwaway container and compiles mysqli there.

```powershell
docker run --detach --rm --name carmilla-p00-declared-qa-20261018 --tmpfs /var/lib/mysql:rw --env MARIADB_ROOT_PASSWORD=synthetic_root_password --env MARIADB_DATABASE=carmilla_compat_qa --env MARIADB_USER=synthetic_qa --env MARIADB_PASSWORD=synthetic_qa_password mariadb:10.6
$taskSource = (Resolve-Path 'wordpress').Path.Replace('\', '/') + ':/source:ro'
$taskCore = 'D:/Carmilla-QA/compatibility-fixtures-20261018/wordpress-6.3.5/wordpress:/wordpress-source:ro'
$taskWoo = 'D:/Carmilla-QA/compatibility-fixtures-20261018/woocommerce-8.5.2/woocommerce:/woocommerce:ro'
$taskCli = 'D:/Carmilla-QA/wp-cli.phar:/wp-cli.phar:ro'
$taskPrefix = 'qa_' + [guid]::NewGuid().ToString('N').Substring(0,12) + '_'
docker run --rm --network container:carmilla-p00-declared-qa-20261018 --volume $taskSource --volume $taskCore --volume $taskWoo --volume $taskCli --env CARMILLA_REFERENCE_DISPOSABLE=yes --env "CARMILLA_REFERENCE_PREFIX=$taskPrefix" php:7.4-cli sh /source/carmilla-bridge/tests/smoke-declared-minimum.sh
```

Require exit 0, `Checks: 7; failures: 0.` and `PASS disposable Woo reference smoke`. The exact temporary MariaDB container was inspected then stopped, removing only its synthetic tmpfs database; preserved WordPress installations and data were left alone.
