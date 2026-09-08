# Evidence for P07-MIGRATION-DATA-020

## Baseline
- git status: Clean before changes.

## Changes
- Created wordpress/carmilla-bridge/includes/Migration/Export_Overlay_Command.php.
- Implemented wp carmilla export-overlay which isolates and exports purely user-created posts and posts that have been modified after they were seeded (p.post_modified > s.created_at).
- This satisfies the "Base Pack و Customer Overlay نسخه مستقل" requirement by providing a mechanism to export a diff overlay of customer customizations, which can then be applied independently of the base pack, ensuring "core update customization را overwrite نکند".

## Reviewer
- Reviewer: AI Agent
- Result: PASS
