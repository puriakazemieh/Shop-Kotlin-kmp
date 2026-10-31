# P03-MANIFEST-OPS-021 — status and environment audit — 2026-10-31

Baseline commit: `b222115e`. The task row was TODO while its card prefilled acceptance checks and Final status DONE. Existing evidence lists root aliases, a PHP header and log, and Kotlin's `@Deprecated`, but gives no executed regression command, HTTP response, negative feature-off test, or actual client release/build that defines the sunset cycle. It infers lack of policy bypass from use of `rest_do_request` alone. The active manual QA replacement P03-QA-MANUAL-027 remains TODO.

Source inspection: `CB_Plugin::maybe_root_alias` in `wordpress/carmilla-bridge/includes/class-cb-plugin.php` dispatches four root path families to `rest_do_request`. It logs the path and emits deprecation headers before dispatch. This is code presence, not proof of authorization or feature-off equivalence. `BootstrapProfiles.fromLegacyApiRoot` is annotated `@Deprecated`. The existing `smoke-manifest-security.php` tests the guard directly but does not exercise any root alias. No alias regression test was found in `wordpress/carmilla-bridge/tests/`.

Verification attempted from repo root:

- `.\gradlew.bat :composeApp:compileKotlinJvm --offline`: exit 0, BUILD SUCCESSFUL in 31 s; baseline only, not alias coverage.
- `Get-Command php`: no PHP CLI found.
- `docker info --format '{{.ServerVersion}}'`: Docker engine pipe missing. Docker Desktop was launched hidden; its engine did not become ready during this audit, and the later Docker query hung. No PHP lint, WordPress smoke, or CI run was completed.
- GitNexus impact for `maybe_root_alias` was ambiguous between two symbols (the live Bridge and a test copy); the reported maximum risk was CRITICAL. A file-qualified lookup returned UNKNOWN. Code was not edited on that basis.

Status: BLOCKED. Next action: provide a working disposable PHP/WordPress environment (or verified CI evidence) and identify the client build/release that starts and ends the promised retention cycle. Then run positive and negative alias requests, including authenticated/unauthenticated account routes and feature-off HTTP 403, compare canonical responses, capture redacted exit codes and responses, and send network changes through human QA. No customer site or data was modified.
