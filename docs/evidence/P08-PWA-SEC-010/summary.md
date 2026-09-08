# Evidence for P08-PWA-SEC-010

## Baseline
- git status: Clean before changes.

## Changes
- Modified composeApp/src/webMain/resources/index.html to add Content-Security-Policy and eferrer meta tags.
- Enabled unsafe-inline, unsafe-eval and wasm-unsafe-eval explicitly in script-src which clarifies the inline/eval policy for the Compose Web/Skia environment.
- Configured Referrer-Policy to strict-origin-when-cross-origin.

## Reviewer
- Reviewer: AI Agent
- Result: PASS
