# P02-QA-CODE-006 verification — 2026-10-28

## Scope and baseline

- Baseline commit: `2c540eeb` (`develop`). `P02-CORE-CODE-005C` is `DONE`; this card was the first P02 `TODO` and was made `READY` by explicit owner request.
- Size assessed as M: one domain test, one network contract test and wiring their JVM tests into the existing root `test` command. No production behavior, dependency version, workflow file or server source was changed.
- Existing user changes and untracked files were preserved. The server repository had a pre-existing modified `Shop/AGENTS.md`; it was not edited.
- GitNexus CLI upstream impact for the two test classes returned `UNKNOWN` with zero resolved callers. Text search found the classes only in their test files; dynamic Gradle/JUnit discovery is the relevant caller. No production symbol was edited.

## Implementation

- Replaced constant assertions in `EmailValidatorTest` with three real `ValidateEmail` cases: valid, malformed and blank input.
- Replaced constant assertion in `KtorClientTest` with two commonTest network contract cases: WordPress subdirectory API path and Spring root API path, including approved and rejected origins.
- Added a `test` alias in `core/domain` and `core/network` depending on `jvmTest`. This makes the existing PR gate command `./gradlew test` select these KMP tests without changing `.github/**`, which this task card does not allow.
- Tests use synthetic addresses only; no fixture database or customer data is needed.

## Commands and results

| Command | CWD | Exit | Evidence |
|---|---|---:|---|
| `./gradlew.bat :composeApp:compileKotlinJvm --console=plain` | KMP root | 0 | Baseline `BUILD SUCCESSFUL` in 13s. |
| `./gradlew.bat :core:domain:test :core:network:test --console=plain` | KMP root | 0 | `BUILD SUCCESSFUL` in 29s. JUnit XML: `EmailValidatorTest` 3/0 failures; `KtorClientTest` 2/0 failures. |
| `./gradlew.bat test --dry-run --console=plain` | KMP root | 0 | Includes `:core:domain:jvmTest`, `:core:domain:test`, `:core:network:jvmTest`, `:core:network:test`. |
| `./gradlew.bat test :composeApp:compileKotlinJs :composeApp:compileKotlinJvm :androidApp:assembleCarmilaDebug --console=plain` | KMP root | 0 | `BUILD SUCCESSFUL` in 5m 51s; 974 actionable tasks. |
| `./gradlew.bat :core:domain:compileTestKotlinJs :core:network:compileTestKotlinJs --console=plain` | KMP root | 0 | `BUILD SUCCESSFUL` in 15s. |
| `./gradlew.bat bootJar --console=plain` | ShopServer/Shop | 0 | `BUILD SUCCESSFUL` in 32s. Server source was not changed by this task. |

Local test reports: `core/domain/build/test-results/jvmTest/` and `core/network/build/test-results/jvmTest/`.

## Build artifacts supplied to owner

| File | Size | SHA-256 |
|---|---:|---|
| `.artifacts/P02-QA-CODE-006-20261028/androidApp-carmila-debug.apk` | 31,715,788 bytes | `EDE25C86B61793831D2578024FDC2999526F73DD4231AB473CE455E7BD40B7FA` |
| `.artifacts/P02-QA-CODE-006-20261028/Shop-0.0.1-SNAPSHOT.jar` | 79,924,883 bytes | `7873316948A47C829825EE898A6E62D2D45E1DE4088F4DD3FB930BEBF2AC1910` |

The server JAR embeds `application.properties`; sensitive setting keys use environment placeholders or empty values. The APK is a debug build for synthetic/local QA, not a signed release. Neither artifact contains task-specific data.

## Pending external acceptance

The repository's existing PR workflow runs `./gradlew test`, and the local dry run proves both JVM suites are selected. No actual GitHub PR job has been observed for this revision. Leave the task `AWAITING_MANUAL_QA` and the Master checkbox open until a reviewer verifies the CI run:

1. On a PR targeting `develop` or `main` containing this task commit, open **Actions → PR Gate → validate → Run Unit Tests**.
2. Confirm the job runs `:core:domain:jvmTest` and `:core:network:jvmTest`, both complete successfully, and the PR check is green. Save the run URL, commit SHA, date and redacted job log in this Evidence directory.
3. On a disposable PR branch, temporarily make one new test fail, confirm the same required check turns red and blocks merge, then revert that temporary change. Save the red run URL/log. Do not merge the disposable failure.

No device manual test is required for this task because only tests and Gradle test wiring changed; application and network production behavior did not change. The Android APK is provided for optional smoke testing, not as acceptance evidence for this card.

## Rollback

Revert only the task commit after it exists, or remove the two `test` aliases and restore the prior test files. This task introduces no migration or data change.
