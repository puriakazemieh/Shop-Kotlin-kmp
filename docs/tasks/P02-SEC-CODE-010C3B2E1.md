# P02-SEC-CODE-010C3B2E1 — Disable unsafe wallet checkout

- Status: READY
- Phase/Area/Type: P02 / SECURITY / CODE
- Priority/Risk/Size: P0 / CRITICAL / S
- Owner: AI
- Depends on: P02-CI-CODE-010C3B2E
- Blocks: P02-CI-CODE-010C3B2F

## Scope and acceptance

`WalletService::deduct_funds` updates user meta before an unchecked wallet-log insert and uses a non-atomic read/modify/write. `WC_Gateway_Carmilla_Wallet` is registered by `WooCompat` and defaults to enabled. Prevent the wallet gateway from being available at checkout until a separately reviewed atomic balance/ledger and idempotent order implementation exists. Preserve other WooCommerce gateways. Verify registration/checkout behavior in a representative WordPress/WooCommerce environment and record evidence that wallet cannot process payment while other gateways remain. Allowed: bundled core Woo compatibility/wallet gateway code, focused tests, docs/evidence. Do not claim the wallet ledger fixed by merely hiding it.

## Completion record

- Final status: READY after shipped-file DB triage exposed the default-enabled wallet checkout risk.
