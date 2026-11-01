# P02-SEC-CODE-010C1B verification — 2026-11-01

## Change and impact

The legacy export command still creates NDJSON payloads in memory and uses the unchanged `Migration_Crypto` AES-256-GCM package format. It no longer opens or writes `posts.ndjson` and `media.ndjson` in uploads. Both encrypted packages are prepared before disk I/O, written to randomized encrypted temporary files, and published only after complete writes. A failed write or rename reports an error rather than success; temporary encrypted files are removed. If plaintext files from an older run already exist, the command stops and asks the operator to secure/remove them; it does not silently delete historical data.

GitNexus pre-edit `impact` for `Export_Command` reported UNKNOWN with no resolved callers. Text inspection confirmed standalone boot registration of the file, WP-CLI registration of `export-legacy`, the documented runbook/manual command, and the importer dependency on `posts.enc`. UNKNOWN was not treated as safety evidence.

## Executed checks

- `docker run --rm -v "${PWD}:/app" -w /app php:8.1-cli php wordpress/carmilla-bridge/tests/smoke-migration-export-no-plaintext.php` → exit 0: encrypted posts/media decrypt, forced second write failure cleans the first encrypted temporary file without a success report, and pre-existing legacy plaintext blocks export.
- `docker run --rm -v "${PWD}:/app" -w /app php:8.1-cli php wordpress/carmilla-bridge/tests/smoke-p07-seed-migration.php` → exit 0: 14 existing denylist/encryption assertions passed.
- PHP 8.1 `php -l` for `Export_Command.php` and the new test → exit 0, no syntax errors.
- The same focused export/failure smoke on PHP 7.4 → exit 0, all three assertions passed. PHP 7.4 `php -l` for this and the four messaging production files → exit 0, no syntax errors.
- `git diff --check` → exit 0.
- Local QA package built from a copied Bridge source and current shared core at `.artifacts/p02-security-010C1B/carmilla-bridge.zip`; SHA-256 `C84083CA0F8B68D484A03ED93FE2C88289C5201A992C97DD32AE80EE09C38994`. ZIP entry `carmilla-bridge/includes/Migration/Export_Command.php` was inspected (5,564 bytes). This is a local QA package, not a release/signing gate.

## Required human QA

Use two disposable WordPress sites, or snapshots that can be restored, with Bridge installed from the QA ZIP and only synthetic posts/media. Back up database and `wp-content` before import. On the source, create one post titled `Synthetic migration post` with harmless text and one synthetic media item. Run `wp carmilla export-legacy --output=/tmp/carmilla-qa-export --customer_uuid=synthetic-site --encryption_key=12345678901234567890123456789012` as the WordPress CLI user. Expect success, exactly `posts.enc` and `media.enc` in the fresh output directory, and no `.ndjson` or `.tmp` files. Confirm neither encrypted file contains the post text. On the disposable target, run `wp carmilla import-legacy --input=/tmp/carmilla-qa-export/posts.enc --site_uuid=synthetic-site --encryption_key=12345678901234567890123456789012 --consent=yes` after backup. Expect the synthetic post once and no excluded private metadata. For a failure case on the source, use a separate output directory that the CLI user cannot write, rerun export, and expect nonzero exit, no success message, no plaintext and no temporary output. For a legacy case, place a synthetic `posts.ndjson` in a separate output directory and expect export refusal with instructions to secure/remove that file. Never use a production migration key or customer data in these tests. Record host, tester, date, and outcomes before setting DONE.

No human QA has been claimed. C2 remains BLOCKED pending this and 010C1A confirmation.
