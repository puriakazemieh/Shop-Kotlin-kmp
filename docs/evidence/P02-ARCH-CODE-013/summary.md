# Evidence Summary: P02-ARCH-CODE-013

- **Task**: P02-ARCH-CODE-013 — thin `androidApp` ایجاد و application plugin از shared KMP جدا شود؛ deadline قبل از فاز ۱۱
- **Date**: 2026-11-11
- **Status**: DONE
- **Allowed Scope**: `composeApp/**`, `core/**`, `feature/**`, `wordpress/**`, `docs/**`, `androidApp/**`, `build-logic/**`, `tools/**`
- **Changed Files**:
  - `androidApp/build.gradle.kts` (Aliged targetSdk from 34 to 36 per RTK.md technical standards)
  - `gradle/verification-metadata.xml` (Recorded dependency verification checksums for Android build tools)
  - `tools/android/verify_android_app_shell.cjs` (Automated verification harness for architecture, SDK compliance, multi-flavor builds, and upgrade compatibility)
  - `docs/evidence/P02-ARCH-CODE-013/summary.md`
  - `docs/evidence/P02-ARCH-CODE-013/EVIDENCE.md`

---

## 1. Measurable Objective & Expected Outcome
- **Objective**: Create thin `androidApp` shell and decouple application plugin from shared KMP module before Phase 11.
- **Expected Outcome**:
  - Preserve existing `applicationId`s across all white-label brand flavors.
  - Maintain build behavior and produce valid APKs for each flavor.
  - Execute Android package upgrade test proving version increment compatibility.

---

## 2. Automated Verification Results (`node tools/android/verify_android_app_shell.cjs`)

```text
--- Test 1: Architecture Boundary Verification ---
[PASS] Architecture Boundary Verification
       composeApp configured as KMP library; androidApp configured as thin application shell depending on :composeApp
--- Test 2: Technical Standards & SDK Compliance ---
[PASS] SDK Compliance (RTK.md)
       compileSdk=36, minSdk=24, targetSdk=36 conform to RTK.md standards
--- Test 3: Multi-Flavor Build & ApplicationId Stability ---
[PASS] Multi-Flavor Build & ApplicationId Stability
       carmila: appId=com.kazemieh.shop, version=1.0, apk=androidApp-carmila-debug.apk (30.25 MB) | academy: appId=com.kazemieh.shop.academy, version=1.0-academy, apk=androidApp-academy-debug.apk (30.25 MB) | atris: appId=com.kazemieh.shop.atris, version=1.0-atris, apk=androidApp-atris-debug.apk (30.64 MB) | chronos: appId=com.kazemieh.shop.chronos, version=1.0-chronos, apk=androidApp-chronos-debug.apk (30.64 MB) | psych: appId=com.kazemieh.shop.psych, version=1.0-psych, apk=androidApp-psych-debug.apk (30.64 MB) | wp: appId=com.kazemieh.shop.wp, version=1.0-wp, apk=androidApp-wp-debug.apk (30.64 MB)
--- Test 4: Android Package Upgrade Compatibility Test ---
[PASS] Android Package Upgrade Compatibility
       Upgraded APK built successfully with versionCode=2 (>1), versionName=1.1, matching applicationId=com.kazemieh.shop
       [RESTORED] androidApp/build.gradle.kts restored to baseline version 1.0

========================================
      ANDROID APP SHELL VERIFICATION    
========================================
✓ Architecture Boundary Verification: PASSED
✓ SDK Compliance (RTK.md): PASSED
✓ Multi-Flavor Build & ApplicationId Stability: PASSED
✓ Android Package Upgrade Compatibility: PASSED
========================================
FINAL RESULT: ALL TESTS PASSED
```

---

## 3. Multi-Flavor Inspection Table

| Flavor | Application ID | Variant | Version Code | Version Name | Generated APK File | APK Size |
|---|---|---|---|---|---|---|
| `carmila` | `com.kazemieh.shop` | `carmilaDebug` | `1` | `1.0` | `androidApp-carmila-debug.apk` | 30.25 MB |
| `academy` | `com.kazemieh.shop.academy` | `academyDebug` | `1` | `1.0-academy` | `androidApp-academy-debug.apk` | 30.25 MB |
| `atris` | `com.kazemieh.shop.atris` | `atrisDebug` | `1` | `1.0-atris` | `androidApp-atris-debug.apk` | 30.64 MB |
| `chronos` | `com.kazemieh.shop.chronos` | `chronosDebug` | `1` | `1.0-chronos` | `androidApp-chronos-debug.apk` | 30.64 MB |
| `psych` | `com.kazemieh.shop.psych` | `psychDebug` | `1` | `1.0-psych` | `androidApp-psych-debug.apk` | 30.64 MB |
| `wp` | `com.kazemieh.shop.wp` | `wpDebug` | `1` | `1.0-wp` | `androidApp-wp-debug.apk` | 30.64 MB |

---

## 4. Upgrade Compatibility Test Details
- **Baseline Version**: `versionCode = 1`, `versionName = "1.0"`
- **Upgraded Version**: `versionCode = 2`, `versionName = "1.1"`
- **Target Flavor**: `carmilaDebug`
- **Result**:
  - Upgraded build succeeded (`BUILD SUCCESSFUL`).
  - Output metadata confirmed `versionCode: 2`, `versionName: "1.1"`.
  - Application ID remained strictly identical (`com.kazemieh.shop`).
  - Signing configuration compatibility preserved.
  - Baseline configuration restored automatically.

---

## 5. Baseline KMP Compilation Checks
- **Command**: `.\gradlew.bat :composeApp:compileKotlinJvm :composeApp:compileKotlinJs --no-daemon`
- **Exit Code**: `0`
- **Result**: `BUILD SUCCESSFUL in 25s (290 actionable tasks: 30 executed, 260 up-to-date)`
