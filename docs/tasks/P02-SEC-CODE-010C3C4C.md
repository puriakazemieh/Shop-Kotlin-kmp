# P02-SEC-CODE-010C3C4C — Fail closed unsafe wallet debit path

- Status: DONE
- Phase/Area/Type: P02 / SECURITY / CODE
- Priority/Risk/Size: P0 / CRITICAL / M
- Owner: AI
- Depends on: P02-CI-CODE-010C3C4B (the wallet gateway code mitigation from P02-SEC-CODE-010C3B2E1 is already present; its checkout QA remains separately open)
- Blocks: P02-CI-CODE-010C3C4D

## Scope and acceptance

Trace every caller of `WalletService::deduct_funds` and the current direct-query finding. Until an atomic balance/ledger and idempotent order implementation has its own reviewed task, make this unsafe debit path fail closed with no balance, ledger or paid-order mutation; preserve other payment gateways. Verify direct calls and representative WordPress HTTP behavior with synthetic accounts, and rerun the delivery-shaped Plugin Check scan to show the exact wallet direct-query finding gone. Do not claim wallet checkout acceptance from a direct-call test; keep `P02-SEC-CODE-010C3B2E1` manual QA open until the full WooCommerce scenario is actually observed. Evidence: `docs/evidence/P02-SEC-CODE-010C3C4C/`. Allowed: bundled wallet service/gateway boundary, focused tests and evidence.

## Completion record

- Final status: DONE for the bounded fail-closed service change. Direct financial/gateway smoke, installed-WordPress authenticated HTTP mutation denial, PHP lint and delivery-ZIP Plugin Check passed; the exact DB finding disappeared. Evidence: `docs/evidence/P02-SEC-CODE-010C3C4C/summary.md` and full report. Real WooCommerce checkout QA remains open in P02-SEC-CODE-010C3B2E1.
