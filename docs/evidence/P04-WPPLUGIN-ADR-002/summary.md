# P04-WPPLUGIN-ADR-002 — evidence

- Date/time zone: 2026-11-22, Asia/Tehran
- Repository/cwd: `D:/Android/AndroidStudioProjects/kmp-shop`; baseline commit `ee3f83de` on `develop`.
- Pre-edit status: only user-owned `docs/.obsidian/appearance.json` and `docs/.obsidian/workspace.json` modified; preserved.
- Reviewer: Codex (technical architecture review), 2026-11-22. No owner product-release approval claimed.
- Change: [ADR-007](../../architecture/adr/ADR-007-SHARED-KERNEL-HOST-BOOTSTRAP.md) defines package boundaries, candidate timing, one compatible kernel/schema authority, mismatch isolation, host transfer and external build runner.

## Characterization

Live `wordpress/carmilla-bridge/carmilla-bridge.php` invokes `Carmilla_Kernel::boot` from `plugins_loaded`; `wordpress/carmilla-theme/functions.php` invokes it from `after_setup_theme`. Both currently use `class_exists` to load a kernel. `wordpress/carmilla-theme/inc/post-types.php` and `wordpress/carmilla-bridge/includes/class-cb-standalone-boot.php` independently register CPTs at `init`; Bridge registers routes at `rest_api_init`. These facts do not demonstrate the target single-kernel behavior. WordPress official hook docs confirm plugin hook precedes theme setup and `rest_api_init` is for REST registration. ADR-005 is historical under ADR-006.

## Architecture review scenarios

| Case | Documented result |
|---|---|
| Theme-only | Embedded kernel selected; UI/API and write path available for purchased modules. |
| Plugin-only | Embedded kernel selected; public renderer on third-party theme and API. |
| Both compatible | One kernel, one schema/write path and one build job per request. |
| API/schema mismatch | Isolate incompatible module/host, fail closed for Carmilla writes, actionable admin notice; no blind downgrade. |
| One host disabled or transferred | Re-evaluate remaining candidate and entitlement; preserve data/IDs. |
| Last host disabled | Carmilla services stop; site data and previously delivered artifacts remain. |
| Unauthorized/disabled module | New UI/API/job operation denied; past data retained; separate authorized operations remain. |

## Verification and limits

`git diff --check` baseline: exit 0 from repository cwd. Link existence check: exit 0 for ADR-007, ADR-038 and this evidence file. Architecture scenario review: PASS against ADR-006, ADR-038, active product spec and P04 acceptance row. This is documentation QA only: no ZIP, WordPress runtime, HTTP/UI, migration, or native builder was executed. No real customer data, signing key or payment was used. Runner and product acceptance remain in later cards.

GitNexus `detect-changes --scope all --repo .`: exit 0, six changed files, two tracked symbols, zero affected processes, low risk; the new ADR was reviewed separately. The prior task's post-commit `analyze --index-only` and `status` both exited 0 and reported the index current at commit `ee3f83de` before this task's commit.

Rollback: revert only the ADR and tracking diff for this task. No schema/data changed here. Future migration must use backup/snapshot and forward-fix; never downgrade live schema blindly.
