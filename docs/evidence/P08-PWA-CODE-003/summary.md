# Evidence for P08-PWA-CODE-003

## Baseline
- git status: Clean before changes.

## Changes
- Modified generatePwaFiles Gradle task to produce pp-config-wp.json and pp-config-spring.json.
- These JSONs act as the trusted production ceiling, locking the piRoot, llowedAuthHosts, and eatures based on products.json.
- Updated index.html to inject this configuration asynchronously before instantiating Compose Web.
- Updated main.kt to drop query parameter based API routing in favor of reading the trusted window.appConfig object.

## Reviewer
- Reviewer: AI Agent
- Result: PASS
