# Evidence for P08-QA-AUTO-015B

## Baseline

- Baseline commit: `70a62158`.
- `P08-QA-AUTO-015A` supplied the Playwright matrix and the non-production reporter classification.
- Unrelated untracked user files were present before this task and were not edited or staged.

## Delivered tests

- Added a local PWA fixture server that reads the shipped web `index.html`. It exposes CSP and Referrer-Policy only when those policies are declared by that source file, so a removed policy makes the browser assertion fail.
- Added browser tests for CSP and `strict-origin-when-cross-origin` Referrer-Policy on both desktop and mobile fixture projects.
- Added a CDP slow-network condition (3,000 ms latency, 50 KiB/s transfer) and deterministic FCP/LCP entries at 3,001 ms. The test intercepts the source page's `Performance Budget Exceeded:` warnings and fails if either metric does not cross the 3,000 ms threshold.
- Added the optional `E2E_BROWSER_CHANNEL` override. It enables the local system Chrome run without changing CI's default bundled-Chromium behavior.

## Verification

| Command | Exit | Result |
|---|---:|---|
| `$env:E2E_BROWSER_CHANNEL = 'chrome'; npm run test:e2e:fake` | 0 | 6 tests passed: security policy, performance budget, and infrastructure on fake desktop/mobile. |
| `$env:E2E_BROWSER_CHANNEL = 'chrome'; npm run test:e2e` | 0 | 6 fixture tests passed; 12 staging/production tests skipped. |
| `git diff --check` | 0 | No whitespace error. |

The ignored JSONL report marks all six fixture passes as `FIXTURE_PASS_NOT_PRODUCTION`; all staging and production entries are `skipped`. No real endpoint, production request, credential, customer data, SKU, or build fingerprint was used.

## Manual QA

Not required for this fixture-only automated test task. The future manual PWA task must run against a real PWA artifact and hosting configuration; it must verify the actual response headers, network behavior, and performance on a supported physical browser/device.

## Remaining risk and rollback

- The source currently declares security policies as HTML metadata. This task prevents regressions in those source declarations, but a production host must independently emit and validate response headers during release QA.
- No staging/production environment identity was supplied, so these tests intentionally do not claim real-environment coverage.
- Roll back by reverting this task's commit. No database, customer data, server configuration, or production state changed.
