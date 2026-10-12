# P07 — XAMPP WordPress smoke test

Date: 2026-10-12

## Isolated runtime

- Official WordPress 7.1.3 was downloaded and installed outside both source repositories at `D:\Carmilla-QA\p07-wordpress`.
- The test used the local XAMPP runtime: PHP 8.2.12 and MariaDB 10.4.
- A dedicated, disposable database was used. No production or customer data was imported.
- Carmilla Bridge was activated in the fresh WordPress installation.

## Findings fixed before the test

- The Shared Core bootstrap returned before its dependencies were loaded because the class name was evaluated with autoloading enabled.
- The standalone Bridge bootstrap calculated the plugin directory as `includes/includes`.
- Boot checkpoints were stored across requests, preventing standalone dependencies from loading on later requests.

## Result

The WordPress-backed migration smoke test passed with all checks:

1. Synthetic records were inserted.
2. Source mappings were stored.
3. Parent/child relationships were restored in the second pass.
4. Legacy URLs were rewritten.
5. Allowed metadata was imported.
6. Blocked billing-email metadata was not imported.
7. A repeated unchanged import was skipped.

Additionally, `php -l` passed for all 78 PHP files in Carmilla Bridge and Shared Core, and the Bridge plugin reported `Active` in the isolated WordPress installation.
