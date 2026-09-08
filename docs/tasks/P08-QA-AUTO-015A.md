# P08-QA-AUTO-015A — Playwright Infrastructure, Matrix (2 Profiles, 3 Hosts) & Reporting

- Status: DONE
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

## Completion record
- Started at: 2026-09-08T12:20:44+03:30
- Completed at: 2026-09-08T12:40:02+03:30
- Changed files: `package.json`, `package-lock.json`, `playwright.config.cjs`, `tests/e2e/`, `.gitignore`
- Commands and exit codes: `npm install --ignore-scripts` (0); `npm run test:e2e:list` (0; 6 projects); `npm run test:e2e` (0; 2 fixture PASS, 4 real-environment SKIPPED); system Chrome launch smoke (0)
- Manual tester/date/result: N/A — infrastructure-only change; no UI, network, or migration behavior was changed.
- Evidence paths: `docs/evidence/P08-QA-AUTO-015A/summary.md`
- Remaining risks/blockers: No real staging/production endpoint, SKU, or build fingerprint was provided, so those projects remain SKIPPED and cannot yield `PRODUCTION_PASS`. The local bundled Chromium download did not complete; the installed system Chrome launch smoke passed. `npm audit` reports two high-severity findings in the CI-aligned Playwright 1.48.0 dependency; remediation is out of scope for this task.
- Final status: DONE
