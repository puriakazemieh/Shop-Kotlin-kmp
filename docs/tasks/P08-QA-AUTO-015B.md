# P08-QA-AUTO-015B — Security Headers & Performance Budget Automated Tests

- Status: TODO
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
