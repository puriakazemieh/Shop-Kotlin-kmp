# P02-QA-CODE-007 scope and size gate — 2026-10-28

## Baseline

- Parent dependency `P02-QA-CODE-006` is `DONE`; `P02-QA-CODE-007` is the first unresolved P02 row in queue order.
- The task card's allowed paths are `composeApp/**`, `core/**`, `feature/**`, `wordpress/**`, and `docs/**`. It explicitly requires `BLOCKED` and an owner scope decision when another path is necessary.
- The existing integration workflow is `.github/workflows/wordpress-integration.yml`, outside the card's allowed paths. It configures PHP 7.4/8.1/8.2, WordPress latest/6.4.3, WooCommerce latest, and a clean installation, then blindly runs every `smoke*.php` in the Bridge tests directory.
- Several `smoke*-real-wp.php` files require disposable flags and a random `qa_..._` table prefix. Several hardcode `ABSPATH=/usr/src/wordpress/` and dedicated synthetic database names; the workflow installs WordPress in `wp-env/` and does not provide those fixtures. For example, `smoke-web-login-real-wp.php` exits 2 without `CARMILLA_LOGIN_DISPOSABLE=yes` and a valid prefix.
- The existing evidence documents workflow creation and a KMP JVM compile, but no successful WordPress/Woo matrix run or clean activation smoke in CI.

## Size decision and blocker

The parent combines a multi-version WordPress/Woo/PHP installation matrix, synthetic databases, real-WP smoke compatibility, CI orchestration and negative gate validation. This crosses more than an M-sized vertical slice. No source or workflow code was edited. The parent is `BLOCKED` pending an owner decision to authorize ordered child cards and include `.github/workflows/wordpress-integration.yml` in the applicable child scopes. Do not mark the parent or its phase gate complete on static workflow text.

## Proposed ordered XS/S/M children

1. **P02-QA-CODE-007A — S, smoke inventory and fixture contract.** Scope: `wordpress/carmilla-bridge/tests/**`, `wordpress/carmilla-theme/**` only for inventory, and `docs/**`. Prerequisite: owner approves the split. Classify every `smoke*.php` as hermetic or real-WP, list PHP/WP/Woo prerequisites, disposable flags, database names and path assumptions, and define a synthetic fixture/cleanup contract. Acceptance: complete test inventory and reproducible local commands, with no customer data. Evidence: inventory, exact commands, expected exits and redacted output in `docs/evidence/P02-QA-CODE-007A/`.
2. **P02-QA-CODE-007B — M, core installation matrix.** Scope: `.github/workflows/wordpress-integration.yml`, `wordpress/carmilla-bridge/tests/**`, `wordpress/carmilla-theme/**`, `docs/**`. Prerequisite: 007A and explicit approval of `.github/workflows/**` scope. Build disposable PHP/WP/Woo matrix cells, clean install and activate Theme and Bridge, run the hermetic/activation smoke subset with fail-on-error and reports. Acceptance: each declared matrix cell runs and a forced failing smoke makes its cell red. Evidence: local Docker reproduction where possible plus actual CI run URL, commit, matrix cells and redacted logs in `docs/evidence/P02-QA-CODE-007B/`.
3. **P02-QA-CODE-007C — M, real-WP smoke integration.** Scope: `.github/workflows/wordpress-integration.yml`, `wordpress/carmilla-bridge/tests/**`, `docs/**`. Prerequisite: 007B. Run real-WP smokes in disposable databases with per-test random prefixes, correct installation path and required flags; isolate/clean fixtures and preserve failure exit codes. Acceptance: real-WP smoke subset runs under the declared supported matrix or an explicitly justified reduced matrix, clean install and activation remain green, and a negative fixture fails CI. Evidence: redacted local/CI reports with PHP/WP/Woo versions, commit and exit codes in `docs/evidence/P02-QA-CODE-007C/`.

The parent can be reconsidered only after the child evidence satisfies its original acceptance. If a child requires UI/network/migration manual QA, record exact environment, synthetic data, steps and success criteria and keep it `AWAITING_MANUAL_QA` until human confirmation. No child was created or executed in this decomposition turn.
