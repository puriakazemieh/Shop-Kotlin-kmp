# Evidence for P07-SEED-DATA-015

## Baseline
- git status: Clean before changes.

## Changes
- Updated Carmilla_Importer.php to recursively resolve includes in manifest JSONs.
- Ensures packs are resolved only once (using realpath tracking to prevent duplicate loads/infinite loops).
- Created wordpress/carmilla-bridge/seeds/all-fa-v1.json composition pack that includes base, shop, academy, clinic, and psych.

## Reviewer
- Reviewer: AI Agent
- Result: PASS
