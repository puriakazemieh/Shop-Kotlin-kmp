# Exact WPCS style baseline — 2026-11-09

Source revision: `7dbc2321208b5516167d4ec13ad4882537cfe4ea` (product source unchanged during this task). Pinned tools: WPCS 3.4.1, PHPCS 3.13.6, WordPress standard, PHP 8.1 container. Command:

`docker run --rm -v "${PWD}:/app" -w /app php:8.1-cli php .artifacts/p02-ci-010c2/wpcs/vendor/bin/phpcs --standard=WordPress --ignore='*/vendor/*,*/node_modules/*,*/tests/*' --report=json wordpress/carmilla-bridge`

The fresh report is local/ignored at `.artifacts/p02-ci-010c2/wpcs-current.json` (SHA-256 `3B69EBDF440F252996D1343E80FE0B8432BC6788D17908AEF44E88997E33BBD7`); scan exit 2 is expected for findings. It contains 5,593 total findings: 5,425 nonsecurity style and 168 `WordPress.Security.*`/`WordPress.DB.*`. `tools/code-quality/wpcs-style-baseline.json` (SHA-256 `A1E57367B6F59AAF197B9AB10D2D53B2511D9748C8584016860F91FD68D86B9B`) records every pre-existing style finding by path, line, column, sniff, message, type, and multiplicity, plus source/tool pins. Security/DB findings are absent from the baseline.

`check_wpcs_baseline.py` fails on every new exact style finding. Strict mode also fails while any Security/DB finding remains. The workflow now uploads a machine-readable JSON report and runs strict mode. No rule-wide suppression or security/DB waiver was added.

Verification: `docker run --rm -v "${PWD}:/app" -w /app python:3.12-alpine python tools/code-quality/probe_wpcs_baseline.py .artifacts/p02-ci-010c2/wpcs-current.json tools/code-quality/wpcs-style-baseline.json` exited 0. Its three assertions passed (`probe-output.txt`): current style scan clean with zero new findings; strict scan red with 168 unresolved Security/DB findings; synthetic style violation red with one new finding. This proves baseline behavior, not a positive CI result. The unresolved findings are tracked in B1–B6 triage and future-phase fix/review cards; they block the strict CI gate and the positive combined proof in D. No owner manual QA is required for this baseline task.
