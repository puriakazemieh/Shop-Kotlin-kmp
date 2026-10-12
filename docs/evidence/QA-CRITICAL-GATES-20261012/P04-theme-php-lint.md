# P04 Theme PHP lint

- Date: 2026-10-12
- Scope: `wordpress/carmilla-theme/functions.php`
- Runtime: Docker PHP 8.1 CLI

## Command

```powershell
docker run --rm -v "${PWD}:/app" -w /app php:8.1-cli php -l wordpress/carmilla-theme/functions.php
```

## Result

- Exit code: 0
- Output: `No syntax errors detected in wordpress/carmilla-theme/functions.php`

The full WordPress lint was also started. It stopped at the known Phase 07 Migration syntax failure in `wordpress/carmilla-bridge/includes/Migration/Export_Command.php`; this does not invalidate the successful, file-scoped Theme bootstrap lint above.
