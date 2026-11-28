# Evidence Summary: P04-WPPLUGIN-CODE-018

- **Task ID:** `P04-WPPLUGIN-CODE-018`
- **Title:** ترجمه و escaping مرزهای مشترک و میزبان‌های مستقل
- **Date:** 2026-11-28
- **Base Commit:** `21657c30`
- **Status:** `DONE`

## Summary of Changes

1. **POT Translation Templates for Both Hosts & Shared Core**:
   - Added `wordpress/carmilla-theme/languages/carmilla.pot` (`carmilla` textdomain).
   - Added `wordpress/carmilla-bridge/languages/carmilla-bridge.pot` (`carmilla-bridge` textdomain).
   - Added `wordpress/packages/carmilla-core/languages/carmilla-core.pot` (shared core `carmilla` textdomain; vertical strings remain scoped to each vertical's domain card).
2. **Plugin Textdomain Loading Without Co-Install Duplication (`wordpress/carmilla-bridge/carmilla-bridge.php`)**:
   - Declared `Domain Path: /languages` in plugin header.
   - Hooked `carmilla_bridge_load_textdomains()` on `init` to load `'carmilla-bridge'` and load `'carmilla'` from bundled `packages/carmilla-core/languages` ONLY when not already loaded by the theme (`!is_textdomain_loaded('carmilla')`).
3. **Real WordPress i18n & Escaping Test Suite (`wordpress/packages/carmilla-core/tests/test-i18n-escaping-018.php`)**:
   - Verified `.pot` presence in all 3 locations, plugin headers, `fa_IR` / `en_US` locale switching across `Co-Install`, `Theme-Only`, and `Plugin-Only` (TT4), and HTML/XSS output escaping (`esc_html`, `esc_attr`) on `AdminSettingsPage`.

## Verification Commands & Results

- **Environment:** Docker `test-env-wordpress-test-1` (WordPress 7.1.3, MariaDB 10.6.28, Apache)
- **Results:**
  - `php -l carmilla-bridge.php`: **0 errors (exit code 0)**
  - `test-i18n-escaping-018.php`: **15 / 15 passed (exit code 0)**
  - `verify-all-phase04-real-wp.php`: **78 / 78 passed (exit code 0)**
