# Evidence: P03-MANIFEST-CODE-025A — اتصال منو و route/deep link به وضعیت مؤثر

## Overview
- **Task ID**: `P03-MANIFEST-CODE-025A`
- **Scope**: `core/navigation/**`, `feature/main/**`
- **Goal**: Connect navigation menus, route guard, and deep links to live `EffectiveFeatureStore` reactive state. Prevent unauthorized routes from opening on restart/deep-link, dynamically eject users from newly disabled features without rebuild, and prevent back stack re-entry into disabled screens.

## Changes Made
1. **`FeatureRouteGuard.kt` (`core/navigation`)**:
   - Added `checkRoute(destination: Any): RouteGuardDecision` to support type-safe navigation route objects (`Screen.*`) across all Kotlin targets without reflection limitations on Kotlin/JS.
   - Connected route evaluations directly to `EffectiveFeatureStore.features.value` so any manifest update instantly affects routing decisions.
   - Expanded route recognition to include `Emergency` and `ReturnRequest`.
   - Added `isFeatureEnabled(featureId: String)` helper.

2. **`AppNavigation.kt` (`core/navigation`)**:
   - Resolved start destination dynamically: if `startDestination` (from deep links or initial hash) targets a disabled feature, it resolves directly to `Screen.FeatureUnavailable(decision.featureId)` preventing the forbidden screen and its ViewModels from initializing.
   - Added `composable<Screen.FeatureUnavailable>` destination into `NavHost` displaying clear Persian explanation and a return button navigating safely to `Screen.HomeGraph`.
   - Connected `LaunchedEffect(features, currentRoute)` to immediately eject active users to `Screen.FeatureUnavailable` when a feature is toggled off in the live store.
   - Added `OnDestinationChangedListener` to intercept back-stack popping so back navigation never re-enters a newly disabled screen.

3. **`MainGraphScreen.kt`, `MoreScreen.kt`, `BottomBar.kt`, `SideNavRail.kt` (`feature/main`)**:
   - Connected `MainGraphScreen` bottom bar and side rail to `features`: when `commerce.core` is disabled, the `Cart` tab is hidden from `BottomBar` and `SideNavRail`.
   - Added automatic ejection from `Screen.Cart` back to `Screen.ProductsOverview` if `commerce.core` is dynamically disabled while on Cart.
   - Connected `MoreScreen` items (`showRecurringOrders`, `showShoppingAssistant`, and `isAdmin && features.isEnabled("admin.mobile")`) to live `EffectiveFeatureStore` features.

4. **Automated Verification (`core/navigation/jvmTest`)**:
   - Updated `FeatureRouteGuardTest.kt` with comprehensive tests for type-safe route objects, live reactive updates on `DefaultEffectiveFeatureStore`, and deep link path evaluation.

## Verification & Commands Executed
All commands executed in `D:/Android/AndroidStudioProjects/kmp-shop`:

1. `./gradlew.bat :core:config:capabilities:jvmTest :core:navigation:jvmTest :core:network:jvmTest`
   - **Exit code**: 0
   - **Result**: All tests passed including `FeatureRouteGuardTest`, `P03ManifestMatrixTest`, `NavigationCharacterizationTest`, and `FeatureDependencyInversionTest`.

2. `./gradlew.bat :composeApp:compileKotlinJs :composeApp:compileKotlinJvm`
   - **Exit code**: 0
   - **Result**: Kotlin JS and JVM compilation succeeded with full multiplatform parity.

3. `./gradlew.bat :androidApp:assembleDebug`
   - **Exit code**: 0
   - **Result**: All Android variants (`AcademyDebug`, `AtrisDebug`, `CarmilaDebug`, `ChronosDebug`, `PsychDebug`, `WpDebug`) assembled successfully.

4. `./gradlew.bat :composeApp:jvmTest`
   - **Exit code**: 0
   - **Result**: `GeneratedProductSpecsTest` and composeApp integration tests passed.

## Acceptance Criteria
- [x] Menu toggles and deep links reflect live effective feature state without rebuild.
- [x] Restart and back stack cannot open forbidden routes (`OnDestinationChangedListener` and upfront `resolvedStartDestination`).
- [x] Type-safe routes and deep-link mappings work across JVM, JS, and Android.
- [x] Scope boundary maintained strictly within allowed modules (`core/navigation`, `feature/main`).
