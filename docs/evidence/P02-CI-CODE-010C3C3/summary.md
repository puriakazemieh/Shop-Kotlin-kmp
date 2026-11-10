# P02-CI-CODE-010C3C3 — isolated Plugin Check gate proof

Date: 2026-11-10. Repository: `puriakazemieh/Shop-Kotlin-kmp`. The isolated [draft PR #34](https://github.com/puriakazemieh/Shop-Kotlin-kmp/pull/34) targeted `codex/p02-ci-phase-triage`; it was **closed without merge** after the tests. Its `codex/p02-c3c3-synthetic` branch is retained for audit. The PR changed only CI, checker, task and evidence files, with no product-source change. The report override was explicitly marked `SYNTHETIC ... not a product scan` in each artifact.

## CI defect found and corrected

Initial runs [42837682824](https://github.com/puriakazemieh/Shop-Kotlin-kmp/actions/runs/42837682824) and [42838439033](https://github.com/puriakazemieh/Shop-Kotlin-kmp/actions/runs/42838439033) exposed a real version mismatch: pinned `wordpress/plugin-check-action@10857da14b6c2246d15402b3e69f777edcf8c12e` overwrote `.wp-env.json`, ignored non-trunk `wp-version: '6.4.3'`, and ran WordPress **7.1.4**. The baseline correctly rejected the supposedly positive fixture on version drift. The pinned action's `action.yml` confirms that its `WP_VERSION` is set only for `trunk` and otherwise `null`.

The main workflow now runs the actual Plugin Check scan in the verified WordPress **6.4.3** / Plugin Check **2.1.0** environment *before* the action resets wp-env. It records both observed versions and the full pinned report, restores that report after the action, and applies the exact baseline to it. The action still provides PR feedback and its own separate artifact. The gate artifact also includes the version records and full report. The synthetic PR temporarily replaced the restored report only to test the gate; this override is absent from the main workflow.

## Positive run

- Revision `24e61ed11fc22148a3585fb2a797463e1521ad84`; [run 42839324176](https://github.com/puriakazemieh/Shop-Kotlin-kmp/actions/runs/42839324176), Plugin Check job `129909971105`: **success**. Pinned scan, action, restore, synthetic injection, exact gate, fingerprint, and artifact steps all succeeded. Other jobs were intentionally skipped on this isolated branch.
- Gate artifact `13776510077` ([preserved ZIP](positive-gate.zip)): report, SHA-256, diagnostics, synthetic marker and observed versions. Extracted and inspected: scanner `2.1.0`, WordPress `6.4.3`, 311 approved findings, report SHA-256 `cb2eb66d4f8ac8ffe1ceadf7398992e41eeda4ec51e32899fbf6c6200d4ab41d`, checksum matches; diagnostics: `PASS Plugin Check exact baseline: actual=311 reviewed=315 resolved=4`. Action's separate artifact ID `13774834875`.

## New-finding negative run

- Revision `acc7626811de5a4f39dc2c378a957e4672612f0d`; [run 42840114684](https://github.com/puriakazemieh/Shop-Kotlin-kmp/actions/runs/42840114684), Plugin Check job `129912746911`: **failure**. Pinned scan, action, restore, injection, fingerprint and artifact upload succeeded; the exact gate failed on the one added `WordPress.Security.Synthetic.NewFinding` at `includes/class-cb-integrations-controller.php:999:1`.
- Gate artifact `13776096295` ([preserved ZIP](negative-gate.zip)): same six files despite the failed gate. Extracted and inspected: scanner `2.1.0`, WordPress `6.4.3`, 312 findings, report SHA-256 `6bd70ad76f600586a7ab77716f2a50f08fdfcf36b4ec7f5b1ea59681bbb9daa6`, checksum matches; diagnostics name the new finding. Action's separate artifact ID `13776800749`.

## Local verification and limits

- `rhysd/actionlint:1.7.7` parsed the corrected main workflow successfully.
- Running the positive and negative fixture generators and exact checker locally reproduced PASS (311) and FAIL (new finding), respectively.
- The real current Bridge package still fails the exact baseline on the mismatches recorded in C3C2. This synthetic proof establishes CI gate behavior, **not** product scan clearance, wallet checkout QA, or the P02 phase gate. The closed PR must not be merged. The retained isolated branch can be removed after evidence retention if desired.
