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

## CI confirmation

Pending isolated PR run of the pinned workflow; no PASS or DONE is claimed until the report artifacts, versions, hashes and failure mode are inspected.
