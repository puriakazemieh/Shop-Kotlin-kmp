# Evidence: P04-WPPLUGIN-ADR-002

## Changes Made
- Appended the **Plugin & Theme Co-install and Schema Authority** section to `docs/architecture/adr/ADR-006-INDEPENDENT-PRODUCTS-AND-ENTITLEMENTS.md` as per task requirements.
- The rules clearly define how `Carmilla_Kernel` handles co-installs to prevent double-booting.
- Established the Plugin (Bridge) as the Schema Authority for CPTs and tables.
- Defined a `Version Mismatch` strategy using Graceful Degradation to avoid Fatal Errors when versions differ.

## Validation
- Successfully reviewed and formatted the ADR document.
- Marked task as DONE in tracking lists.

## Manual QA
- N/A (Documentation task).
