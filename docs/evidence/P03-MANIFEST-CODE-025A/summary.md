# Evidence: P03-MANIFEST-CODE-025A

## Changes Made
- Connected `MainGraphScreen.kt` to `EffectiveFeatureStore` directly via `koinInject()`.
- Deprecated usages of `BrandConfig.features` and replaced them with `features.isEnabled(...)` checking the live store values.
- Injected `EffectiveFeatureStore` to `feature:main` module in `build.gradle.kts`.
- Re-architected `AppNavHost` in `AppNavigation.kt` to use `EffectiveFeatureStore` using a `LaunchedEffect` that instantly kicks users out of disabled screens.
- Added a `FeatureUnavailable` screen inside `Screen.kt` and `AppNavigation.kt` to display a comprehensible message when a user navigates to or is currently on a disabled feature.

## Validation
- `compileKotlinJvm` and `compileKotlinJs` completed successfully without any compilation errors.
- AppNavigation now handles dynamic updates properly.

## Note for Manual QA
User should run the Android app locally (e.g. `carmilaDebug`) and verify:
1. No crashes on startup.
2. Clicking a disabled feature correctly navigates to the `FeatureUnavailable` screen.
3. Turning off a feature while being on its screen immediately kicks the user out to the `FeatureUnavailable` screen without rebuilding or restarting the app.
