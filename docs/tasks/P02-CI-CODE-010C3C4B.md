# P02-CI-CODE-010C3C4B — Normalize shipped order-controller line endings

- Status: TODO
- Phase/Area/Type: P02 / CI / CODE
- Priority/Risk/Size: P2 / MEDIUM / XS
- Owner: AI
- Depends on: P02-SEC-CODE-010C3C4A
- Blocks: P02-CI-CODE-010C3C4D

## Scope and acceptance

Investigate the `Internal.LineEndings.Mixed` finding at `includes/class-cb-order-controller.php:1:1` in the actual Bridge ZIP. Normalize only the necessary source/packaging line endings without changing PHP behavior or overwriting user changes. Build the ZIP on the supported host path, rerun Plugin Check 2.1.0, and prove this exact finding disappears while the PHP file still lints. Record bytes/checks and evidence in `docs/evidence/P02-CI-CODE-010C3C4B/`.

## Completion record

- Final status: TODO.
