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
