# P02-SEC-CODE-010C3B2E1 — Disable unsafe wallet checkout

- Status: AWAITING_MANUAL_QA
- Phase/Area/Type: P02 / SECURITY / CODE
- Priority/Risk/Size: P0 / CRITICAL / S
- Owner: AI
- Depends on: P02-CI-CODE-010C3B2E
- Blocks: P02-CI-CODE-010C3B2F

## Scope and acceptance

`WalletService::deduct_funds` updates user meta before an unchecked wallet-log insert and uses a non-atomic read/modify/write. `WC_Gateway_Carmilla_Wallet` is registered by `WooCompat` and defaults to enabled. Prevent the wallet gateway from being available at checkout until a separately reviewed atomic balance/ledger and idempotent order implementation exists. Preserve other WooCommerce gateways. Verify registration/checkout behavior in a representative WordPress/WooCommerce environment and record evidence that wallet cannot process payment while other gateways remain. Allowed: bundled core Woo compatibility/wallet gateway code, focused tests, docs/evidence. Do not claim the wallet ledger fixed by merely hiding it.

## Completion record

- Final status: AWAITING_MANUAL_QA after code change on 2026-11-10. Local PHP and real-WordPress filter checks passed, including a pre-change negative control. WooCommerce checkout could not be installed in the disposable environment because WordPress.org downloads were unreachable. Exact checkout steps and success criteria are in `docs/evidence/P02-SEC-CODE-010C3B2E1/summary.md` and the agent's Persian report. Do not claim wallet transactions repaired or phase gate passed.
