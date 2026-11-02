# P02-SEC-CODE-010C2B2A2 — Guard managed deletion dependency race

- Status: BLOCKED
- Phase/Area/Type: P02 / Security / CODE
- Priority/Risk/Size: P1 / HIGH / M
- Owner: AI
- Depends on: P02-SEC-CODE-010C2B2A1
- Blocks: P02-CI-CODE-010C2C

## Scope and acceptance

The dependency check for posts/comments precedes `START TRANSACTION`, allowing a concurrent write between check and user deletion. Establish a bounded, provable fail-closed boundary for standard single-site WordPress or stop and document an unsupported topology. Demonstrate with two independent synthetic DB connections that no dependent content can be orphaned by this managed deletion path; preserve native deletion protection and ordinary successful deletion. If an M-sized solution cannot prove this, decompose before coding. Allowed: `wordpress/carmilla-bridge/includes/class-cb-managed-user-deletion.php`, focused tests, `docs/**`. Migration/deletion manual QA and owner confirmation required. Evidence: `docs/evidence/P02-SEC-CODE-010C2B2A2/`.

## Completion record

- Reviewed at: 2026-11-01
- Evidence: `docs/evidence/P02-SEC-CODE-010C2B2A2/blocker.md`
- Blocker: Standard WordPress post/comment tables do not enforce a user foreign key. A lock held only by the deletion path cannot constrain independent writers after release. The allowed class/test/docs scope cannot establish the required lasting invariant.
- Required decision: authorize a separate database-integrity/migration design with compatibility and rollback requirements, or explicitly narrow the guarantee to cooperative Carmilla writers and accept unsupported external writes.
- Owner decision: on 2026-11-02, authorized the database-integrity design path. Ordered child cards `P02-SEC-DISC-010C2B2A2A`, `P02-SEC-CODE-010C2B2A2B`, and `P02-SEC-QA-010C2B2A2C` were queued; this parent remains BLOCKED until their evidence and manual confirmation satisfy its acceptance.
- Final status: BLOCKED
