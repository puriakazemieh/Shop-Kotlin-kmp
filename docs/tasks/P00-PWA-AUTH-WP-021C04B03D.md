# P00-PWA-AUTH-WP-021C04B03D — composite proof and feasibility gate

- Status: BLOCKED; Size: M; Owner: AI; Risk: HIGH
- Depends on: `P00-PWA-AUTH-WP-021C04B03C`.
- Review and run a combined synthetic WP/SQL concurrency, rollback, fault and bypass matrix across all covered source operations. Verify owner binding, same-owner alias reference lifecycle and no unproven leap from one child test to global coverage. Document unresolved normalization, engine, permissions, deployment, compatibility, recovery and external-side-effect limitations.
- Address the `021C04B03A` finding that a rejected database INSERT can return user ID 0 and fire `user_register` with ID 0, and the `021C04B03B` finding that a rejected update can return the existing ID and fire `profile_update`: prove safe behavior for supported writers and their side effects, or keep the parent blocked. Do not infer a safe WP operation from the source-row constraint alone.
- Reconcile parent B03 only if its stated acceptance conditions and a safe next step are genuinely met; otherwise mark the specific blocker and do not start migration. No live route or site changes. Evidence: `docs/evidence/P00-PWA-AUTH-WP-021C04B03D/`.

The owner authorized independent Phase 00 work before its dependency is ready. This is an early feasibility reassessment only: it cannot waive the unresolved dependency or enable a migration.

## Reassessment — 2026-10-18

The shared-schema WP writer experiment reproduced misleading insert/update results and callbacks on three WP/PHP versions; the independent concurrency/fault matrix also reproduced the partial-delete blocker. The full combined acceptance and a supported-writer/recovery solution are not established. [Evidence and reproduction](../evidence/P00-PWA-AUTH-WP-021C04B03D/composite-reassessment.md). Parent remains blocked; no live route or real migration was enabled.

## Supported-writer preparation — 2026-10-20

The owner-authorized limited integration now uses the normal-WordPress writer
in the existing password and first-OTP registration paths. It holds only short
per-identifier locks, checks the committed source rows after callbacks, and
returns recovery-required rather than a success result on a post-callback
conflict. It has no migration or trigger; see the [preparation
evidence](../evidence/P00-PWA-AUTH-WP-021C04B03D/supported-writer-preparation.md)
and the required [manual API QA](P00-PWA-AUTH-WP-021C04B03D01.md). This does
not unblock the parent.
