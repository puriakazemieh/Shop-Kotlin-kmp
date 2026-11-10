# P02-SEC-CODE-010C3B3D1 — Guard shared-core direct entry

- Status: READY
- Phase/Area/Type: P02 / Security / CODE
- Priority/Risk/Size: P1 / HIGH / S
- Owner: AI
- Depends on: P02-CI-CODE-010C3B3D
- Blocks: P02-CI-CODE-010C3C

## Scope and acceptance

Add a direct-request guard to shipped `packages/carmilla-core/Carmilla_Kernel.php` without breaking Theme-only, Bridge-only or combined loading. Verify standalone direct HTTP request cannot execute/emit content or trigger PHP errors, and normal package activation still loads the kernel from each host. Use disposable WordPress/PHP and synthetic data. Allowed: shared-core entry, focused tests, evidence. Evidence: `docs/evidence/P02-SEC-CODE-010C3B3D1/`.

## Completion record

- Final status: TODO; fix owner from C3B3D triage.
