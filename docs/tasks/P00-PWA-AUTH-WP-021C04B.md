# P00-PWA-AUTH-WP-021C04B — isolated guarded registration

- Status: READY; Size: M; Owner: AI; Risk: HIGH
- Depends on: `P00-PWA-AUTH-WP-021C04A`.
- Implement/test guarded registration against real WordPress users and the common token handoff. Respect server registration policy and WP validation/filter outcomes; prevent duplicate/ambiguous identity and unsafe account association. Preserve mobile API behavior. Account creation followed by session failure must report the partial outcome accurately without deleting the user.
- Test invalid input, existing identities, concurrent/ambiguous cases, WP failure, guard rejection, account creation and storage failure/redaction using synthetic WordPress and MariaDB only. If identity uniqueness cannot be safely established with current schema, fail closed and document the precise limitation; do not declare the feature production-ready.
- No hooks, routes, cookie emission, migration or edits to preserved sites. Record redacted evidence in `docs/evidence/P00-PWA-AUTH-WP-021C04B/`; owner network QA remains `021C07`.
