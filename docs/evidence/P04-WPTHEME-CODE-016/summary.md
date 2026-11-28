# Evidence Summary: P04-WPTHEME-CODE-016

- **Task ID:** `P04-WPTHEME-CODE-016`
- **Title:** template hierarchy، RTL/LTR، light/dark و responsive تثبیت
- **Date:** 2026-11-28
- **Base Commit:** `7429dd2f`
- **Status:** `AWAITING_MANUAL_QA` (Code & automated HTTP/template/token checks complete; awaiting human visual inspection at `360/390/600/840/1024/1440` viewports)

## Summary of Changes

1. **Dynamic RTL/LTR Directionality (`wordpress/carmilla-theme/header.php`)**:
   - Replaced hardcoded `dir="rtl"` on `.dc-root` with `dir="<?php echo esc_attr( is_rtl() ? 'rtl' : 'ltr' ); ?>"` so WordPress locale directionality applies dynamically.
2. **Token-Backed RTL/LTR & Light/Dark Styling (`wordpress/carmilla-theme/rtl.css`)**:
   - Replaced hardcoded `#121212` / `#ffffff` / `#000000` overrides with canonical CSS tokens (`var(--bg)`, `var(--ink)`), added explicit `[dir="ltr"]` / `[dir="rtl"]` logical alignment, and added `padding-inline` responsive container rules.
3. **Real WordPress Template Hierarchy & Responsive Test Suite (`wordpress/carmilla-theme/tests/test-theme-hierarchy-responsive-016.php`)**:
   - Verified all 18 canonical WP, CPT, and WooCommerce templates exist.
   - Verified dynamic `is_rtl()` directionality, pre-paint `cb_theme` light/dark script, token overrides (`:root[data-theme="dark"]`, `prefers-color-scheme: dark`), horizontal overflow protection (`overflow-x: hidden`), and live HTTP rendering for Home (`200`), Search (`200`), and 404 (`404`) with zero PHP Fatal errors.

## Verification Commands & Results

- **Environment:** Docker `test-env-wordpress-test-1` (WordPress 7.1.3, MariaDB 10.6.28, Apache)
- **Results:**
  - `php -l header.php`: **0 errors (exit code 0)**
  - `test-theme-metadata-assets.php`: **33 / 33 passed (exit code 0)**
  - `test-template-pagination-searchform.php`: **13 / 13 passed (exit code 0)**
  - `test-theme-hierarchy-responsive-016.php`: **38 / 38 passed (exit code 0)**
  - `verify-all-phase04-real-wp.php`: **78 / 78 passed (exit code 0)**

## Remaining Manual Visual QA

- Capture/inspect screenshots at `360px`, `390px`, `600px`, `840px`, `1024px`, and `1440px` in Light & Dark modes and RTL/LTR.
