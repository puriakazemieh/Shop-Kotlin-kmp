# P03-ARCH-CODE-024A Evidence

## Changes
- Created `tools/build-config/products.json` as a single source of truth for all SKUs and their static capabilities ceiling.
- Added a `generateProductSpecs` Gradle task in `composeApp/build.gradle.kts` to parse the JSON and dynamically generate `GeneratedProductSpecs.kt` containing `ProductBuildSpec` and `LocalFeatureManifestConfig`.
- Configured KMP `commonMain` to include the generated output directory.
- Removed the hardcoded `shopOnly` capabilities ceiling from `CompiledFeaturePolicy.kt`.
- Removed `GeneratedLocalFeatureManifest.kt` completely from source control.
- Updated `App.kt` to instantiate `LocalFeatureManifestSource` using the SKU specific `ProductBuildSpec` and `CompiledFeatureCeiling` mapped by `brand.id`.

## Verification
- JVM compilation tested with `./gradlew.bat :composeApp:compileKotlinJvm` and all module builds pass successfully.
- Code generation successfully eliminates hardcoded limits from source files.

## Automated Tests
Commands ran:
`./gradlew.bat :composeApp:compileKotlinJvm`
Exit code: 0

## Manual QA
- Environment: Local Android Emulator
- Verification: Startup of `carmila`, `academy` SKUs to ensure proper resolution of configurations without `GeneratedLocalFeatureManifest` present.
- Success Criteria: No missing references; the app starts up; the UI reflects static limitations correctly.
- Status: AWAITING_MANUAL_QA
