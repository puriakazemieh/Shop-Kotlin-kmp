# P00-PWA-AUTH-WP-021 — scope review

Date: 2026-10-17

- Read the task card and execution queue; inspected the bridge authentication controller and JWT implementation.
- A local POST with an empty JSON object to `http://localhost/carmilla-p07-qa/wp-json/carmilla/v1/api/auth/web/login` returned HTTP **404**. This is a route-existence check, not a login or security test; no credentials or tokens were sent.
- The controller registers mobile login, register and refresh routes. Its existing authentication response includes a refresh token in JSON. The JWT class has user-wide timestamp-based revocation; that is not evidence of atomic per-session rotation, replay protection or logout.
- The card combines security design, new session storage/lifecycle, public authentication routes and security verification. Its scope exceeds M under the project execution rules. Proposed smaller tasks are recorded on the card; none has been implemented or validated in this review.
- No WordPress installation, database, PWA build or source code was changed. Existing unrelated working-tree changes were left untouched.
- Manual QA and P00-T05 remain pending; do not interpret this diagnostic check as successful login or a passed security gate.
