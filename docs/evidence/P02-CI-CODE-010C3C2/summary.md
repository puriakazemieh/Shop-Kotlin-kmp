# P02-CI-CODE-010C3C2 verification

## Change

The pinned Plugin Check action remains at `10857da14b6c2246d15402b3e69f777edcf8c12e` with scanner `2.1.0` and WordPress `6.4.3`. Its finding-level exit is now deferred to an independent exact-baseline step. That step runs even after action failure, verifies the actual scanner and WordPress versions, and fails on an absent report or any unapproved finding. The full scanner report, SHA-256, and gate output are uploaded even when the gate fails. The action's PR comment is informational; an oversized or failed comment cannot make the separate gate green.

## Local verification, 2026-11-10

- `docker run --rm -v "${PWD}:/repo" -w /repo rhysd/actionlint:1.7.7 -color .github/workflows/code-quality.yml` — exit 0.
- `node tools/code-quality/test_plugin_check_baseline.cjs` — 15 scenarios passed, including the approved subset, new findings, a remaining P02 finding, missing report, version drift and tampered ledger.
- `git diff --check` — exit 0.
- Disposable WordPress 6.4.3/PHP 8.1 with MariaDB 10.6, current Bridge ZIP and Plugin Check 2.1.0: `wp plugin check carmilla-bridge --format=json --require=./wp-content/plugins/plugin-check/cli.php` produced 314 findings in 42 files. Complete report: [plugin-check-current.txt.gz](plugin-check-current.txt.gz), uncompressed SHA-256 `d2a634c5b074605eb22e9cf6ac25b4d983d4f88c911900c3e35d427d1bbc2941`.
- Running the exact checker on that report exited 1 as required. It reported five shifted/new integration-controller fingerprints, one mixed-line-ending finding in the order controller, and the remaining wallet direct-query fingerprint. The gate is genuinely red on the current package; this task does not reclassify those findings or claim a green Plugin Check result.
- Relevant prior [GitHub Actions run 42751751194](https://github.com/puriakazemieh/Shop-Kotlin-kmp/actions/runs/42751751194) on `4db12379`: scanner installation succeeded, Plugin Check failed on findings, and both its full `plugin-check-results` artifact and fingerprint artifact uploaded. This validates the pinned action/report path. The new workflow itself has not yet run remotely; C3C3 owns the isolated PR run and final review.

## Limitations and follow-up

The current exact baseline remains red. C3C3 must inspect the changed CI run and resolve or explicitly triage every current fingerprint mismatch without a blanket waiver. The wallet checkout acceptance remains `AWAITING_MANUAL_QA` in its own task; this CI task does not assert a phase gate PASS.
