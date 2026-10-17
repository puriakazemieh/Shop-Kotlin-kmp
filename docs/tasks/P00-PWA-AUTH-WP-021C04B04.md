# P00-PWA-AUTH-WP-021C04B04 — synthetic migration and owner review

- Status: TODO; Size: M; Owner: BOTH; Risk: HIGH
- Depends on: `P00-PWA-AUTH-WP-021C04B03`.
- Implement and test the B02-approved collision-aware reversible migration only on a separate synthetic WordPress/database. Preserve all existing accounts and both persistent local WordPress sites. If this exceeds M, split before proceeding. Never apply it to an existing site without separate explicit scope and an appropriate backup/rollback process.
- Define exact owner manual QA steps with a persistent isolated fixture, expected results and redacted evidence. Status must be `AWAITING_MANUAL_QA` until explicit owner confirmation; an automated test or the agent's inspection cannot substitute for that confirmation.
