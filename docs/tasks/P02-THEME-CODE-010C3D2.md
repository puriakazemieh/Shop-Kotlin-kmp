# P02-THEME-CODE-010C3D2 — Implement wp_link_pages pagination and searchform integration in theme templates

- Status: TODO
- Phase/Area/Type: P02 / THEME / CODE
- Priority/Risk/Size: P2 / LOW / S
- Owner: AI
- Depends on: P02-SEC-CODE-010C3D1
- Blocks: P02-THEME-CODE-010C3D3, P02-CI-CODE-010C3E

## Scope and acceptance

In `wordpress/carmilla-theme/single.php`, `wordpress/carmilla-theme/page.php`, and `single-cb_*.php` post templates, invoke standard `wp_link_pages()` after `the_content()` to paginate multi-part posts. In `wordpress/carmilla-theme/footer.php:45`, replace the hardcoded `<form role="search"...>` element with `get_search_form()` to leverage the existing `searchform.php` template. Verify PHP syntax (`php -l`), template rendering, and check that Theme Check findings for `wp_link_pages` and `role="search"` in footer are eliminated. Allowed: `wordpress/carmilla-theme/*.php`, `docs/**`. Evidence: `docs/evidence/P02-THEME-CODE-010C3D2/`.

## Completion record

- Final status: TODO.
