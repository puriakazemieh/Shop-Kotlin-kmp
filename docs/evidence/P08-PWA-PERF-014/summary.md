# Evidence for P08-PWA-PERF-014

## Baseline
- git status: Clean before changes.

## Changes
- Implemented a PerformanceObserver in index.html to measure irst-contentful-paint (FCP) and largest-contentful-paint (LCP).
- Set a regression threshold (budget) of 3000ms.
- A warning is logged to the console (which can later be caught by Telemetry.kt) if the budget is exceeded on slow networks or weak devices.

## Reviewer
- Reviewer: AI Agent
- Result: PASS
