# Evidence for P08-PWA-CODE-012

## Baseline
- git status: Clean before changes.

## Changes
- Updated the Service Worker (uild.gradle.kts templates) etch listener to intercept all 
avigate requests.
- For 
avigate requests (e.g. user manually refreshing or entering a deep link URL like /products/123), the Service Worker now responds with the cached /index.html.
- This ensures SPA client-side routing works for internal routes without hitting the server for every path (which would return 404 on static hosts without rewrite rules).
- If /index.html is missing, it falls back to network, and then to /offline.html.

## Reviewer
- Reviewer: AI Agent
- Result: PASS
