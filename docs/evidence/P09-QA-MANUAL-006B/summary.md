# Manual QA Evidence Handoff: P09-QA-MANUAL-006B

- Task ID: P09-QA-MANUAL-006B
- Date: 2026-09-06
- Title: Runtime Toggle Testing Without Rebuild in WordPress/PWA/Client Internal

## Test Instructions for User (Manual QA)
### Where to Look
- WordPress Admin -> Carmilla Capabilities Page (`/wp-admin/admin.php?page=carmilla-manifest`)
- KMP Client Application (Android / PWA)

### How to Test
1. **Prepare Release Build:**
   - Deploy production or staging build of client app and WordPress plugin.
2. **Toggle Feature On/Off:**
   - Toggle a purchased feature (e.g. `clinic.booking` or `academy.core`) off in WordPress admin panel.
   - Open app (without reinstalling or rebuilding) and verify the feature is immediately hidden or locked.
3. **Invalid/Stale Manifest Simulation:**
   - Simulate 403 or invalid signature on manifest endpoint.
   - Verify app falls back safely without crashing or exposing unauthorized screens.
4. **Deep Link Handling on Disabled Feature:**
   - Open a deep link pointing to a disabled feature (e.g. `app://booking/123`).
   - Verify app intercepts the link and safely redirects user to home/upgrade page with clear message.
5. **Process Restart & Cache Validation:**
   - Force restart app process and verify cached feature toggles load correctly on cold start.

### Success Criteria
- Features turn on/off dynamically in client without rebuilding binary.
- Deep links to disabled features fail gracefully.

## Status
- Final Status: AWAITING_MANUAL_QA
