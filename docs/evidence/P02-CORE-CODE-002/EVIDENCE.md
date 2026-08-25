# Evidence for P02-CORE-CODE-002

## Baseline
- Commit: `ad746d94` (develop)
- Date: 2026-08-25

## Changes Summary
Moved all hardcoded dependency versions to the Gradle Version Catalog (`libs.versions.toml`).

### Versions added to `[versions]`
| Key | Value |
|-----|-------|
| `ktor` | `3.3.3` |
| `coil` | `3.0.4` |
| `composeMaterialIcons` | `1.7.3` |
| `imageLoader` | `1.9.0` |
| `pagingCompose` | `3.4.0-rc01` |

### Libraries added to `[libraries]`
- `kotlinx-coroutines-core` → version.ref `kotlinx-coroutines`
- `ktor-client-core`, `ktor-client-content-negotiation`, `ktor-serialization-kotlinx-json`, `ktor-client-logging`, `ktor-client-auth`, `ktor-client-android`, `ktor-client-darwin`, `ktor-client-js`, `ktor-client-cio` → version.ref `ktor`
- `coil-compose` → version.ref `coil`

### Inline versions fixed in `[libraries]`
- `compose-material-icons-core` → version.ref `composeMaterialIcons`
- `compose-material-icons-extended` → version.ref `composeMaterialIcons`
- `image-loader` → version.ref `imageLoader`
- `paging-compose-multiplatform` → version.ref `pagingCompose`

### build.gradle.kts files updated (hardcoded → libs.*)
- `core/common/build.gradle.kts` — coroutines
- `core/data/build.gradle.kts` — coroutines
- `core/domain/build.gradle.kts` — coroutines
- `core/network/build.gradle.kts` — ktor (9 deps) + coroutines
- `feature/admin/options/build.gradle.kts` — coil
- `feature/admin/orders/build.gradle.kts` — coil
- `feature/admin/products/build.gradle.kts` — coil
- `feature/admin/wallet/build.gradle.kts` — coil
- `feature/cart/build.gradle.kts` — coil
- `feature/catalog/build.gradle.kts` — coil
- `feature/main/build.gradle.kts` — coil
- `feature/settings/build.gradle.kts` — coil

## Verification
- `./gradlew.bat :composeApp:compileKotlinJvm` → exit code 0 (BUILD SUCCESSFUL in 1m 34s)
- No dependency resolution changes — same versions, just centralized.
- Manual test: N/A (no UI/network/migration change)
