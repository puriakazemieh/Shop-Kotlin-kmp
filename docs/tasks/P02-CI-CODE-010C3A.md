# P02-CI-CODE-010C3A — Pin scanners and capture delivery-shaped reports

- Status: READY
- Phase/Area/Type: P02 / CI / DISC+CODE
- Priority/Risk/Size: P1 / HIGH / M
- Owner: AI
- Depends on: P02-CI-CODE-010C2
- Blocks: P02-CI-CODE-010C3A1

## Scope and acceptance

Pin the Plugin Check action and scanner to immutable versions and retain the Theme Check/WP/PHP version pins. Capture complete machine-readable reports and SHA-256 fingerprints in an isolated CI run. Compare scanned roots with the actual Bridge and Theme ZIP contents from both the package workflow and local build scripts, including bundled core and test files. The currently observed Plugin Check 3,381 findings include 3,115 test-path findings, while one Bridge package path excludes tests and another does not; prove which is the supported delivery artifact before changing the scan. Verify whether the package workflow's Theme ZIP actually includes the required core (the Theme Check staging and local build script copy it, but the package workflow has no explicit copy). Do not baseline, suppress, or declare security findings safe in this card. Allowed: `.github/workflows/code-quality.yml`, `tools/code-quality/**`, `docs/**`; read-only package workflow/source inspection. Evidence: `docs/evidence/P02-CI-CODE-010C3A/` with tool versions, source/run/artifact IDs, report hashes, file inventory diff, reproducible commands and exit codes. If alignment requires edits outside this scope, record a narrow follow-up card before any such edit.

## Completion record

- Final status: READY; no implementation started in the decomposition turn.
