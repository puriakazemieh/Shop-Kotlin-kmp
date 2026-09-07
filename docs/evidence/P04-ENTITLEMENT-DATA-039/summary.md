# Evidence: P04-ENTITLEMENT-DATA-039

## Changes Made
- Created `carmilla-core` package directory structure at `wordpress/packages/carmilla-core/inc/Catalog`.
- Developed `EntitlementSchema.php`: Defines the canonical catalog of capabilities, features, and SKUs (Commerce, Academy, Clinic, Psych, App Builder) and their dependencies.
- Developed `EntitlementClaims.php`: Implements fixtures for fully unlocked environments and theme-only mode, and includes the resolver logic (`resolve_effective_features`) to calculate active features based on active SKUs and overrides.

## Validation
- Successfully modeled the product kinds and artifact inventory.
- Features correctly map to their parent SKUs.
- Marked task as DONE in tracking lists.

## Manual QA
- N/A (Data/schema definition task).
