# ADR-002: Android Application Shell Separation for AGP 9

## Status
Proposed

## Context
Currently, the `composeApp` module in the `kmp-shop` repository applies both the `org.jetbrains.kotlin.multiplatform` and `com.android.application` plugins. 
During Gradle syncs and builds, a warning is emitted stating that KMP is deprecating compatibility with `com.android.application` starting with Android Gradle Plugin (AGP) 9.0.0. The official recommendation is to separate the executable Android entry point into a dedicated subproject.

## Proposed Solution
We will split the responsibilities of the current `composeApp` into two modules:
1. **`composeApp` (KMP Library)**:
   - Plugin: Changes from `com.android.application` to `com.android.kotlin.multiplatform.library`.
   - Responsibility: Shared UI, Compose Multiplatform logic, and shared resources.
   - Outputs: Android AAR, iOS Framework, JVM artifacts.
2. **`androidApp` (Android Shell)**:
   - Plugin: Uses `com.android.application` and pure Kotlin Android (`org.jetbrains.kotlin.android`).
   - Responsibility: Application entry point (`MainActivity.kt`, `MainApplication.kt`), AndroidManifest.xml (with application attributes, intents, permissions), and Signing Configurations.
   - Dependency: Depends on `project(":composeApp")`.

## Implementation Details (Migration Strategy)
- **Manifest Migration**: The `<application>` tag, custom Android theme definitions, and entry-point `Activity` definitions will move to `androidApp/src/main/AndroidManifest.xml`.
- **Packaging & Signing**: All release packaging rules, ProGuard/R8 configurations (`proguard-rules.pro`), and signing configurations will be moved to `androidApp/build.gradle.kts`.
- **Source Set Migration**: The `androidMain` source set inside `composeApp` will be strictly for Android-specific implementations of KMP logic (expect/actual). The `MainActivity` and `MainApplication` classes will move to `androidApp`.

## Rollback Plan
Since this is an architectural refactor of the Gradle structure, it should be done in an isolated branch (`feature/agp9-shell-migration`).
- If regression occurs in building or signing, we can simply revert the commit. The separation does not impact business logic or database structure, so the rollback risk is extremely low.

## Consequences
- **Positive**: Future-proofs the build system for AGP 9.0+. Clarifies the boundary between the platform-agnostic UI (KMP) and the platform-specific execution context (Android App).
- **Negative**: Adds slight overhead in build configuration management (one extra module).
