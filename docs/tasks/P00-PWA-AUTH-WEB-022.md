# P00-PWA-AUTH-WEB-022 — browser session lifecycle

- Status: TODO; Size: M; Owner: AI; Risk: HIGH
- Depends on: P00-PWA-AUTH-WP-021.
- Align PWA credential sending, in-memory access token, refresh, startup restoration and logout with the reviewed web-session contract; preserve mobile and Spring behavior. Add automated positive and negative tests. If larger than M, split before implementation.
- UI/network changes require `AWAITING_MANUAL_QA` and evidence. Do not claim P00-T05 passes without owner confirmation.

The independently scoped bearer/redirect hardening child `P00-PWA-AUTH-WEB-022A` has automated evidence but awaits manual integration and compatibility QA. It does not implement session lifecycle or complete this parent.

`WEB-022B` implements only 401-triggered cookie refresh, and corrects the browser-incompatible URL-security regex; it awaits integrated manual QA. [Evidence](../evidence/P00-PWA-AUTH-WEB-022B/verification.md). Startup restore, cross-tab coordination, logout and WP guard integration remain open.
