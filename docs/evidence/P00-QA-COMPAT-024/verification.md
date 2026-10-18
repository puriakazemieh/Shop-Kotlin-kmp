# P00-QA-COMPAT-024 — WooCommerce 8.5 reference probe

2026-10-18. Narrow isolated test only; it does not establish the support contract, approve a release, validate an existing ZIP or change any retained WordPress installation. No owner UI QA is asserted.

## Inputs

- Official artifact: `https://downloads.wordpress.org/plugin/woocommerce.8.5.2.zip`; SHA-256 `263067cd011103c707a33a57b53b9f9382baed9d8762fa70163722e88b8acffa`. The hash records what was tested; no independent signature verification was performed. The downloaded archive and its extracted test fixture remain under `D:\Carmilla-QA\compatibility-fixtures-20261018`.
- Runtime: WordPress `6.4.3`, PHP `8.1.27`, MariaDB `10.6.28`, WooCommerce `8.5.2`, WP-CLI `2.12.0`; WP-CLI SHA-256 `ce34ddd838f7351d6759068d09793f26755463b4a4610a5a5c0a97b68220d85c`. Docker image tags may change.
- Carmilla Theme/Bridge files were copied from the current repository into an ephemeral container and each received the current shared core; this is **not** installation or validation of the packaged ZIPs. All mounted source and downloaded files were read-only. The test uses only synthetic data and disables outbound WordPress HTTP.

## Result

Fresh WP installation, Woo/Bridge activation and Theme activation succeeded. Seven checks passed: expected Woo version and shared core; synthetic product persistence; cart quantity 2 and subtotal 200000; Woo Store API cart response; Carmilla client manifest; and Carmilla catalog returning the synthetic product. REST calls used `rest_do_request` inside WP-CLI, not browser or external HTTP. PHP lint, shell syntax and the disposable execution guard passed.

No HPOS/checkout/payment, upgrade or rollback, independent Theme-only/Plugin-only mode, older WP/PHP minimum, or owner UI test was performed. The retained matrix still has Woo 8.3.1 in its minimum row. Reference QA versions and the commercial WordPress/PHP/Woo support floors remain to be reconciled; `P00-T07` remains open.

## Reproduction

Use the downloaded verified-by-hash artifact only as a test input; inspect the exact temporary container name first. The new MariaDB uses tmpfs, synthetic credentials and no host ports. The source, Woo extraction and WP-CLI mounts must be read-only. Wait for the final MariaDB readiness log on port 3306 before invoking the test; port 0 is initialization only.

```powershell
docker run --detach --rm --name carmilla-p00-compat-qa-20261018 --tmpfs /var/lib/mysql:rw --env MARIADB_ROOT_PASSWORD=synthetic_root_password --env MARIADB_DATABASE=carmilla_compat_qa --env MARIADB_USER=synthetic_qa --env MARIADB_PASSWORD=synthetic_qa_password mariadb:10.6
$taskSource = (Resolve-Path 'wordpress').Path.Replace('\', '/') + ':/source:ro'
$taskWoo = 'D:/Carmilla-QA/compatibility-fixtures-20261018/woocommerce-8.5.2/woocommerce:/woocommerce:ro'
$taskCli = 'D:/Carmilla-QA/wp-cli.phar:/wp-cli.phar:ro'
$taskPrefix = 'qa_' + [guid]::NewGuid().ToString('N').Substring(0,12) + '_'
docker run --rm --network container:carmilla-p00-compat-qa-20261018 --volume $taskSource --volume $taskWoo --volume $taskCli --env CARMILLA_REFERENCE_DISPOSABLE=yes --env "CARMILLA_REFERENCE_PREFIX=$taskPrefix" wordpress:6.4-php8.1-apache sh /source/carmilla-bridge/tests/smoke-woo-reference.sh
```

Require both exit 0 and the `PASS disposable Woo reference smoke` marker. After checking the exact temporary container's tmpfs and mounts, stopping it removed only its synthetic in-memory database. Retained WordPress sites and their data were untouched.
