# Evidence Summary: P02-THEME-CODE-010C3D3

- **Task ID**: `P02-THEME-CODE-010C3D3`
- **Phase / Area / Type**: `P02 / THEME / ASSETS`
- **Date**: 2026-11-11
- **Status**: DONE

---

## 1. Scope & Objective

Complete the Theme Check remediation items concerning theme assets, documentation, and metadata:
1. Provide a standard `screenshot.png` (1200x900) in `wordpress/carmilla-theme/` displaying the theme visual identity for the wp-admin Themes screen (resolves Theme Check Finding #21).
2. Add `wordpress/carmilla-theme/readme.txt` with description, GPL license, tags, requirements, and changelog (resolves Theme Check Finding #10).
3. Complete `wordpress/carmilla-theme/style.css` header fields:
   - Add `License URI: https://www.gnu.org/licenses/gpl-2.0.html` (resolves Finding #27).
   - Add `Tested up to: 6.4` (resolves Finding #28).
   - Add `Author URI: https://github.com/puriakazemieh/Shop-Kotlin-kmp` (resolves Finding #31).
   - Add GPL copyright declaration comment (resolves Finding #9).
   - Normalize tags to standard WordPress tag vocabulary, removing non-standard/deprecated `rtl` and `woocommerce` tags (resolves Findings #29 and #30).

---

## 2. Changes Made

1. **`wordpress/carmilla-theme/screenshot.png`**:
   - Generated valid 1200x900 PNG image with theme branding palette, typography, and visual vertical indicators.
   - Verified via `getimagesize()`: width 1200px, height 900px, MIME `image/png`.

2. **`wordpress/carmilla-theme/readme.txt`**:
   - Created WordPress-standard `readme.txt` with headers:
     - `Contributors: carmilla`
     - `Tags: e-commerce, custom-colors, theme-options, translation-ready`
     - `Requires at least: 6.3`
     - `Tested up to: 6.4`
     - `Requires PHP: 7.4`
     - `License: GPLv2 or later`
     - `License URI: https://www.gnu.org/licenses/gpl-2.0.html`
   - Included Description, Installation, Frequently Asked Questions, Copyright, and Changelog sections.

3. **`wordpress/carmilla-theme/style.css`**:
   - Updated header comment with `Author URI`, `Tested up to: 6.4`, `License URI`, and copyright declaration.
   - Normalized `Tags: e-commerce, custom-colors, theme-options, translation-ready`.

4. **`wordpress/carmilla-theme/tests/test-theme-metadata-assets.php`**:
   - Added automated verification script executing 33 assertions covering screenshot dimensions, mime type, readme.txt headers and sections, style.css fields, copyright notices, and tag validation.

---

## 3. Verification & Results

### 3.1 Automated Metadata & Asset Test Suite
```bash
docker run --rm -v "d:\Android\AndroidStudioProjects\kmp-shop\wordpress:/app" -w /app php:8.1-cli php carmilla-theme/tests/test-theme-metadata-assets.php
```
**Output**:
```
=== Test Theme Metadata & Assets (P02-THEME-CODE-010C3D3) ===

1. Screenshot validation:
  [PASS] screenshot.png exists in theme root
  [PASS] screenshot.png is a valid image readable by getimagesize()
  [PASS] screenshot width is 1200px (actual: 1200)
  [PASS] screenshot height is 900px (actual: 900)
  [PASS] screenshot MIME type is image/png (actual: image/png)

2. Readme.txt validation:
  [PASS] readme.txt exists in theme root
  [PASS] readme.txt has theme header === Carmilla ===
  [PASS] readme.txt defines GPL license
  [PASS] readme.txt defines License URI
  [PASS] readme.txt defines Tested up to: 6.4
  [PASS] readme.txt defines Requires at least: 6.3
  [PASS] readme.txt contains Description section
  [PASS] readme.txt contains Installation section
  [PASS] readme.txt contains Changelog section
  [PASS] readme.txt contains Copyright section

3. style.css metadata validation:
  [PASS] style.css exists in theme root
  [PASS] style.css header contains 'Theme Name: Carmilla'
  [PASS] style.css header contains 'Theme URI: https://github.com/puriakazemieh/Shop-Kotlin-kmp'
  [PASS] style.css header contains 'Author: Carmilla'
  [PASS] style.css header contains 'Author URI: https://github.com/puriakazemieh/Shop-Kotlin-kmp'
  [PASS] style.css header contains 'Description: پوسته‌ی فارسی/RTL کارمیلا'
  [PASS] style.css header contains 'Version: 0.8.0'
  [PASS] style.css header contains 'Requires at least: 6.3'
  [PASS] style.css header contains 'Tested up to: 6.4'
  [PASS] style.css header contains 'Requires PHP: 7.4'
  [PASS] style.css header contains 'License: GPL-2.0-or-later'
  [PASS] style.css header contains 'License URI: https://www.gnu.org/licenses/gpl-2.0.html'
  [PASS] style.css header contains 'Text Domain: carmilla'
  [PASS] style.css header contains 'Tags: e-commerce, custom-colors, theme-options, translation-ready'
  [PASS] style.css has copyright declaration
  [PASS] style.css has GPL declaration
  [PASS] Tags list does NOT contain deprecated non-standard 'rtl' tag
  [PASS] Tags list does NOT contain non-standard 'woocommerce' tag

Summary: 33 passed, 0 failed.
All metadata and asset tests passed successfully!
```
Exit code: `0`.

### 3.2 Packaging Verification
```bash
docker run --rm -v "d:\Android\AndroidStudioProjects\kmp-shop\wordpress:/app" -w /app composer:2 sh -c "bash build-theme-zip.sh /tmp/carmilla-theme.zip && unzip -l /tmp/carmilla-theme.zip | grep -E 'screenshot\.png|readme\.txt|style\.css'"
```
**Output**:
```
Built /tmp/carmilla-theme.zip
     1036  2026-11-11 11:41   carmilla-theme/style.css
    18142  2026-11-11 11:41   carmilla-theme/screenshot.png
     1593  2026-11-11 11:41   carmilla-theme/readme.txt
```
Exit code: `0`.

### 3.3 Regression Tests
```bash
docker run --rm -v "d:\Android\AndroidStudioProjects\kmp-shop\wordpress:/app" -w /app php:8.1-cli sh -c "php carmilla-theme/tests/smoke-manifest-theme.php && php carmilla-theme/tests/test-template-pagination-searchform.php"
```
**Output**:
All 5 manifest smoke tests and 13 pagination/searchform tests passed.
Exit code: `0`.
