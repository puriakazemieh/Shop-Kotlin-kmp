# P00-PWA-AUTH-WEB-022 — browser session lifecycle

- Status: BLOCKED; Size: L; Owner: AI; Risk: HIGH
- Depends on: P00-PWA-AUTH-WP-021.
- Align PWA credential sending, in-memory access token, refresh, startup restoration and logout with the reviewed web-session contract; preserve mobile and Spring behavior. Add automated positive and negative tests. This was split before completion into `022F` (guarded endpoint selection), `022G` (in-memory browser credentials and startup restoration) and `022H` (server logout and multi-tab lifecycle).
- UI/network changes require `AWAITING_MANUAL_QA` and evidence. Do not claim P00-T05 passes without owner confirmation.

The independently scoped bearer/redirect hardening child `P00-PWA-AUTH-WEB-022A` has automated evidence but awaits manual integration and compatibility QA. It does not implement session lifecycle or complete this parent.

`WEB-022B` implements only 401-triggered cookie refresh, and corrects the browser-incompatible URL-security regex; it awaits integrated manual QA. [Evidence](../evidence/P00-PWA-AUTH-WEB-022B/verification.md). Startup restore, cross-tab coordination, logout and WP guard integration remain open.

`WEB-022C` supplies the scoped client-side web request header, awaiting real backend and owner QA. [Evidence](../evidence/P00-PWA-AUTH-WEB-022C/verification.md). Server-side request policy and route integration are not implied.

`WEB-022D` provides an isolated web logout API request with 401 refresh suppressed; it is not wired to the sign-out repository. [Evidence](../evidence/P00-PWA-AUTH-WEB-022D/verification.md). Local/cache cleanup and refresh races remain open.

`WEB-022E` characterizes a separate Spring refresh/logout ordering defect with both a mock and transactional services on isolated PostgreSQL: logout using a previously rotated token leaves its replacement usable. [Evidence](../evidence/P00-PWA-AUTH-WEB-022E/verification.md). It is not fixed or tested as a concurrent HTTP/browser flow.

The parent remains blocked until the three focused cards below are reconciled. They are deliberately sequenced so browser manual QA happens only after the complete lifecycle is connected.
