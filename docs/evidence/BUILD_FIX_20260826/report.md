# Build Fix Report - JVM Target Inconsistency

## Date: 2026-08-26
## Issue: Inconsistent JVM Target Compatibility Between Java and Kotlin Tasks
The build was failing with the following error:
`Inconsistent JVM-target compatibility detected for tasks 'compileAcademyDebugJavaWithJavac' (11) and 'compileAcademyDebugKotlin' (17).`

## Root Cause
- The `androidApp` module had `compileOptions` set to `JavaVersion.VERSION_11`.
- Kotlin tasks were defaulting to 17 (or set elsewhere), causing a mismatch during compilation.
- Missing dependencies in `androidApp` were also discovered after fixing the JVM issue.

## Changes Made
1.  **androidApp/build.gradle.kts**:
    -   Updated `compileOptions` to `JavaVersion.VERSION_17`.
    -   Added `kotlin { jvmToolchain(17) }` to use JVM Toolchains as recommended.
    -   Added missing dependencies: `project(":core:common")` and `project(":core:designSystem")`.
2.  **Convention Plugins**:
    -   Updated `carmilla.kmp.library.gradle.kts` and `carmilla.compose.application.gradle.kts` to use `jvmToolchain(17)`.
3.  **Source Code**:
    -   Fixed missing import of `BuildConfig` in `ShopApplication.kt`.

## Verification
-   Ran `./gradlew.bat :androidApp:assembleAcademyDebug`
-   Result: **BUILD SUCCESSFUL**

## Impact on Tasks
-   This fix unblocks `P02-CORE-GATE-018` which was failing due to build errors.
