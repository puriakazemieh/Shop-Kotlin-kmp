# P00-PWA-AUTH-QA-023 — evidence map

## End-to-end session verification (P00-T05) — 2026-10-27

- **Tested by:** Owner
- **Environment:** Disposable Docker QA fixture (`carmilla-p00-web-auth-wp-20261027` at `http://localhost:8091/pwa/#/login`)
- **Synthetic QA Account:** پس از تست حذف شد؛ شناسه در Evidence نگهداری نمی‌شود.

## Verification Observations

1. **Login & Cookie Attributes:**
   - User logged in successfully.
   - Verified session cookie has `HttpOnly` and `SameSite` flags.
   - Verified `localStorage` and `sessionStorage` contain no access token, refresh token, or sensitive credentials (memory-only in JS).
2. **Profile & Authenticated API Flow:**
   - Profile screen successfully retrieved and rendered user information via authenticated `/wp-json/carmilla/v1/api/users/me` endpoint.
3. **Session Restoration (F5 / Reload):**
   - Hard reload restored the session smoothly via the HttpOnly cookie without requiring re-authentication.
4. **Multi-Tab Sign-Out Sync:**
   - Sign-out executed in tab 1.
   - Tab 2 (holding the `/profile` route open) immediately cleared user state and displayed unauthenticated empty state ("اوپس! Profile not found"), proving in-memory credentials and cached user profile were erased in real-time across tabs.
   - Upon reload or navigating back, both tabs remained fully in guest / unauthenticated state.
5. **Catalog & Cart Smoke:**
   - Products browsed and items added to cart as guest; cart count and persistence behaved as expected.

## Outcome

All 5 manual QA verification scenarios passed. `P00-T05` manual browser session verification is confirmed.
