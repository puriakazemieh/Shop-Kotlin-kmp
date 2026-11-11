# P02-THEME-CODE-010C3D2 — Implement wp_link_pages Pagination and Searchform Integration

- Date: 2026-11-11
- Task ID: `P02-THEME-CODE-010C3D2`
- Scope: `carmilla-theme/single.php`, `carmilla-theme/page.php`, `carmilla-theme/single-cb_*.php`, `carmilla-theme/footer.php`, `carmilla-theme/assets/css/base.css`
- Outcome: **PASS** (Theme Check findings #2 and #24 resolved; standard pagination and filterable searchform implemented)

---

## 1. Summary of Changes

To resolve Theme Check REQUIRED finding #2 (`wp_link_pages`) and WARNING finding #24 (`role="search"` in footer):
1. **Multi-Part Post Pagination (`wp_link_pages`)**:
   - Added standard `wp_link_pages()` calls after `the_content()` across all single and page content templates:
     - `wordpress/carmilla-theme/single.php`
     - `wordpress/carmilla-theme/page.php`
     - `wordpress/carmilla-theme/single-cb_course.php`
     - `wordpress/carmilla-theme/single-cb_psychtest.php`
     - `wordpress/carmilla-theme/single-cb_therapist.php`
   - Configured accessible navigation markup with `aria-label`, localized `"برگه‌ها:"` title, and wrapped page numbers.
2. **Filterable Searchform Integration**:
   - In `wordpress/carmilla-theme/footer.php`, replaced the hardcoded `<form role="search"...>` element with standard `get_search_form()`.
   - This routes search form rendering through `wordpress/carmilla-theme/searchform.php` and allows WordPress plugins and hooks to filter the form via the `get_search_form` filter hook.
3. **Design System Pagination Styling**:
   - Added responsive, RTL-compatible styling for `.page-links` in `wordpress/carmilla-theme/assets/css/base.css` utilizing Carmilla design tokens (`var(--sp-xs)`, `var(--r-sm)`, `var(--surface)`, `var(--accent)`, `var(--line)`).

---

## 2. Verification Commands and Results

### A. PHP Syntax Lint
```bash
docker run --rm -v "${PWD}/wordpress:/app" -w /app php:8.1-cli sh -c "
php -l carmilla-theme/single.php && \
php -l carmilla-theme/page.php && \
php -l carmilla-theme/single-cb_course.php && \
php -l carmilla-theme/single-cb_psychtest.php && \
php -l carmilla-theme/single-cb_therapist.php && \
php -l carmilla-theme/footer.php
"
```
- **Exit Code**: 0
- **Output**:
  ```
  No syntax errors detected in carmilla-theme/single.php
  No syntax errors detected in carmilla-theme/page.php
  No syntax errors detected in carmilla-theme/single-cb_course.php
  No syntax errors detected in carmilla-theme/single-cb_psychtest.php
  No syntax errors detected in carmilla-theme/single-cb_therapist.php
  No syntax errors detected in carmilla-theme/footer.php
  ```

### B. Dedicated Template & Execution Test Suite
Executed in `php:8.1-cli` container:
```bash
docker run --rm -v "${PWD}/wordpress:/app" -w /app php:8.1-cli php carmilla-theme/tests/test-template-pagination-searchform.php
```
- **Exit Code**: 0
- **Assertions Passed**: 13 / 13
  - `PASS: single.php invokes wp_link_pages`
  - `PASS: page.php invokes wp_link_pages`
  - `PASS: single-cb_course.php invokes wp_link_pages`
  - `PASS: single-cb_psychtest.php invokes wp_link_pages`
  - `PASS: single-cb_therapist.php invokes wp_link_pages`
  - `PASS: footer.php does not hardcode role="search" form`
  - `PASS: footer.php invokes get_search_form()`
  - `PASS: base.css provides .page-links styling`
  - `PASS: single.php executed wp_link_pages in loop`
  - `PASS: wp_link_pages received proper before arg`
  - `PASS: page.php executed wp_link_pages in loop`
  - `PASS: footer.php executed get_search_form()`
  - `PASS: footer.php output contains rendered searchform`

### C. Existing Theme Smoke Tests
```bash
docker run --rm -v "${PWD}/wordpress:/app" -w /app php:8.1-cli php carmilla-theme/tests/smoke-manifest-theme.php
```
- **Exit Code**: 0
- **Output**: `ALL PASSED`

### D. Package Delivery Verification
```bash
docker run --rm -v "${PWD}/wordpress:/app" -w /app composer:2 sh -c "bash build-theme-zip.sh /tmp/theme.zip && unzip -p /tmp/theme.zip carmilla-theme/single.php | grep -i 'wp_link_pages' && unzip -p /tmp/theme.zip carmilla-theme/footer.php | grep -i 'get_search_form' && unzip -l /tmp/theme.zip | grep -i 'tests/' || echo 'NO_TESTS_IN_ZIP'"
```
- **Exit Code**: 0
- Confirmed `wp_link_pages` and `get_search_form` present in shipped theme ZIP; test files excluded.

---

## 3. Conclusion & Next Steps

Task `P02-THEME-CODE-010C3D2` is complete with automated evidence.
Next ready task in queue: `P02-THEME-CODE-010C3D3` (Add theme screenshot, readme.txt, and complete style.css metadata).
