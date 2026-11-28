# Evidence Summary: P04-WPTHEME-CODE-025

- **Task ID:** `P04-WPTHEME-CODE-025`
- **Title:** رفع Template Hierarchy برگه و سازگاری Elementor Page Layouts
- **Date:** 2026-11-28
- **Base Commit:** `7323f1e2`
- **Status:** `AWAITING_MANUAL_QA` (Template trace, `elementor-blank.php` fix, and 46/46 HTTP matrix with Bridge OFF/ON passed; Elementor Free plugin is not installed in test container so Elementor Canvas/Full Width manual check remains)

## Summary of Changes

1. **Template Hierarchy & Root Cause Trace**:
   - Confirmed `wordpress/carmilla-theme/page.php` exists and executes `the_content()`, preventing fallback to `index.php` blog cards.
   - Confirmed `elementor-full-width.php` renders `get_header()` + `the_content()` + `get_footer()` with full-bleed content width.
   - Updated `wordpress/carmilla-theme/page-templates/elementor-blank.php` to dynamically use `is_rtl() ? 'rtl' : 'ltr'` and wrap `the_content()` in `<main id="content" tabindex="-1">`, omitting theme header/footer while loading `wp_head()` and `wp_footer()`.
2. **Real WordPress Layout Matrix Test (`wordpress/carmilla-theme/tests/test-page-elementor-layouts-025.php`)**:
   - Verified `Default Page`, `Carmilla Full Width`, `Carmilla Blank`, and `Regular Post` via live HTTP rendering both with `carmilla-bridge` **OFF** (Theme-Only) and **ON** (Co-Install), confirming identical `the_content()` rendering and zero duplicate headers.

## Verification Commands & Results

- **Environment:** Docker `test-env-wordpress-test-1` (WordPress 7.1.3, MariaDB 10.6.28, Apache)
- **Results:**
  - `php -l elementor-blank.php`: **0 errors (exit code 0)**
  - `test-page-elementor-layouts-025.php`: **46 / 46 passed (exit code 0)**
  - `verify-all-phase04-real-wp.php`: **78 / 78 passed (exit code 0)**

## Remaining Manual QA

- Install/activate **Elementor Free** in staging and open a synthetic Elementor page under **Elementor Canvas** and **Elementor Full Width** (with Bridge OFF and ON) across 360px/tablet/desktop viewports.
