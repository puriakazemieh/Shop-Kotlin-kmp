# P02-CI-CODE-010C3A1 — Align WordPress delivery ZIPs and scanner roots

- Status: DONE
- Phase/Area/Type: P02 / CI / CODE+QA
- Priority/Risk/Size: P1 / HIGH / M
- Owner: AI
- Depends on: P02-CI-CODE-010C3A
- Blocks: P02-CI-CODE-010C3B1

## Scope and acceptance

The C3A inventory found that the package workflow's Bridge and Theme ZIPs omit the required `carmilla-core`, while the local build scripts include it and include test files. The archived Bash scripts also failed on Linux due to CRLF until normalized in a disposable copy. Make the supported delivery paths explicit and consistent in `.github/workflows/wordpress-package.yml`, `wordpress/build-bridge-zip.sh`, and `wordpress/build-theme-zip.sh`. Keep both products independently installable; do not silently exclude production code or mark scanner findings safe. Ensure Plugin Check and Theme Check scan the same shipped PHP files, excluding development-only tests only after the ZIP contents prove they are absent. Verify the two ZIP contents from both build paths, clean WordPress installation/activation in a disposable environment, and a negative check that fails if the required core disappears. Preserve the package manifest/feature selection contract for later P04 work. Evidence: `docs/evidence/P02-CI-CODE-010C3A1/` with exact commands, hashes, inventories, environment, outcomes, and limitations. Any larger package refactor requires further decomposition before code.

## Completion record

- Final status: DONE on 2026-11-10. Both build paths use the same staged builders; ZIP inventories match, core is bundled, tests are excluded and missing-core builds fail. Disposable WordPress 6.4.3 accepted Theme-only and Plugin-on-standard-Theme ZIP installs and loaded each bundled kernel. PR run `42751751194` proved both scanners read the ZIP-extracted roots and saved complete hashed reports. Evidence: `docs/evidence/P02-CI-CODE-010C3A1/`. Scanner findings and Theme-only manifest 404 remain open in their own cards; no phase gate PASS claimed.
