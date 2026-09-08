# Manual QA Evidence Handoff: P09-QA-MANUAL-003

- Task ID: P09-QA-MANUAL-003
- Date: 2026-09-06
- Title: Clean install, upgrade, and rollback on WP/PHP/Woo matrix

## Test Instructions for User (Manual QA)
### Where to Look
- WordPress Admin Panel (`/wp-admin/plugins.php` or `/wp-admin/themes.php`)
- WooCommerce Settings -> Advanced -> Features (HPOS - High-Performance Order Storage status)
- PHP / WordPress Logs (`wp-content/debug.log`)

### How to Test
1. **Clean Installation Matrix:**
   - Install `carmilla-bridge.zip` (Plugin) on clean WP 6.4/6.5/6.6 with PHP 8.1/8.2/8.3.
   - Install `carmilla-theme.zip` (Theme) on clean WP.
   - Verify active status without PHP errors or warnings.
2. **Upgrade Test:**
   - Install previous release version, populate sample products/orders.
   - Upgrade to current candidate version. Verify DB migrations run automatically and cleanly.
3. **Rollback Test:**
   - Rollback plugin/theme version. Verify site stability and zero data corruption.
4. **HPOS & WooCommerce Blocks Compatibility:**
   - Enable HPOS (High-Performance Order Storage) in WooCommerce.
   - Test Cart & Checkout Gutenberg Blocks.

### Success Criteria
- Zero fatal PHP errors during install, upgrade, or rollback.
- Orders and settings preserved after upgrade/rollback.
- Full HPOS and WooCommerce Checkout Blocks compatibility verified.

## Status
- Final Status: AWAITING_MANUAL_QA
