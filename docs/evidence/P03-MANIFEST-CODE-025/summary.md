# Evidence: P03-MANIFEST-CODE-025

## Changes Made
- Created `EffectiveFeatureStore` (interface and default implementation) in `core/config/capabilities` to act as a reactive central store for the active feature policy.
- Modified `FeatureManifestBootstrapCoordinator` results to be passed into `EffectiveFeatureStore` inside `App.kt` upon load and retry.
- Updated `FeatureUseCaseGuard` and `FeatureRouteGuard` to use `EffectiveFeatureStore` natively, dynamically checking `store.features.value.isEnabled(...)`.
- Retained old `ResolvedFeatures` constructor with `@Deprecated` for backwards compatibility.
- Updated `FeatureFlagShadowMode` to return the new valid policy (`manifest`) in the active path, instead of `legacy`.
- Fixed multiple unit tests in `core/config/capabilities/src/jvmTest/` that were broken by previous tasks.

## Validation
- `compileKotlinJvm` and `compileKotlinJs` completed successfully without any compilation errors.
- Tests in `core/config/capabilities/jvmTest` successfully pass (31 tests passed).
- Old unit tests updated to correctly supply the `ceiling` dependency.

## Note for Manual QA
User should run the Android app locally (e.g. `carmilaDebug`) and verify:
1. No crashes on startup (DI works as expected).
2. Remote flags are successfully applied (shadow mode now passes the new flags correctly).
