# P08-PWA-CODE-006 — PWA public cache policy

Date: 2026-10-12

## Baseline finding

The generated service workers cached every successful same-origin `GET` that
was not on a short sensitive blacklist. The existing cache test manually wrote
private data to Cache Storage and contained a placeholder logout assertion.
`npm run test:e2e:fake` initially failed because Playwright's bundled Chromium
was absent; the installed system Chrome was then used as the real browser.

## Change

- Both generated service workers now use an allowlist: app shell plus explicit
  public catalog/content paths only.
- Requests containing known private paths, an `Authorization` header, or token
  query parameters are network-only. Writes are also network-only and receive
  a safe JSON `503` when the network is unavailable.
- The PWA fixture server serves the generated service worker and can cut its
  network connection. The Playwright checks therefore exercise the real worker
  rather than manually inserting private Cache Storage entries.

## Verification

| Command | Result |
| --- | --- |
| `./gradlew.bat :composeApp:compileKotlinJvm` | pass |
| `./gradlew.bat :composeApp:generatePwaFiles` | pass |
| `E2E_BROWSER_CHANNEL=chrome npx playwright test tests/e2e/tenant-cache.spec.cjs --config=playwright.config.cjs --project=fake-desktop --project=fake-mobile` | pass — 4 tests, real system Chrome |
| `E2E_BROWSER_CHANNEL=chrome npm run test:e2e:fake` | pass — 28 fixture tests, real system Chrome |
| `./gradlew.bat :composeApp:compileKotlinJs` | pass |
| `./gradlew.bat :composeApp:jsBrowserDistribution` | pass — production artifact contains `pwa-bootstrap.js`, `sw-wp.js` and `sw-spring.js`; its legacy `sw.js` is network-only |

The cache tests fetch public catalog/content and all protected route families,
then inspect actual Cache Storage. They also cut the fixture server connection
and assert that an offline payment write returns `503` rather than being cached
or queued.

The production distribution was inspected after the build. Its `index.html`
loads the bootstrap rather than registering a cache-all worker directly; the
bootstrap selects the WordPress or Spring worker. The artifact CSP has no
`unsafe-eval` or wildcard source.

## Evidence classification and remaining work

This is browser evidence for a local fixture, not production or staging
evidence. No real backend credential, customer data, token, or payment was
used. CSP/header hardening, VAPID/consent backend and staging validation remain
separate P08 scope; the overall PWA critical gate therefore remains unchecked.

## Manual QA handoff

Use a fresh browser profile against the PWA deployment after the updated build
is served. In DevTools → Application → Cache Storage, visit public catalog and
content once, then visit authenticated profile/order/payment pages. Success is:

1. shell and public catalog/content entries may appear;
2. auth, profile, address, favorites, support, clinic, psych, order, payment,
   message and health entries never appear;
3. while offline, a payment write fails with the visible offline error and no
   duplicate is submitted after reconnecting.

Record browser, URL, build fingerprint, tester, date and result before marking
the task `DONE`.

## Rollback

Deploy the previous PWA service worker revision and reload the application.
The new worker retains the existing cache-name revision; activation removes only
older Carmilla worker caches as before.

## WordPress runtime revalidation — 2026-10-13

- Production artifact rebuilt with `:composeApp:jsBrowserDistribution` and served at the persistent QA URL `http://localhost/carmilla-pwa-qa/`.
- A real Chrome/Playwright run registered `http://localhost/carmilla-pwa-qa/sw-wp.js`, rendered one Compose canvas, and received HTTP 200 from the WordPress manifest and public catalog endpoints.
- `E2E_BROWSER_CHANNEL=chrome npx playwright test tests/e2e/tenant-cache.spec.cjs --config=playwright.config.cjs --project=fake-desktop --project=fake-mobile` passed again: 4/4.
