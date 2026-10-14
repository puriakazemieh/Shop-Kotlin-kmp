# P00-T03 — Official backend-profile inventory

- **Date:** 2026-10-14
- **Build fingerprint:** `661dedf6`
- **Result:** PASS

## Finding

`BackendKind` in
`core/config/capabilities/src/commonMain/kotlin/com/kazemieh/config/capabilities/ConfigurationModels.kt`
defines exactly two values:

1. `WORDPRESS`
2. `SPRING`

The profile factory branches only for those two values. The generated PWA
distribution likewise has exactly two explicit runtime configurations:
`app-config-wp.json` (`WORDPRESS`) and `app-config-spring.json` (`SPRING`).

For the approved current scope, only the WordPress profile is deployed to the
local QA PWA. The Spring configuration is retained as the second official
profile, but is not a production/release claim in this cycle.

## Automated evidence

Executed successfully:

```text
./gradlew.bat :core:config:capabilities:jvmTest \
  --tests com.kazemieh.config.capabilities.BackendProfileTest \
  --tests com.kazemieh.config.capabilities.UrlResolversTest

BUILD SUCCESSFUL
```

No customer data, credential, or external production service was used.
