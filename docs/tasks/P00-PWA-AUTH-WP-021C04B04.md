# P00-PWA-AUTH-WP-021C04B04 — synthetic migration and owner review

- Status: BLOCKED; Size: M; Owner: BOTH; Risk: HIGH
- Depends on: `P00-PWA-AUTH-WP-021C04B03`.
- Implement and test the B02-approved collision-aware reversible migration only on a separate synthetic WordPress/database. Preserve all existing accounts and both persistent local WordPress sites. If this exceeds M, split before proceeding. Never apply it to an existing site without separate explicit scope and an appropriate backup/rollback process.
- Define exact owner manual QA steps with a persistent isolated fixture, expected results and redacted evidence. Status must be `AWAITING_MANUAL_QA` until explicit owner confirmation; an automated test or the agent's inspection cannot substitute for that confirmation.

Independent child `021C04B04A` supplies only an inactive, bounded exact-ASCII collision audit in a synthetic fixture. [Evidence](../evidence/P00-PWA-AUTH-WP-021C04B04A/verification.md). It is not normalization, migration, collision repair or proof that the dependency has passed. This parent remains TODO.
