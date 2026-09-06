# P03-ARCH-CODE-024B Evidence

## Changes
- Updated `initKoin` in `App.kt` to accept `sku` and `apiBaseUrlOverride` instead of a hardcoded `brand` parameter.
- Removed hardcoded backend selection from `initKoin`. It now dynamically resolves `ProductBuildSpec`, `TenantConfig`, `BrandConfig`, and `BackendProfile` based on the specified SKU.
- `privateSessionNamespace` correctly uses `kind, tenantId, apiRoot` combining `backend+tenant+origin`.
- Modified `ShopApplication.kt` (Android) to inject `BuildConfig.BRAND` as `sku`.
- Modified `main.kt` (Web) to resolve `sku` instead of `BrandConfig`.
- Modified `main.kt` (JVM) to inject `sku` via `System.getProperty("brand")`.
- Modified `MainViewController.kt` (iOS) to invoke `initKoin(sku = "carmila")` by default.

## Verification
- JVM and JS compilation verified successfully via `./gradlew.bat :composeApp:compileKotlinJs :composeApp:compileKotlinJvm`.
- Tests for `core:config:capabilities`, `core:navigation`, and `core:network` passed via `./gradlew.bat :core:config:capabilities:jvmTest :core:navigation:jvmTest :core:network:jvmTest`.
- Android app build tested via `./gradlew.bat :androidApp:assembleDebug`.

## Automated Tests
- `./gradlew.bat :composeApp:compileKotlinJs :composeApp:compileKotlinJvm` exit code: 0
- `./gradlew.bat :core:config:capabilities:jvmTest :core:navigation:jvmTest :core:network:jvmTest` exit code: 0
- `./gradlew.bat :androidApp:assembleDebug` exit code: 0

## Manual QA
- Environment: Staging environment on Android Emulator.
- Verification: Boot up `carmila` or `wp` build variants.
- Success Criteria: Ensure the app boots correctly, resolving branding properly, while preserving `tenant logout/cache purge` behaviors. No regressions in deep links or startup.
- Status: AWAITING_MANUAL_QA
