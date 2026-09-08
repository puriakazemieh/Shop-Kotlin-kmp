# Evidence for P08-PWA-CODE-006

## Baseline
- git status: Clean before changes.

## Changes
- Modified Service Worker generation logic in composeApp/build.gradle.kts.
- Introduced SENSITIVE_PATHS containing /auth, /order, /payment, /message, /health, and /wp-json/cb/v1/auth.
- Updated etch listener to enforce a **Network-only** policy for sensitive endpoints (and any non-GET requests).
- Updated install listener to explicitly precache App Shell resources (index.html, JS/CSS, Icons).
- Updated etch listener to dynamically cache public API responses only if they are not sensitive and return HTTP 200.

## Reviewer
- Reviewer: AI Agent
- Result: PASS
