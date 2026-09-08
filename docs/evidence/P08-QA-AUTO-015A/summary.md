# Evidence for P08-QA-AUTO-015A

## Baseline

- Baseline commit: `4f0b1bfe`.
- The working tree contained unrelated untracked user files before this task; they were neither edited nor staged.

## Delivered infrastructure

- Added the root Playwright test package and reproducible lockfile, pinned to the repository's existing CI version: `@playwright/test` 1.48.0.
- Added six named projects: `fake`, `staging`, and `production`, each on `desktop` and `mobile` profiles.
- Added a JSONL reporter that records host, profile, SKU, fingerprint, retry, and outcome.
- A result is `PRODUCTION_PASS` only when the project is production **and** an explicit production base URL, SKU, and build fingerprint are supplied. Fixture passes are recorded as `FIXTURE_PASS_NOT_PRODUCTION`.
- The only executable infrastructure assertion runs on fake projects. Staging and production are skipped until a real endpoint and release identity are supplied; this avoids a fixture or configuration-only pass being treated as production evidence.

## Verification

| Command | Exit | Result |
|---|---:|---|
| `npm install --ignore-scripts` | 0 | Test runner installed. |
| `npm run test:e2e:list` | 0 | Six projects discovered: 3 hosts × 2 profiles. |
| `npm run test:e2e` | 0 | 2 fake fixture tests passed; 4 staging/production tests skipped. |
| System Chrome launch smoke | 0 | Local `channel: chrome` launched and closed. |

The generated, ignored JSONL report contained two `FIXTURE_PASS_NOT_PRODUCTION` records and four `skipped` records. No real backend, SKU, fingerprint, customer data, credential, or production request was used.

## Manual QA

Not required for this infrastructure-only task. Future E2E cards require a separately declared environment, synthetic data, SKU, and build fingerprint before staging or production evidence can be collected.

## Remaining risk and rollback

- `npx playwright install chromium` downloaded the archive but did not complete local extraction in this runtime; the system Chrome smoke passed. CI/browser-provisioning verification remains necessary when a browser-facing E2E test is added.
- `npm audit` reports two high-severity findings in the pre-existing CI-aligned Playwright 1.48.0 dependency. This task did not upgrade dependencies.
- Roll back by reverting this task's commit; no database, network contract, customer data, or production state changed.
