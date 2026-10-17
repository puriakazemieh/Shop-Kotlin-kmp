# P00-PWA-AUTH-WP-021C — WordPress browser-session routes

- Status: READY; Size: M; Owner: BOTH; Risk: HIGH
- Depends on: `P00-PWA-AUTH-WP-021B`.
- Integrate the reviewed secure session infrastructure with web login, registration, refresh and logout; address OTP where required by the reviewed contract. Enforce the origin/CSRF and secure-deployment rules, preserve mobile endpoints, and add positive and negative automated tests. If larger than M, stop and propose smaller cards.
- Do not deploy to production or modify the preserved local QA site without an appropriate backup. Record redacted evidence under `docs/evidence/P00-PWA-AUTH-WP-021C/`.
- Network changes require explicit manual QA: provide the owner the environment, exact steps and success criteria and keep `AWAITING_MANUAL_QA` until confirmed. Backend-only QA must not be presented as end-to-end PWA verification; that remains `P00-PWA-AUTH-QA-023` after the client lifecycle card.
- After evidence and required confirmation, reconcile the parent `P00-PWA-AUTH-WP-021` with the actual child outcomes before moving to `P00-PWA-AUTH-WEB-022`.
