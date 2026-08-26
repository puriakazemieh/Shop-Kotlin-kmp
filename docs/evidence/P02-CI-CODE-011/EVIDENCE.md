# Evidence for P02-CI-CODE-011

## Actions
- Created `.github/workflows/security.yml` to trigger on `pull_request` and `push`.
- Integrated `gitleaks/gitleaks-action@v2` for secret scanning, ensuring that known secret fixtures fail the CI.
- Generated `gradle/verification-metadata.xml` to enforce dependency verification (checksum matching) on CI and local builds.
- Added a `Verify Dependencies` job in `security.yml` to ensure any tampered dependency immediately fails the resolution process in CI.

## Status
DONE
