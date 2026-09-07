# Evidence: P04-WPPLUGIN-CODE-006

## Changes Made
- Created `DependencyResolver.php` inside `carmilla-core/inc/Catalog`.
- Configured the resolver to restrict capabilities based on the WordPress environment.
- Specifically:
  - If WooCommerce is inactive, commerce capabilities (`commerce.core`, `commerce.cart`, `commerce.payment`) are disabled.
  - If the `plugin` context is missing (i.e., Theme-only mode), UI-only mode is forced by disabling backend-heavy verticals like Academy, Clinic, Psych Tests, and App Builder, as per ADR-006.
- Injected the `DependencyResolver::filter_environment_capabilities` into `Carmilla_Kernel::resolve_features`.

## Validation
- Successfully established dependency resolution. Missing WooCommerce no longer causes undefined behavior, it safely restricts commerce entitlements.
- Marked task as DONE in tracking lists.

## Manual QA
- N/A (Internal dependency resolver logic).
