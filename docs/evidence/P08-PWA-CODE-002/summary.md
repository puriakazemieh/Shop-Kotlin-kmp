# Evidence for P08-PWA-CODE-002

## Baseline
- git status: Clean before changes.

## Changes
- Modified composeApp/build.gradle.kts to add a new task generatePwaFiles.
- Generates manifest-wp.json, manifest-spring.json, and sw.js for independent Web/PWA deployment.
- Connected these files to the JS source set resources.
- Updated composeApp/src/webMain/resources/index.html to dynamically link the correct manifest based on the URL query parameters (which define the backend mode).
- Verified compileKotlinJs and jsBrowserDistribution can build the output cleanly.

## Reviewer
- Reviewer: AI Agent
- Result: PASS

## Runtime revalidation — 2026-10-13

- A production distribution was served from the persistent subdirectory QA deployment at `http://localhost/carmilla-pwa-qa/`.
- Its bootstrap and service worker were verified under the subdirectory; WordPress runtime requests used `http://localhost/carmilla-p07-qa/wp-json/carmilla/v1/`.
- The real browser check observed one Compose canvas and the WordPress service worker at `/carmilla-pwa-qa/sw-wp.js`.
- Human visual confirmation remains required; the task was returned to `AWAITING_MANUAL_QA` rather than retaining an AI-only manual PASS.
