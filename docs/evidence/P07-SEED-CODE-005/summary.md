# Evidence for P07-SEED-CODE-005

## Baseline
- git status: Clean before changes.

## Changes
- Updated import_chunk in Carmilla_Importer.php.
- Implemented true idempotency by checking carmilla_seed_objects table instead of relying only on WP-specific functions.
- Post meta is still stored, but query uses the central database table for efficiency and cross-entity standard tracking.
- Inserts new registry row on creation.
- Explicitly skips updates on existing entities to protect customer modifications (0 duplicate execution on second run).

## Reviewer
- Reviewer: AI Agent
- Result: PASS
