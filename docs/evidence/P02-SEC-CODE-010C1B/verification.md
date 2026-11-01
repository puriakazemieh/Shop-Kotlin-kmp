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

## Agent-run real WordPress migration QA — 2026-11-01

The agent installed and activated the QA Bridge ZIP in separate isolated WordPress 6.4.3 / PHP 8.1 / MariaDB 10.6 source and target installations, with distinct database prefixes and no Carmilla theme. The source received one synthetic post, one synthetic media entry, an allowed metadata field and a denied billing-email metadata field. It invoked the actual `Export_Command::execute` with a synthetic 32-byte key and site UUID. The export returned success, the output directory contained exactly `posts.enc` and `media.enc`, neither encrypted file contained the clear post title, both decrypted for the same site, and the denied metadata was absent. The source probe exited 0.

In that real installation, the agent also ran export against a root-owned unwritable directory as the WordPress user. It failed without a success report or any output file. A separate directory containing a synthetic `posts.ndjson` was rejected before export and produced no encrypted package. Both checks exited 0. The earlier PHP 7.4/8.1 focused smoke additionally forced failure specifically on the second encrypted write and verified cleanup of the first temporary file.

The encrypted `posts.enc` was copied to the distinct disposable target WordPress installation. The actual `Import_Command::execute` with `consent=yes` imported the synthetic post once, kept the allowed metadata, omitted the denied billing-email metadata, and a repeated import still left exactly one matching post. Target probe exited 0. All five QA WordPress front pages, including the target, returned HTTP 200. Scripts and synthetic package remain under ignored `.artifacts/p02-real-wp-qa-20261101/`.

The installation and data-path checks passed, but no owner has yet confirmed the migration outcome. Under `AGENTS.md` and the active product contract the card remains `AWAITING_MANUAL_QA`; C2 remains BLOCKED. No production migration or customer data was touched.
