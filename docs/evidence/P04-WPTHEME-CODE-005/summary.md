# Evidence: P04-WPTHEME-CODE-005

## Changes Made
- Integrated the Shared Kernel (`Carmilla_Kernel`) into the theme and bridge build scripts.
- Updated `build-theme-zip.sh` and `build-bridge-zip.sh` to package the `wordpress/packages/carmilla-core` directory into their respective zip distributions, removing the dependency on external core availability.
- This ensures that if the Theme is installed by itself, or the Bridge is installed by itself, they both have access to the Versioned Shared Kernel.

## Validation
- Scripts copy `packages/carmilla-core` correctly during build and clean up afterward.
- Marked task as DONE in tracking lists.

## Manual QA
- N/A (Build scripts and connection task).
