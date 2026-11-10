# P02-SEC-CODE-010C3C4C — fail-closed wallet mutation

Date: 2026-11-10. The previous `deduct_funds` changed user meta before an unchecked direct DB ledger insert. `add_funds` used the same non-atomic pattern. Both mutation methods now return `false` without reading or writing financial state; read-only `get_balance` remains. The direct-query logger was removed. This is a temporary fail-closed state until a separately reviewed atomic balance/ledger and idempotent order implementation exists; no wallet credit or debit is supported by this service.

## Impact and scope

GitNexus class impact for `WalletService` and the wallet gateway returned `UNKNOWN`/no resolved callers; bare method-name lookups cross-linked unrelated Kotlin symbols, so those results were not used as clearance. Text search across the WordPress products found the only production `deduct_funds` caller in `WC_Gateway_Carmilla_Wallet::process_payment`; no production `add_funds` caller. The gateway remains unregistered by `WooCompat` from the earlier mitigation and its direct call checks `false` before payment completion. No other gateway code changed. RTK's non-atomic financial stop-ship rule motivated disabling both credit and debit, not only the flagged debit.

## Verification

- PHP 8.1 lint passed for the changed service and both focused fixtures. `smoke-wallet-service-disabled.php` passed: credit/debit returned `false`, balance remained 250, zero meta writes or ledger inserts, and a direct gateway call produced an error without payment completion, order note, stock reduction or cart emptying. `smoke-wallet-gateway-disabled.php` passed with the other gateway preserved.
- Built the delivery-shaped Bridge ZIP with `wordpress/build-bridge-zip.sh`, SHA-256 `8168832867640530deef22b17c30e54f8ba0ae666c24a900b014659c337a9f54`; installed it in disposable WordPress 6.4.3/PHP 8.1/MariaDB 10.6. The guarded temporary must-use route was reached through a real authenticated `wp-admin/admin-post.php` HTTP request with a synthetic administrator and nonce. Its credit/debit calls both returned `false`, the actual WordPress user-meta balance stayed 250, and the synthetic user/session were removed. Output: `PASS installed WordPress HTTP wallet mutations denied`.
- Plugin Check 2.1.0 on that installed ZIP: 311 findings before, 310 after. The exact `packages/carmilla-core/inc/Payment/WalletService.php:40:9 WordPress.DB.DirectDatabaseQuery.DirectQuery` finding disappeared. Complete after-report: [plugin-check-after.txt.gz](plugin-check-after.txt.gz), uncompressed SHA-256 `3c45375b51d1c525be3accd509ddc5f6d11e7c37fc27c5f6974cf2465c2fecbb`.
- Exact baseline still rejects only three `render_admin_page` read-only GET nonce warnings, assigned to C3C4D.

The HTTP fixture exercises the installed service boundary, **not** a real WooCommerce checkout. `P02-SEC-CODE-010C3B2E1` remains `AWAITING_MANUAL_QA` for that distinct scenario. No paid-order or financial transaction acceptance is inferred from this card.
