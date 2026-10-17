# P00-T09 — tracked-source secret and test-data scan

- **Date:** 2026-10-14
- **Scope:** 2,230 tracked repository files only. Untracked user files and
  local WordPress databases were intentionally not read or copied.
- **Status:** `PASS for tracked source/evidence; runtime-log review remains in P01-T09`

## Commands and results

| Check | Result |
| --- | --- |
| `git ls-files` inventory | 2,230 tracked files |
| tracked `wp-config.php` | 0 files |
| tracked `.env*` | 0 files |
| private-key marker | 0 files |
| AWS access-key marker | 0 files |
| credential-assignment candidate | 1 reviewed file |

The single candidate was
`wordpress/carmilla-bridge/includes/class-cb-sms-http-adapter.php`. Review
showed it reads an encrypted administrator setting at runtime, decrypts it only
server-side, and masks it for diagnostics; no credential literal is committed.

This result does not replace P01-T09: browser, WordPress, proxy, and device
logs must still be inspected during the security manual run.
