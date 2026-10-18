# P00-QA-COMPAT-026 — isolated installation of newly built ZIPs

2026-10-18. This is an automated, bounded co-installation probe, not a release or owner sign-off. No retained WordPress installation, existing ZIP, or database was modified.

## Build and integrity

The Theme, Bridge, shared core, and the repository's two build scripts were copied into the new directory `D:\Carmilla-QA\package-probe-20261018-8c8ad954`. The scripts were executed **there**, not in the checkout: their `rm` operations affect only the isolated staging copy. The directory and ZIPs remain available; they are test artifacts, not release artifacts.

| ZIP | SHA-256 |
| --- | --- |
| `carmilla-theme.zip` | `839f43b678b82e783951535e59d2d47bd226edc78e7a548d3a427fec74aaa8c3` |
| `carmilla-bridge.zip` | `8d6cbc239a8f33079184aaaf2776583a88c6993b88df5f3360c0423169ed13e6` |

Both archives contained exactly 22 shared-core files: each path and SHA-256 matched the current source. This check is limited to shared-core integrity and does not certify all other entries, supply-chain provenance or release readiness.

## Fresh install

Using WordPress `6.4.3`, PHP `8.1.27`, MariaDB `10.6.28`, WooCommerce `8.5.2` and WP-CLI `2.12.0`, WP-CLI installed and activated both local ZIPs in a newly installed synthetic site. The seven existing product, cart, shared-core and in-process Woo/Carmilla REST assertions all passed: `Checks: 7; failures: 0`, `PASS disposable Woo reference smoke`, `PASS packaged install smoke`. The test wrapper passed `bash -n` and refused to run without the disposable flag.

Inputs were read-only mounts and WordPress external HTTP was blocked. The test database container had no bind mounts or host ports and used tmpfs for synthetic data; after inspection it was stopped, destroying only that temporary synthetic database. All retained installations and data were preserved.

## Reproduction

Verify the directory and artifacts and use a new temporary container name. Wait for the final MariaDB ready message before invoking the probe. The WP CLI, Woo fixture and artifact hashes are documented here and in [P00-QA-COMPAT-024](../P00-QA-COMPAT-024/verification.md).

```powershell
docker run --detach --rm --name carmilla-p00-package-probe-20261018 --tmpfs /var/lib/mysql:rw --env MARIADB_ROOT_PASSWORD=synthetic_root_password --env MARIADB_DATABASE=carmilla_compat_qa --env MARIADB_USER=synthetic_qa --env MARIADB_PASSWORD=synthetic_qa_password mariadb:10.6
$taskSource = (Resolve-Path 'wordpress').Path.Replace('\', '/') + ':/source:ro'
$taskArtifacts = 'D:/Carmilla-QA/package-probe-20261018-8c8ad954:/artifacts:ro'
$taskWoo = 'D:/Carmilla-QA/compatibility-fixtures-20261018/woocommerce-8.5.2/woocommerce:/woocommerce:ro'
$taskCli = 'D:/Carmilla-QA/wp-cli.phar:/wp-cli.phar:ro'
$taskPrefix = 'qa_' + [guid]::NewGuid().ToString('N').Substring(0,12) + '_'
docker run --rm --network container:carmilla-p00-package-probe-20261018 --volume $taskSource --volume $taskArtifacts --volume $taskWoo --volume $taskCli --env CARMILLA_REFERENCE_DISPOSABLE=yes --env "CARMILLA_REFERENCE_PREFIX=$taskPrefix" wordpress:6.4-php8.1-apache sh /source/carmilla-bridge/tests/smoke-packaged-reference.sh
```

Require exit code 0 and both final PASS markers. The recorded stage was copied before this test wrapper was added; its shared-core contents and product sources were identical to those tested.

## Remaining limitations

No browser or HTTP checkout, payment, upgrade/rollback, independent Theme-only or Bridge-only mode, other hosting environment, or human acceptance was tested. This combination does not establish the minimum support contract or close `P00-T07`; the PWA authentication chain and human gates remain open.
