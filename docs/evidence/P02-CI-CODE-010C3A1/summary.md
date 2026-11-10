# C3A1 package and scanner-root alignment — 2026-11-10

## Change

The two local Bash builders now stage each host and the required shared core in a disposable directory, exclude `tests/`, `node_modules/`, and `.DS_Store`, and verify the core entry before reporting success. They do not add or remove `packages/` in the source tree. `.gitattributes` forces LF for the two executable Bash files, fixing the archived CRLF failure measured in C3A. The package workflow invokes these exact builders, so its ZIP contents use the same construction path. Plugin Check and Theme Check now extract the built ZIPs and scan those shipped roots instead of separate raw-source staging. Theme preview/PARITY exclusions remain as before. No security finding is suppressed.

## Isolated package tests

Source was exported from base commit `48c20a16` with `git archive HEAD wordpress` into ignored `.artifacts/p02-ci-010c3a1/checkout`; only the two modified builder scripts were overlaid there. The source tree and pre-existing user ZIP were not changed. Environment: Alpine 3.20 with `bash`, `zip`, `unzip`; PHP 8.1 CLI for lint. Commands and results:

1. `bash -n build-theme-zip.sh build-bridge-zip.sh` — exit 0 without CRLF normalization.
2. `bash build-theme-zip.sh /work/theme-local.zip` and `bash build-bridge-zip.sh /work/bridge-local.zip` — exit 0; both ZIPs contain `packages/carmilla-core/Carmilla_Kernel.php`, no `tests/` entries.
3. The package workflow's versioned builder calls produced `carmilla-theme-v0.8.0.zip` and `carmilla-bridge-v1.0.0.zip` — exit 0. Comparing every non-directory path, CRC32 and uncompressed length against local outputs found **zero differences** for each product. Binary ZIP hashes differ because staging metadata/timestamps differ; byte-for-byte reproducibility is a later P04 gate. Full entry inventories and four ZIP SHA-256 values are in this directory.
4. The Theme ZIP has 109 files, including 20 shared-core PHP/source files; the Bridge ZIP has 96 files, including the same 20 core files. Neither has a test file. Extracting each ZIP into the scanner root yielded **zero PHP-path differences**: 79 theme PHP files and 89 plugin PHP files. `find scanned-theme scanned-bridge -name '*.php' -print0 | xargs -0 -n1 php -l` — exit 0.
5. Negative test: remove only `packages/carmilla-core/Carmilla_Kernel.php` in a second disposable source copy, run both builders, and require nonzero exit plus absent output ZIP. Both builders failed and produced no ZIP; the wrapper printed `PASS-negative-core`.

## Disposable WordPress install/activation test

Fresh `wordpress:6.4-php8.1-apache` and `mariadb:10.6` containers were started on a dedicated temporary network with synthetic credentials and no production volume. PHP was 8.1.27, WordPress 6.4.3, MariaDB 10.6.28, and WP-CLI 2.12.0 (`wp-cli.phar` SHA-256 `ce34ddd838f7351d6759068d09793f26755463b4a4610a5a5c0a97b68220d85c`). `wp core install` succeeded. `wp db check` was not used as a health assertion because the WordPress image lacks `mysqlcheck`; `wp core install` and `core is-installed` both returned 0.

- Theme first: `wp theme install /artifacts/theme-local.zip --activate` exited 0 while Bridge was not installed (`wp plugin is-installed carmilla-bridge` exited 1). `ReflectionClass('Carmilla_Kernel')->getFileName()` resolved under the Theme ZIP's `packages/carmilla-core` and the class existed.
- Plugin on a standard theme: activate Twenty Twenty-Four, then `wp plugin install /artifacts/bridge-local.zip --activate`; both exited 0. Reflection resolved under the Bridge ZIP's `packages/carmilla-core`; `GET /carmilla/v1/client-manifest` through `rest_do_request` returned HTTP 200.
- Theme alone after deactivating Bridge: Theme activation and core load succeeded, with reflection under the Theme ZIP; the same manifest route returned 404. This is a **known functional gap** for later `P04-WORDPRESS-CODE-051` and is not called a Theme-only API PASS here. C3A1 acceptance concerns installable ZIP content and scanner parity, not completion of the independent-product API gate.

Both disposable containers and their network were removed after the check. No owner-only or manual QA is required for this package-alignment card.

## CI confirmation

Pending PR run of the updated quality workflow; the task remains READY until the two scanner jobs visibly build and inspect ZIP roots and upload complete reports.
