# P02-CI-CODE-010C3D — Triage Theme Check required findings and artifact scope

- Status: DONE
- Phase/Area/Type: P02 / CI / REVIEW
- Priority/Risk/Size: P1 / HIGH / M
- Owner: AI
- Depends on: P02-CI-CODE-010C3A1 (pinned Theme Check report; independent of the Plugin Check parent gate)
- Blocks: P02-SEC-CODE-010C3D1, P02-CI-CODE-010C3E

## Scope and acceptance

Using the pinned full Theme Check JSON and the actual Theme ZIP from each supported build path, inspect every REQUIRED/WARNING/RECOMMENDED finding. Separate active independent commercial-Theme behavior from any WordPress.org UI-only channel requirement using the active contract; do not infer permission to ship a security issue. Specifically trace copied `carmilla-core/tests`, possible missing core in the package workflow ZIP, `base64_encode`/`base64_decode` in message settings, `wp_link_pages`, `readme.txt`, and any forbidden API/secret/output finding. Record exact finding identity, severity, source-backed disposition, owner task and phase dependency. Create S/M package/product fix cards for genuine issues; do not edit product or package workflows in this review. Allowed: read-only Theme/core/package source, `tools/code-quality/**`, `docs/**`. Evidence: `docs/evidence/P02-CI-CODE-010C3D/` with complete report, artifact inventory, channel decision and follow-up cards.

## Completion record

- Final status: DONE on 2026-11-11. All 58 findings (13 REQUIRED, 16 WARNING, 22 RECOMMENDED, 7 INFO) in the pinned Theme Check report (`e5c56a2c...`) triaged and reconciled against the independent commercial product contract (`docs/INDEPENDENT_PRODUCTS_SPEC_FA.md` Sections 1, 6, 12).
  - Traces confirmed: 0 test files in shipped Theme ZIP, exact package workflow parity with bundled `carmilla-core` verified via C3A1, `base64_decode` in `MessageSettings.php` confirmed as legitimate AES-256-CBC secret storage with S-size refactor card created, `wp_link_pages` confirmed missing from single/page post templates, `readme.txt` and `screenshot.png` confirmed missing, forbidden APIs (`register_post_type`, `register_taxonomy`, `add_shortcode`) approved as commercial debt consolidating in `P04-WORDPRESS-CODE-051`, and `PrivacyManager.php` string concatenation parser explosion isolated.
  - Four ordered follow-up fix cards created: `P02-SEC-CODE-010C3D1` (S), `P02-THEME-CODE-010C3D2` (S), `P02-THEME-CODE-010C3D3` (S), `P02-CORE-CODE-010C3D4` (XS).
  - Full evidence, structured dispositions ledger, and SHA-256 fingerprints recorded in `docs/evidence/P02-CI-CODE-010C3D/`.
