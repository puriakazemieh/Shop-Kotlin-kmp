# Evidence for P01-WPPLUGIN-ARCH-029

## Actions
- Identified split-brain logic in the WordPress theme (\carmilla-theme/inc/*\).
- Deleted redundant business logic files from the theme (booking.php, clinic-extra.php, course.php, rest.php, psychtest.php, etc.) because these APIs and logic are already handled by \carmilla-bridge\ plugin and the Spring Boot backend.
- Moved \meta-boxes.php\ (which defines the Admin UI and Data fields for CPTs) from the theme to the plugin (\carmilla-bridge/includes/\), ensuring the plugin fully owns the data model.
- Removed all \equire\ statements for the deleted files from \carmilla-theme/functions.php\.

## Verification
- Verified via regex that no \update_post_meta\, \update_user_meta\, or \\\ calls remain in the theme (except for \demo-import.php\).
- Ran \	ests/smoke.php\ on the plugin to ensure no syntax/load errors.
