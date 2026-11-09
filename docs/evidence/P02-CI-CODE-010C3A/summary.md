# C3A scanner and delivery inventory — 2026-11-09

## Scope and sources

Base source commit: `47a8d44f364ce4c1ca7763a321b5c1dfa33be045`. The original failing Plugin Check and Theme Check jobs are in [run 42602414295](https://github.com/puriakazemieh/Shop-Kotlin-kmp/actions/runs/42602414295), job IDs `129109856435` and `129109856837`; Plugin Check artifact ID `13666107970`. The prior raw-source Plugin Check log contained 3,381 file findings, 3,115 in test paths. These counts are **not** the pinned, shipped-file baseline.

Plugin Check action `v1.1.9` resolves to immutable commit `10857da14b6c2246d15402b3e69f777edcf8c12e` (`git ls-remote ... refs/tags/v1.1.9`). The official [action definition](https://github.com/WordPress/plugin-check-action/blob/main/action.yml) has no scanner-version input and installs the latest scanner if absent. The workflow now preinstalls Plugin Check `2.1.0`, checks the official ZIP SHA-256 `6ff4bd2145f3befcf907df158cc466b1649dafed5686de8369907403c3013fc4`, checks the installed version, pins WordPress `6.4.3`, and uses the immutable action commit. The Theme Check job retains PHP `8.1`, WordPress `6.4.3`, and Theme Check `20260901`. Both reports get SHA-256 fingerprints. The scanner ZIP was downloaded from `https://downloads.wordpress.org/plugin/plugin-check.2.1.0.zip` on 2026-11-09 (4,458,043 bytes); the workflow verifies the same bytes before installing from that URL.

## ZIP inventory (isolated reproduction of current delivery paths)

An isolated `git archive HEAD wordpress` checkout was used; the real source tree and user-owned ZIP were not modified. The two local Bash scripts were executed in Alpine 3.20 with `bash` and `zip`. Their checked-out CRLF was normalized **only in the disposable copy** so Bash could execute; the first unnormalized attempt exited 1 at `set -o pipefail`. The package workflow's two `zip -rq` commands were executed verbatim in the isolated copy. Four archive inventories, hashes, and full set differences are in this evidence directory.

| Path | Files | Shared core files | Test files | Observation |
| --- | ---: | ---: | ---: | --- |
| Package workflow Theme ZIP | 93 | 0 | 1 | Omits required runtime core; includes one theme test path. |
| Local Theme script ZIP | 113 | 23 | 4 | Includes core and its three tests; excludes three preview/docs files. |
| Package workflow Bridge ZIP | 76 | 0 | 0 | Omits required runtime core; excludes 85 bridge tests. |
| Local Bridge script ZIP | 184 | 23 | 88 | Includes core and all bridge/core tests. |

The current Plugin Check action scans `wordpress/carmilla-bridge` (161 tracked files, 85 tests), whereas neither package workflow ZIP nor local script ZIP has that exact set. The current Theme Check staging copies the theme's 93 tracked files and all 23 core files (including three core tests); neither Theme ZIP is identical. The package workflow ZIPs omit core because `.github/workflows/wordpress-package.yml` zips the host directories without copying `wordpress/packages/carmilla-core`. The local scripts copy core, then remove the staged copy. No scanner finding was suppressed or classified as safe by this inventory.

This is a delivery integrity defect, beyond C3A's allowed edit scope. Follow-up `P02-CI-CODE-010C3A1` is ordered before security triage and must align the ZIPs and scanner roots with reproducible install/activation checks. Until then, no shipped-file Plugin Check or Theme Check baseline can be declared, and `P02-CI-CODE-010` remains open.

## Reproduction and graph note

1. `git archive 47a8d44f wordpress -o source.tar`; extract to a disposable directory.
2. In Alpine 3.20 install `bash zip`; normalize CRLF in the **copy only**; run `bash build-bridge-zip.sh` and `bash build-theme-zip.sh` under copied `wordpress/` (both exit 0 after normalization).
3. Run the two `zip -rq` invocations from `.github/workflows/wordpress-package.yml` in the same copy (both exit 0).
4. Enumerate non-directory `ZipArchive.Entries` and compare sorted paths. See `*.files.txt`, `*-inventory-diff.json`, and `package-hashes-and-counts.json` for full results.
5. `git ls-files 'wordpress/carmilla-bridge/**'`, `git ls-files 'wordpress/carmilla-theme/**'`, and `git ls-files 'wordpress/packages/carmilla-core/**'` produced 161, 93, and 23 source files.

GitNexus was refreshed successfully with `GITNEXUS_LBUG_MAX_DB_SIZE=34359738368` after the earlier 16 GiB limit; status matched commit `47a8d44f` and all 2,787 covered files. File impact for `.github/workflows/code-quality.yml` returned `UNKNOWN`/target not indexed; text inspection of the action, workflow, package paths and scanner commands was therefore required. `detect-changes --scope all --repo .` exited 0, reported five indexed changed symbols and low graph risk, but the workflow file is outside graph coverage. The user-owned `docs/.obsidian/workspace.json` was preserved.

## Isolated PR CI confirmation

The pinned workflow ran on commit `d99fc42c3b965b73774df5f1c3ab25d07f7eedec`: [run 42649930350](https://github.com/puriakazemieh/Shop-Kotlin-kmp/actions/runs/42649930350). The Plugin Check job `129273226625` successfully installed the SHA-verified `2.1.0` scanner and the action resolved to `10857da14b6c2246d15402b3e69f777edcf8c12e`. Its log showed `plugin-check active ... 2.1.0 2.2.0`, proving it used the pinned installed version even though a newer version was available. The Theme Check job `129273226829` logged `PHP=8.1.34 WP=6.4.3 ThemeCheck=20260901`.

Both scanner jobs failed on actual findings, as expected; their fingerprint and artifact upload steps succeeded. Plugin Check's complete 844,352-byte report (artifact `13689004613`) has SHA-256 `10c40d28aebd311d34a62b614ef50cc84c45a3399d07f8b75bd7de4afa0c99b0`; its separate fingerprint artifact is `13689925655`. The report has 3,381 findings across 121 file headers: 580 ERROR and 2,801 WARNING; 3,115 findings are in test paths, and the 266 non-test findings contain 45 ERROR and 221 WARNING. The complete report is retained here as `plugin-check-results.txt.gz`; decompression reproduces the original hash. Theme Check's complete 26,314-byte JSON report (artifact `13688119750`) has SHA-256 `dbb841ae0ecf27c6aa7be152d0cad67df100be6a17806539bd8e8455d87c7e5f`, verified against the CI fingerprint. Its 63 findings comprise 13 REQUIRED, 19 WARNING, 23 RECOMMENDED and 8 INFO. The two complete reports and their CI fingerprint files are preserved in this directory. Other quality jobs (ktlint, detekt, WPCS) passed in the same run.

These are pinned **source/staging** measurements, not shipped-file baselines: the package mismatch above remains. No finding is waived. `P02-CI-CODE-010C3A1` is the next task and must fix package/scanner alignment before the security ledger. The aggregate `P02-CI-CODE-010` and phase gate remain open.
