# P02-THEME-CODE-010C3D3 — Add theme screenshot, readme.txt, and complete style.css metadata

- Status: DONE
- Phase/Area/Type: P02 / THEME / ASSETS
- Priority/Risk/Size: P2 / LOW / S
- Owner: AI
- Depends on: P02-THEME-CODE-010C3D2
- Blocks: P02-CORE-CODE-010C3D4, P02-CI-CODE-010C3E

## Scope and acceptance

Provide a standard `screenshot.png` (880x660 or 1200x900) in `wordpress/carmilla-theme/` displaying theme identity for wp-admin Themes screen. Add `wordpress/carmilla-theme/readme.txt` with description, GPL license, and changelog. Complete `style.css` header fields: add `License URI: https://www.gnu.org/licenses/gpl-2.0.html`, `Tested up to: 6.4`, `Author URI: https://github.com/puriakazemieh/Shop-Kotlin-kmp`, a GPL copyright notice comment, and normalize tags to standard WordPress tag vocabulary. Verify via Theme Check and packaging scripts. Allowed: `wordpress/carmilla-theme/style.css`, `screenshot.png`, `readme.txt`, `docs/**`. Evidence: `docs/evidence/P02-THEME-CODE-010C3D3/`.

## Completion record

- Final status: DONE
- Completed on: 2026-11-11
- Changes made:
  - Added valid 1200x900 `wordpress/carmilla-theme/screenshot.png` with Carmilla branding.
  - Added `wordpress/carmilla-theme/readme.txt` with GPL license, requirements, description, FAQ, and changelog.
  - Completed `wordpress/carmilla-theme/style.css` header fields with `Author URI: https://github.com/puriakazemieh/Shop-Kotlin-kmp`, `Tested up to: 6.4`, `License URI: https://www.gnu.org/licenses/gpl-2.0.html`, GPL copyright notice comment, and normalized tags.
  - Added automated test suite `wordpress/carmilla-theme/tests/test-theme-metadata-assets.php` (33 assertions passed).
  - Verified package build script `build-theme-zip.sh` cleanly includes screenshot, readme, and style.css.
- Evidence: `docs/evidence/P02-THEME-CODE-010C3D3/summary.md`.
