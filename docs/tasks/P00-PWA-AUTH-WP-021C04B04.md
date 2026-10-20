# P00-PWA-AUTH-WP-021C04B04 — synthetic migration and owner review

- Status: DONE; Size: M; Owner: BOTH; Risk: HIGH
- Depends on: `P00-PWA-AUTH-WP-021C04B03`.
- Implement and test the B02-approved collision-aware reversible migration only on a separate synthetic WordPress/database. Preserve all existing accounts and both persistent local WordPress sites. If this exceeds M, split before proceeding. Never apply it to an existing site without separate explicit scope and an appropriate backup/rollback process.
- Define exact owner manual QA steps with a persistent isolated fixture, expected results and redacted evidence. Status must be `AWAITING_MANUAL_QA` until explicit owner confirmation; an automated test or the agent's inspection cannot substitute for that confirmation.

Independent child `021C04B04A` supplies only an inactive, bounded exact-ASCII collision audit in a synthetic fixture. [Evidence](../evidence/P00-PWA-AUTH-WP-021C04B04A/verification.md). It is not normalization, migration, collision repair or proof that the dependency has passed. This parent remains TODO.

## Automated implementation — 2026-10-20

Implemented an inactive migration primitive with no public route or automatic
execution. It first accepts only a bounded exact-audit-clean fixture, marks only
`cb_mobile` owners, journals every run, and rolls back only markers still carrying
that run identifier. It never rewrites login, email or mobile values, and a
collision produces no marker write. The disposable WordPress 7.1/PHP 8.3 fixture
passed all six checks; see [verification](../evidence/P00-PWA-AUTH-WP-021C04B04/verification.md).

## Manual QA — required before guarded browser registration

Use only the disposable Docker WordPress fixture, never a retained site.

1. In the disposable fixture, create two new test customers with different ASCII
   mobile values, then run the documented synthetic migration smoke test once.
2. Confirm both accounts still exist and receive only the `cb_identity_managed=1`
   marker; login, email and mobile values must be unchanged.
3. Run its rollback check and confirm both migration markers disappear while the
   two accounts and their original mobile values remain.
4. Create a second test account with a mobile already assigned to the first, run
   the test again, and confirm it reports a collision without adding either
   migration marker.

Success means clean data applies and reverses only attributable markers, while
colliding data is untouched. Report only pass/fail; do not share credentials,
mobile values or database exports.

## Owner QA — 2026-10-20

The owner reran the disposable fixture and confirmed all six checks passed with
zero failures. The reversible synthetic migration gate is complete.
