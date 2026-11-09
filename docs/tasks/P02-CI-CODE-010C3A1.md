# P02-CI-CODE-010C3A1 — Align WordPress delivery ZIPs and scanner roots

- Status: READY
- Phase/Area/Type: P02 / CI / CODE+QA
- Priority/Risk/Size: P1 / HIGH / M
- Owner: AI
- Depends on: P02-CI-CODE-010C3A
- Blocks: P02-CI-CODE-010C3B1

## Scope and acceptance

The C3A inventory found that the package workflow's Bridge and Theme ZIPs omit the required `carmilla-core`, while the local build scripts include it and include test files. The archived Bash scripts also failed on Linux due to CRLF until normalized in a disposable copy. Make the supported delivery paths explicit and consistent in `.github/workflows/wordpress-package.yml`, `wordpress/build-bridge-zip.sh`, and `wordpress/build-theme-zip.sh`. Keep both products independently installable; do not silently exclude production code or mark scanner findings safe. Ensure Plugin Check and Theme Check scan the same shipped PHP files, excluding development-only tests only after the ZIP contents prove they are absent. Verify the two ZIP contents from both build paths, clean WordPress installation/activation in a disposable environment, and a negative check that fails if the required core disappears. Preserve the package manifest/feature selection contract for later P04 work. Evidence: `docs/evidence/P02-CI-CODE-010C3A1/` with exact commands, hashes, inventories, environment, outcomes, and limitations. Any larger package refactor requires further decomposition before code.

## Completion record

- Final status: READY after C3A; created from its measured package mismatch. No implementation in C3A.
