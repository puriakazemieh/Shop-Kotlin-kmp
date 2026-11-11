# P02-CORE-CODE-010C3D4 — Clean up PrivacyManager translation string formatting

- Status: READY
- Phase/Area/Type: P02 / CORE / CODE
- Priority/Risk/Size: P2 / LOW / XS
- Owner: AI
- Depends on: P02-THEME-CODE-010C3D3
- Blocks: P02-CI-CODE-010C3E

## Scope and acceptance

In `packages/carmilla-core/inc/Privacy/PrivacyManager.php`, refactor `get_privacy_policy_content()` so that the HTML content string is defined cleanly in a variable before calling `__($content, 'carmilla')`, or return the HTML directly without concatenated comma-laden string literals inside the function call. This resolves the Theme Check parser argument explosion (12 false warnings) and prevents extraction of policy fragments as text domains. Verify PHP syntax (`php -l`), translation output, and verify that Theme Check no longer emits translation argument count warnings for `PrivacyManager.php`. Allowed: `packages/carmilla-core/inc/Privacy/PrivacyManager.php`, `docs/**`. Evidence: `docs/evidence/P02-CORE-CODE-010C3D4/`.

## Completion record

- Final status: TODO.
