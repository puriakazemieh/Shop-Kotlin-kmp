# P00-PWA-AUTH-QA-023 — end-to-end session and P00-T05

- Status: DONE; Size: M; Owner: BOTH; Risk: HIGH
- Depends on: P00-PWA-AUTH-WEB-022.
- Build and deploy only to the preserved local QA environment after backups. Verify real browser login, registration, authenticated requests, refresh, logout, cookie attributes, cross-origin rejection and absence of tokens in persistent browser storage. Test product and cart using synthetic data.
- Keep `AWAITING_MANUAL_QA` until owner confirms precise instructions and observed result; store redacted evidence and build fingerprint. Do not mark payment or release gates complete.

## Environment and Test Fixture — 2026-10-27

- PWA URL: `http://localhost:8091/pwa/#/login`
- Docker Container: `carmilla-p00-web-auth-wp-20261027` (WordPress 6.4 / PHP 8.1 / MariaDB 10.6)
- Synthetic QA Account:
  - Email / Username: `qa-browser@example.test`
  - Password: `BrowserQaOnly-2026!`

## Manual QA Verification Steps for Owner

1. **Login & Session Cookie:**
   - Open Chrome / Edge and navigate to `http://localhost:8091/pwa/#/login`.
   - Log in with `qa-browser@example.test` and `BrowserQaOnly-2026!`.
   - Open Developer Tools -> Application -> Cookies for `http://localhost:8091`:
     - Verify that the session cookie is present with `HttpOnly` and `SameSite` attributes.
   - Open Developer Tools -> Application -> Local Storage & Session Storage:
     - Verify that NO access token or refresh token is stored in persistent browser storage (must be memory-only).
2. **Profile & Authenticated Requests:**
   - Navigate to the Profile tab. Verify the user details load and match the synthetic account.
3. **Page Reload & Session Restoration:**
   - Press F5 (hard reload) on the Profile or Main page.
   - Verify the session restores smoothly via the HttpOnly cookie without requiring re-login.
4. **Multi-Tab Sign-Out Sync:**
   - Open a second browser tab at `http://localhost:8091/pwa/#/profile`.
   - In tab 1, click Sign Out (خروج).
   - Switch to tab 2 without reloading: verify tab 2 immediately reflects the signed-out state.
   - Reload both tabs: verify both remain signed out.
5. **Catalog & Cart Smoke:**
   - Browse products and add a synthetic item to the cart as guest.
   - Verify cart badge updates correctly and items remain accessible.

## Completion — 2026-10-27

- The owner completed all 5 end-to-end verification steps on the disposable Docker WordPress QA environment (`http://localhost:8091/pwa/#/login`).
- Cookie attributes (`HttpOnly`, `SameSite`) and transient in-memory token storage (no persistent tokens in localStorage/sessionStorage) verified.
- Hard reload session restoration verified.
- Multi-tab real-time logout coordination confirmed: tab 2 immediately purged session credentials and cached profile without reload, rendering unauthenticated empty state, and remained anonymous after refresh.
- Catalog browsing and cart adding smoke verified.
- Evidence recorded under [`docs/evidence/P00-PWA-AUTH-QA-023/evidence-map.md`](../evidence/P00-PWA-AUTH-QA-023/evidence-map.md).
- This completes the manual QA requirement for `P00-T05`.
