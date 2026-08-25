# ADR-003: Convention Plugin Scope & Approved Plugin List

## Status
**Accepted** — 2026-08-25

## Context
The Carmilla KMP project has 29 Gradle modules (6 core + 22 feature + 1 app). Currently:
- No convention plugins exist (`build-logic/` or `buildSrc/` absent).
- Each module's `build.gradle.kts` independently declares its plugin set and configuration.
- The Version Catalog (`libs.versions.toml`) centralizes versions but not build configuration.

Repeating the same KMP/Compose/Android setup across 29 modules causes:
1. Copy-paste drift (e.g., different `minSdk`, `compileSdk`, or Compose compiler settings).
2. Risk of accidentally applying unauthorized plugins.
3. Difficulty enforcing consistent lint, test, and publishing configuration.

## Decision

### Convention Plugin Scope
Convention plugins **SHOULD be introduced incrementally** via a `build-logic/` included build.
Each convention plugin covers one concern:

| Convention Plugin | Responsibility |
|---|---|
| `carmilla.kmp.library` | KMP library module setup (kotlin-multiplatform + targets + common config) |
| `carmilla.android.library` | Android-specific defaults (compileSdk, minSdk, Java version) |
| `carmilla.compose` | Compose Multiplatform + Compiler plugin configuration |
| `carmilla.feature` | Feature module convention = kmp.library + compose + koin DI |

### Approved Gradle Plugins (Allowlist)

| Plugin ID | Purpose | Version Source |
|---|---|---|
| `org.jetbrains.kotlin.multiplatform` | KMP targets | Version Catalog |
| `org.jetbrains.kotlin.plugin.compose` | Compose compiler | Version Catalog |
| `org.jetbrains.kotlin.plugin.serialization` | kotlinx.serialization | Version Catalog |
| `org.jetbrains.compose` | Compose Multiplatform resources/runtime | Version Catalog |
| `com.android.application` | App module only | Version Catalog |
| `com.android.library` | Android library target | Version Catalog |
| `com.android.kotlin.multiplatform.library` | Android KMP library target | Version Catalog |
| `com.android.lint` | Static analysis | Version Catalog |
| `org.jetbrains.compose.hot-reload` | Dev-only hot reload | Version Catalog |

### Prohibited / Not-Yet-Approved Plugins
- **KSP**: Not currently used; must be evaluated before adoption.
- **Hilt/Dagger**: Project uses Koin; Hilt is not compatible.
- **Google Services / Firebase**: Not integrated; requires separate ADR.
- **Any plugin not in the allowlist above**: Requires team approval + Version Catalog entry.

### Guard Rule
Any `build.gradle.kts` applying a plugin NOT in the approved list should be flagged in code review.
Until `build-logic/` is implemented (P02-CORE-CODE-004+), this is enforced by convention and review only.

## Consequences
- **Positive**: Single source of truth for module setup; prevents unauthorized plugin usage.
- **Negative**: Upfront work to create `build-logic/` (deferred to P02-CORE-CODE-004).
- **Migration**: Existing modules will adopt convention plugins incrementally, not all at once.
