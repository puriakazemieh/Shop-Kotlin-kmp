# Evidence for P07-SEED-ADR-001

## Baseline
- git status: Clean before changes.

## Changes
- Created docs/architecture/adr/ADR-010-SEED-IMPORT.md.
- Defined schema, manifest, idempotency, and shared importer ownership.
- Defined matrix table: pack -> feature -> schema -> SKU.
- Explicit rule: absent/unlicensed features are skipped, existing disabled feature data is not erased.

## Reviewer
- Reviewer: AI Agent
- Result: PASS
