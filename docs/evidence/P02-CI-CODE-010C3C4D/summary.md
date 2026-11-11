# P02-CI-CODE-010C3C4D — current exact Plugin Check reconciliation

Completed 2026-11-11. No product source or package workflow changed in this card.

## Report and disposition

The delivery-shaped Bridge ZIP built after C3C4A–C has SHA-256 `8168832867640530deef22b17c30e54f8ba0ae666c24a900b014659c337a9f54`. It was installed and scanned in disposable WordPress 6.4.3/PHP 8.1/MariaDB 10.6 with Plugin Check 2.1.0 on 2026-11-10. The full 310-finding report is [plugin-check-current.txt.gz](plugin-check-current.txt.gz), uncompressed SHA-256 `3c45375b51d1c525be3accd509ddc5f6d11e7c37fc27c5f6974cf2465c2fecbb`.

Compared with the 315-finding historical baseline, eight old fingerprints disappeared: all four P02 `must_disappear` findings (two old integration logs, core direct-access guard, wallet direct query) and four prior allowed findings (old unescaped error and three old-position tab warnings). The only three report fingerprints absent from the historical baseline are the same read-only tab GET warnings now at `includes/class-cb-integrations-controller.php:78` columns 30, 59 and 109. [The exact ledger](ledger.json) records each message, severity, source path, capability/sanitization/allowlist and read-only reason. The settings and test-message POST paths use separate nonce checks. No code-wide or severity-wide waiver was added.

`check_plugin_check_baseline.cjs` pins the new ledger's SHA-256 `35275460c102242c4a47c58247d4c3af44c775ab442f24b588f880cf914937cc`, the report's uncompressed hash, and exactly these three path/line/column/code/severity/disposition identities. It cross-checks each ledger finding against that full report before allowing it. The original 315-finding ledger and its four `must_disappear` policies remain intact. The current total of individually reviewed fingerprints is 318; resolved fingerprints are allowed to disappear.

## Verification

- `node tools/code-quality/check_plugin_check_baseline.cjs --report <full current report> --scanner-version 2.1.0 --action-ref 10857da14b6c2246d15402b3e69f777edcf8c12e --wp-version 6.4.3` exited 0: `PASS Plugin Check exact baseline: actual=310 reviewed=318 resolved=8`. Exact output: [current-pass.txt](current-pass.txt).
- The same report with one synthetic `WordPress.Security.Synthetic.NewFinding` exited 1 and named its exact location. Exact output: [current-negative.txt](current-negative.txt).
- `node tools/code-quality/test_plugin_check_baseline.cjs` passed all 19 scenarios: historical approved subset, current report, new-finding rejection, earlier P02 findings still rejected if present, missing/empty/malformed report, version drift, message mutation, duplicates, historical ledger tampering, and current ledger/report tampering. Output: [test-output.txt](test-output.txt).
- The GitNexus pre-edit impact for checker functions `ledgerRows`, `main` and the focused test runner was MEDIUM, affecting the code-quality gate; no product symbol was edited. `git diff --check` passed.

The Docker daemon was unavailable when a repeat scan was attempted on 2026-11-11. The retained full scan above was performed after all product fixes and on the exact ZIP used for this reconciliation; no product file changed between that scan and this checker change. This card establishes the local exact gate for the current package. The separate WooCommerce checkout acceptance in `P02-SEC-CODE-010C3B2E1` remains open, so the C3C parent and P02 phase gates are not marked passed.
