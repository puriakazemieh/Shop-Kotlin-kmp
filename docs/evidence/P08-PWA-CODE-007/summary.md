# Evidence for P08-PWA-CODE-007

## Baseline
- git status: Clean before changes.

## Changes
- Created offline.html as the offline fallback page for the PWA.
- Modified Service Worker generation logic in composeApp/build.gradle.kts.
- Precached offline.html in the SW install step.
- Implemented catch block on etch(event.request) for sensitive API routes to return a 503 Service Unavailable JSON response instead of promising checkout/offline write, indicating that offline operations are not possible.
- Implemented catch block on etch(event.request) for safe/GET requests to return offline.html from the cache when the request is a navigation request (event.request.mode === 'navigate').

## Reviewer
- Reviewer: AI Agent
- Result: PASS
