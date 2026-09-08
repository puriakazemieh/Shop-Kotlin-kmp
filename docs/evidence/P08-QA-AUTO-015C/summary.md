# Evidence for P08-QA-AUTO-015C

## Baseline and discovery

- Baseline commit: `96354cb3`.
- The first manifest-parser execution failed because `index.html` contained two `<link id="pwa-manifest">` elements. The duplicate stylesheet and manifest link were removed as the smallest fix needed for a single, deterministic active manifest.
- Unrelated untracked user files were present before this task and were not edited or staged.

## Delivered tests

- Added a cross-platform Node preparation step that runs `:composeApp:generatePwaFiles` before E2E execution.
- The fixture server serves the generated WordPress and Spring manifests, not copied JSON fixtures.
- Browser assertions validate the currently selected WordPress/Spring manifest, including `id`, `display: standalone`, start URL, scope, and 192/512 PNG icons.
- Browser assertions cover a granted permission with a synthetic service worker/subscription and a denied permission rejection. Both flows complete without a page crash or external push request.

## Verification

| Command | Exit | Result |
|---|---:|---|
| `./gradlew.bat :composeApp:generatePwaFiles --no-daemon` | 0 | Generated WordPress and Spring PWA manifests. |
| `$env:E2E_BROWSER_CHANNEL = 'chrome'; npm run test:e2e:fake` | 0 | 14 fixture tests passed on desktop/mobile. |
| `$env:E2E_BROWSER_CHANNEL = 'chrome'; npm run test:e2e` | 0 | 14 fixture tests passed; 28 staging/production tests skipped. |
| `git diff --check` | 0 | No whitespace error. |

The ignored JSONL reporter records fixture results as `FIXTURE_PASS_NOT_PRODUCTION`; no entry is production evidence.

## Manual QA

Not required for this fixture-only automated task. Real notification-permission UI, service-worker registration, VAPID subscription, offline state, and manifest installation must be checked later against a deployed PWA with synthetic test data.

## Remaining risk and rollback

- The tested subscription is synthetic and no push is sent; no real push provider, VAPID key, account, or notification delivery has been validated.
- No staging/production endpoint, SKU, or build fingerprint was supplied, so those tests intentionally remain skipped.
- Roll back by reverting this task's commit. No schema, customer data, or production state changed.
