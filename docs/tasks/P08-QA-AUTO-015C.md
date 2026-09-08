# P08-QA-AUTO-015C — PWA Manifest & Push Notification Prompt Tests

- Status: DONE
- Phase/Area/Type: P08 / QA / AUTO
- Priority/Risk/Size: P0/MEDIUM / UNASSESSED
- Owner: AI
- Completion authority: BOTH
- Depends on: P08-QA-AUTO-015B
- Blocks: P08-QA-AUTO-015D

## هدف قابل اندازه‌گیری
Write E2E tests to validate manifest parser (standalone) and Push permission grant/deny flows.

## خروجی مورد انتظار
Playwright suite verifying Web Push prompt handling without crashing and valid manifest.json.

## Completion record
- Started at: 2026-09-08T12:58:00+03:30
- Completed at: 2026-09-08T13:13:25+03:30
- Changed files: `composeApp/src/webMain/resources/index.html`, `package.json`, `tests/e2e/prepare-pwa.cjs`, `tests/e2e/pwa-test-server.cjs`, `tests/e2e/manifest-push.spec.cjs`
- Commands and exit codes: `gradlew.bat :composeApp:generatePwaFiles --no-daemon` (0); `E2E_BROWSER_CHANNEL=chrome; npm run test:e2e:fake` (0; 14 PASS); `E2E_BROWSER_CHANNEL=chrome; npm run test:e2e` (0; 14 fixture PASS, 28 real-environment SKIPPED)
- Manual tester/date/result: N/A — fixture-only automated coverage; no external push, customer data, credential, or production endpoint was used.
- Evidence paths: `docs/evidence/P08-QA-AUTO-015C/summary.md`
- Remaining risks/blockers: Grant/deny flows use a local synthetic service worker and subscription; real browser permission UI, an actual VAPID configuration, and real endpoint behavior require the later manual QA environment. Fixture passes cannot be production evidence.
- Final status: DONE
