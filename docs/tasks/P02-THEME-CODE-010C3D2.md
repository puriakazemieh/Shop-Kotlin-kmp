# P02-THEME-CODE-010C3D2 — Implement wp_link_pages pagination and searchform integration in theme templates

- Status: DONE
- Phase/Area/Type: P02 / THEME / CODE
- Priority/Risk/Size: P2 / LOW / S
- Owner: AI
- Depends on: P02-SEC-CODE-010C3D1
- Blocks: P02-THEME-CODE-010C3D3, P02-CI-CODE-010C3E

## Scope and acceptance

In `wordpress/carmilla-theme/single.php`, `wordpress/carmilla-theme/page.php`, and `single-cb_*.php` post templates, invoke standard `wp_link_pages()` after `the_content()` to paginate multi-part posts. In `wordpress/carmilla-theme/footer.php:45`, replace the hardcoded `<form role="search"...>` element with `get_search_form()` to leverage the existing `searchform.php` template. Verify PHP syntax (`php -l`), template rendering, and check that Theme Check findings for `wp_link_pages` and `role="search"` in footer are eliminated. Allowed: `wordpress/carmilla-theme/*.php`, `docs/**`. Evidence: `docs/evidence/P02-THEME-CODE-010C3D2/`.

## Completion record

- Final status: DONE on 2026-11-11. `wp_link_pages()` implemented across all single and page content templates (`single.php`, `page.php`, `single-cb_course.php`, `single-cb_psychtest.php`, `single-cb_therapist.php`) with accessible markup and localized labels. `footer.php` search form integrated with standard `get_search_form()`. Added `.page-links` styling in `base.css`. All 6 modified PHP files passed `php -l`. Passed 13-point automated template execution and static test suite (`test-template-pagination-searchform.php`), theme customizer smoke test (`smoke-manifest-theme.php`), and delivery ZIP inspection. Evidence recorded in `docs/evidence/P02-THEME-CODE-010C3D2/summary.md`.
