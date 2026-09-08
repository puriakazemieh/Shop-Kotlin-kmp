# P08-QA-AUTO-015B — Security Headers & Performance Budget Automated Tests

- Status: DONE
- Phase/Area/Type: P08 / QA / AUTO
- Priority/Risk/Size: P0/MEDIUM / UNASSESSED
- Owner: AI
- Completion authority: BOTH
- Depends on: P08-QA-AUTO-015A
- Blocks: P08-QA-AUTO-015C

## هدف قابل اندازه‌گیری
Write E2E tests to validate CSP/Referrer-Policy and intercept performance budget warnings (3000ms threshold).

## خروجی مورد انتظار
Playwright suite fails if security headers are missing or if FCP/LCP exceeds budget on emulated slow network.

## Completion record
- Started at: 2026-09-08T12:42:00+03:30
- Completed at: 2026-09-08T12:56:00+03:30
- Changed files: `playwright.config.cjs`, `tests/e2e/pwa-test-server.cjs`, `tests/e2e/security-performance.spec.cjs`
- Commands and exit codes: `E2E_BROWSER_CHANNEL=chrome; npm run test:e2e:fake` (0; 6 PASS); `E2E_BROWSER_CHANNEL=chrome; npm run test:e2e` (0; 6 fixture PASS, 12 real-environment SKIPPED)
- Manual tester/date/result: N/A — fixture-only E2E coverage; no shipped UI, network contract, or migration was changed.
- Evidence paths: `docs/evidence/P08-QA-AUTO-015B/summary.md`
- Remaining risks/blockers: The suite validates the policy declared by the source HTML through its local fixture server; a real staging/production deployment and its hosting-layer headers still require the separately declared endpoint, SKU, fingerprint, and later manual QA. Fixture results cannot be production evidence.
- Final status: DONE
