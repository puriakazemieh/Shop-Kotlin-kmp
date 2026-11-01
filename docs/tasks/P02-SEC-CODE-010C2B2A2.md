# P02-SEC-CODE-010C2B2A2 — Guard managed deletion dependency race

- Status: TODO
- Phase/Area/Type: P02 / Security / CODE
- Priority/Risk/Size: P1 / HIGH / M
- Owner: AI
- Depends on: P02-SEC-CODE-010C2B2A1
- Blocks: P02-CI-CODE-010C2C

## Scope and acceptance

The dependency check for posts/comments precedes `START TRANSACTION`, allowing a concurrent write between check and user deletion. Establish a bounded, provable fail-closed boundary for standard single-site WordPress or stop and document an unsupported topology. Demonstrate with two independent synthetic DB connections that no dependent content can be orphaned by this managed deletion path; preserve native deletion protection and ordinary successful deletion. If an M-sized solution cannot prove this, decompose before coding. Allowed: `wordpress/carmilla-bridge/includes/class-cb-managed-user-deletion.php`, focused tests, `docs/**`. Migration/deletion manual QA and owner confirmation required. Evidence: `docs/evidence/P02-SEC-CODE-010C2B2A2/`.

## Completion record

- Final status: TODO
