# Evidence for P07-MIGRATION-DATA-017

## Baseline
- git status: Clean before changes.

## Changes
- Created wordpress/carmilla-bridge/includes/Migration/Export_Command.php to register a WP-CLI command wp carmilla export-legacy.
- The export dumps data to secure NDJSON format (posts.ndjson and media.ndjson) to strictly avoid PHP serialization vulnerabilities (e.g., Object Injection).
- Media files are exported with full URLs and generated checksums (md5_file) for data integrity verification on the other end.
- Added Migration/Export_Command.php to the standalone bootloader.

## Reviewer
- Reviewer: AI Agent
- Result: PASS
