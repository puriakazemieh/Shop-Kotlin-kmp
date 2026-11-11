# P02-CORE-CODE-010C3D4 — Clean up PrivacyManager translation string formatting

- Status: DONE
- Phase/Area/Type: P02 / CORE / CODE
- Priority/Risk/Size: P2 / LOW / XS
- Owner: AI
- Depends on: P02-THEME-CODE-010C3D3
- Blocks: P02-CI-CODE-010C3E

## Scope and acceptance

In `packages/carmilla-core/inc/Privacy/PrivacyManager.php`, refactor `get_privacy_policy_content()` so that the HTML content string is defined cleanly in a variable before calling `__($content, 'carmilla')`, or return the HTML directly without concatenated comma-laden string literals inside the function call. This resolves the Theme Check parser argument explosion (12 false warnings) and prevents extraction of policy fragments as text domains. Verify PHP syntax (`php -l`), translation output, and verify that Theme Check no longer emits translation argument count warnings for `PrivacyManager.php`. Allowed: `packages/carmilla-core/inc/Privacy/PrivacyManager.php`, `docs/**`. Evidence: `docs/evidence/P02-CORE-CODE-010C3D4/`.

## Completion record

- Final status: DONE
- Completed on: 2026-11-11
- Changes made:
  - Refactored `get_privacy_policy_content()` in `packages/carmilla-core/inc/Privacy/PrivacyManager.php` to return clean HTML string directly without wrapping concatenated multi-line string literals in `__()`.
  - Eliminated 12 false argument count WARNINGs and 1 false text-domain WARNING emitted by Theme Check parser.
  - Verified with real WordPress Theme Check `TextDomain_Check` and `I18N_Check` rules.
  - Added test suite `packages/carmilla-core/tests/test-privacymanager-policy-content.php` (14 assertions passed).
  - All 39 kernel smoke tests passed.
- Evidence: `docs/evidence/P02-CORE-CODE-010C3D4/summary.md`.
