# P00-PWA-AUTH-WP-021C04B03D — composite proof and feasibility gate

- Status: TODO; Size: M; Owner: AI; Risk: HIGH
- Depends on: `P00-PWA-AUTH-WP-021C04B03C`.
- Review and run a combined synthetic WP/SQL concurrency, rollback, fault and bypass matrix across all covered source operations. Verify owner binding, same-owner alias reference lifecycle and no unproven leap from one child test to global coverage. Document unresolved normalization, engine, permissions, deployment, compatibility, recovery and external-side-effect limitations.
- Address the `021C04B03A` finding that a rejected database INSERT can return user ID 0 and fire `user_register` with ID 0: prove safe behavior for supported writers and their side effects, or keep the parent blocked. Do not infer a safe WP registration outcome from the source-row constraint alone.
- Reconcile parent B03 only if its stated acceptance conditions and a safe next step are genuinely met; otherwise mark the specific blocker and do not start migration. No live route or site changes. Evidence: `docs/evidence/P00-PWA-AUTH-WP-021C04B03D/`.
