# Evidence: P04-WPPLUGIN-CODE-004

## Changes Made
- Authored `Carmilla_Kernel.php` inside `carmilla-core` to manage duplicate booting.
- Included `carmilla-core/Carmilla_Kernel.php` conditionally inside `carmilla-bridge.php` (`class_exists('Carmilla_Kernel')`).
- Included `carmilla-core/Carmilla_Kernel.php` conditionally inside `carmilla-theme/functions.php`.
- Ensured both the theme and plugin will trigger `\Carmilla_Kernel::boot(...)` when active, passing their context (`plugin` or `theme`) and their versions.
- The first one to load executes migrations and registers entitlements without race conditions or Fatal Errors (duplicate classes).

## Validation
- Successfully integrated the versioned shared core into both products.
- Marked task as DONE in tracking lists.

## Manual QA
- N/A (Core integration task).
