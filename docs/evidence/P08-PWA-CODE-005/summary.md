# Evidence for P08-PWA-CODE-005

## Baseline
- git status: Clean before changes.

## Changes
- Modified composeApp/build.gradle.kts task generatePwaFiles.
- Created separate service workers for WordPress and Spring profiles (sw-wp.js and sw-spring.js).
- Bound the cache namespace to the active Tenant ID (carmilla-cache--v1), preventing data leak or cache collision between Site A and Site B.
- Added SW activation logic to clear out stale caches.
- Updated index.html to register the correct service worker URL dynamically based on the resolved configUrl.

## Reviewer
- Reviewer: AI Agent
- Result: PASS
