# P00-PWA-AUTH-CODE-020 — isolated in-memory browser token settings

- Status: DONE
- Owner: AI; Size: S; Risk: HIGH
- Depends on: P00-QA-MANUAL-018 (historical baseline; current revalidation remains open).
- Scope: make the browser token store process-memory-only, wire it to TokenManager, preserve existing Android/iOS behavior. Do not expose refresh tokens in persistent browser storage.
- Characterization: `createTokenSettings()` currently delegates to browser localStorage and is not wired into TokenManager. Android requires context-injected settings; do not invoke its context-free factory.
- Allowed: `core/data/**`, this card, queue and corresponding evidence. Preserve unrelated changes and local WordPress instances.
- Verification: regression test for in-memory token isolation and namespace cleanup; compile JS/JVM and any affected Android code. Record commands/results under `docs/evidence/P00-PWA-AUTH-CODE-020/`.
- Manual QA: bundled with P00-PWA-AUTH-QA-023 after the backend and client session contracts exist; this isolated storage task cannot demonstrate a working login. Do not deploy or claim PWA login is fixed.
- Completion: DONE only with automated evidence; P00-T05 stays open. Make only the next eligible card READY.
- Rollback: revert only this card's source changes; never remove browser data, WordPress sites or databases.

## Execution record — 2026-10-17

- In-memory JS storage and dependency-injection binding implemented; Android and iOS retain the injected settings instance.
- JS/JVM compilation and a Chrome Headless test passed. The first browser-test attempt did not reach the test due to a missing generated `skiko.mjs`; it was rerun after copying missing build-only runtime assets, with no source or user-file overwrite.
- The first Android compile could not resolve `androidx.annotation:annotation-experimental:1.4.0`; the initial HEAD probe returned HTTP 404, but a later GET returned HTTP 200. Using the owner's downloaded AAR, verified against Google's SHA-256 metadata, and a second Google-downloaded SHA-1-verified artifact in a temporary local Maven repository, `:core:data:compileAndroidMain` completed successfully. No dependency version or permanent repository configuration was changed. Ordinary Gradle resolution without the temporary init script has not been shown to work.
- No QA PWA bundle or WordPress installation was changed. Browser storage from previous versions was not deleted or audited.
- Evidence: `docs/evidence/P00-PWA-AUTH-CODE-020/verification.md`.
- Pending manual QA once the integration is ready: open the QA PWA, log in with a synthetic test account, then inspect DevTools Application > Local Storage and Session Storage for the site. Success: no access or refresh token is persisted; login, refresh and logout must also work. Do not perform this test yet because the WordPress web endpoints remain unavailable.
- Final status: DONE for this source-level storage task after the specified automated checks; this does not mean the PWA login or P00-T05 has passed. The integration task retains human manual QA.
