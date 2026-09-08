# Evidence for P07-MIGRATION-SEC-022

## Baseline
- git status: Clean before changes.

## Changes
- Created Migration_Config.php holding centralized denylists.
- Explicitly blocked CPTs: shop_order, shop_order_refund, shop_coupon, cb_health_record, cb_psych_result, cb_payment, cb_user_secret to ensure no live customer transactions or health data leak into migration/seed bundles.
- Explicitly blocked Meta Keys: billing, shipping, payment methods, transaction IDs, passwords, and tokens.
- Updated Export_Command and Export_Overlay_Command to respect the denylist and strictly filter database queries.

## Reviewer
- Reviewer: AI Agent
- Result: PASS
