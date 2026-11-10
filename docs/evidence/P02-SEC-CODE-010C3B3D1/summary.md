# P02-SEC-CODE-010C3B3D1 — shared-core direct-entry guard

Date: 2026-11-10. `Carmilla_Kernel.php` now exits before loading its dependencies or declaring the class when `ABSPATH` is absent. Normal WordPress host loading retains the existing code path.

Impact: GitNexus upstream class impact reported UNKNOWN/zero resolved callers on a stale MCP view. Text search confirmed both Theme `functions.php` and Bridge `carmilla-bridge.php` require and boot the shared kernel. No zero-caller safety inference was used.

Verification used fresh disposable WordPress 6.4.3/PHP 8.1.27/MariaDB 10.6 and synthetic installation credentials on an isolated Docker network. Both standard Bash builders produced delivery ZIPs from the changed source: Theme SHA-256 `786b5e3a5effe8346729194c850aaf7cc90184fed246d014206a5566838fe6f8`; Bridge SHA-256 `8f905d19f4099560c79765c67dee4e9b2206f8e781a3e82667134a08e648e58c`. PHP lint of the entry passed.

- Theme-only: installed and activated Theme ZIP; `Carmilla_Kernel::get_boot_contexts()` returned `theme`. Direct HTTP GET to its bundled `Carmilla_Kernel.php` returned status 200 with zero response bytes.
- Bridge-only on Twenty Twenty-Four: installed and activated Bridge ZIP; boot context returned `plugin`. Direct HTTP GET to its bundled entry returned status 200 with zero response bytes.
- Combined: activated the Theme with Bridge active; boot contexts returned `plugin,theme`. Container PHP/Apache logs showed no fatal error or PHP warning from these requests; only ordinary Apache startup notices matched the broad log pattern.

Both disposable containers and their network were removed. No manual owner QA is required for this bounded guard. This does not pass the whole Plugin Check gate.
