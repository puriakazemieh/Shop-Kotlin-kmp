# P08-QA-AUTO-015A — Playwright Infrastructure, Matrix (2 Profiles, 3 Hosts) & Reporting

- Status: TODO
- Phase/Area/Type: P08 / QA / AUTO
- Priority/Risk/Size: P0/MEDIUM / UNASSESSED
- Owner: AI
- Completion authority: BOTH
- Depends on: P08-PWA-PERF-014
- Blocks: P08-QA-AUTO-015B

## هدف قابل اندازه‌گیری
Initialize Playwright, setup matrix testing (Desktop/Mobile, Fake/Staging/Prod), and custom reporter logging SKU/Fingerprint.

## خروجی مورد انتظار
npm init, playwright installation, strict separation of fake fixtures from real backend (fake must not report as production PASS).
