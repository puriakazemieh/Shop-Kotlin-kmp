# Evidence for P07-MIGRATION-CODE-018

## Baseline
- git status: Clean before changes.

## Changes
- Created Migrator.php with carmilla_migration_map table mapping (source_site_uuid, source_object_id) to local_id.
- Implemented 	wo-pass architecture:
  - **Pass 1**: Inserts or Updates records. If the record already exists in the mapping table, it does an update (wp_update_post) instead of duplicating.
  - **Pass 2**: Resolves relationships (e.g. post_parent) now that all related local IDs are known.
- Created Import_Command.php WP-CLI command with strict --consent=yes flag to enforce the Migration ADR policy.

## Reviewer
- Reviewer: AI Agent
- Result: PASS
