# P03-ARCH-ADR-026 Evidence

## Overview
- **Task ID**: `P03-ARCH-ADR-026`
- **Title**: قرارداد BackendProfile و mode مستقل WordPress
- **Date**: 2026-11-14
- **ADR Document**: `docs/architecture/adr/ADR-026-TRUSTED-BACKEND-PROFILES-AND-WORDPRESS-HOST-MODES.md`

## Summary of Changes
1. **ADR Specification**:
   - Authored and approved `docs/architecture/adr/ADR-026-TRUSTED-BACKEND-PROFILES-AND-WORDPRESS-HOST-MODES.md`.
   - Formalized `BackendKind` (`WORDPRESS`, `SPRING`) and WordPress host modes (`THEME_ONLY`, `PLUGIN_ONLY`, `CO_INSTALL`).
   - Enforced trusted `ProductBuildSpec` primacy over query strings, remote manifests, and branding.
   - Defined canonical paths: `/wp-json/carmilla/v1/client-manifest` for WordPress and `/api/v1/manifest` for Spring.
2. **Capabilities Model Enforcement**:
   - `WordPressHostMode` enum added to `core/config/capabilities/src/commonMain/kotlin/com/kazemieh/config/capabilities/ConfigurationModels.kt`.
   - `BackendProfile` enforces that `wordPressHostMode` must be `null` when `kind == BackendKind.SPRING`.
   - `PrivateSessionNamespace` incorporates `hostMode` into the fingerprint for complete isolation across host modes.
   - `BootstrapProfiles` updated to optionally configure `WordPressHostMode`.
3. **Fail-Closed Remote Client Validation**:
   - `RemoteFeatureManifestClient` decodes `hostMode` and validates against profile settings:
     - Spring profiles fail-closed if the manifest carries a `hostMode`.
     - WordPress profiles fail-closed if the manifest's `hostMode` mismatches the profile's mode.
     - Backend, tenant, schema, and unknown feature mismatches all fail-closed.
4. **Comprehensive Automated Tests**:
   - Added unit tests in `BackendProfileTest.kt` verifying rejection of Spring with WordPressHostMode, acceptance of all valid WordPress host modes, and effective host mode resolution.
   - Added unit tests in `PrivateSessionNamespaceTest.kt` verifying fingerprint isolation across `THEME_ONLY`, `PLUGIN_ONLY`, and `CO_INSTALL`.
   - Added unit tests in `RemoteFeatureManifestClientTest.kt` validating host mode matching and fail-closed rejections.

## Automated Verification & Execution Results
All test suites passed cleanly with exit code 0:
1. **Capabilities Tests**:
   - Command: `.\gradlew.bat :core:config:capabilities:jvmTest`
   - Result: `BUILD SUCCESSFUL in 12s` (14 actionable tasks: 5 executed, 9 up-to-date)
   - Exit code: 0
2. **Full Multiplatform Suite & Compilations**:
   - Command: `.\gradlew.bat :core:config:capabilities:jvmTest :core:navigation:jvmTest :core:network:jvmTest :composeApp:jvmTest :composeApp:compileKotlinJs :composeApp:compileKotlinJvm`
   - Result: `BUILD SUCCESSFUL in 37s` (305 actionable tasks: 48 executed, 257 up-to-date)
   - Exit code: 0

## Acceptance & QA Details
- **Technical QA**: PASS. Architectural contracts and code validations are fully verified and guarded against regressions.
- **Manual QA Instruction**:
  - **محیط**: محیط staging وب یا شبیه‌ساز با استفاده از بیلد‌های مختلف.
  - **مراحل**:
    1. اجرای بیلد با یکی از حالت‌های سه‌گانه وردپرس (مثلاً `PLUGIN_ONLY`).
    2. تلاش برای تغییر origin یا mode از طریق query string (مانند `?wpApi=...` یا تغییر پارامترهای URL).
    3. بررسی وضعیت لاگ‌ها و درخواست‌های شبکه.
  - **معیار موفقیت**: کلاینت به هیچ وجه تغییر آدرس یا تغییر mode را از طریق query/remote نمی‌پذیرد، نشست‌ها بین حالت‌های مختلف تداخل ندارند و در صورت ناهماهنگی مانیفست راه‌دور، برنامه به حالت امن (fail-closed) می‌رود.
- **Final Status**: DONE
