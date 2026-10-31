# P02-QA-CODE-007C6A checkpoint — 2026-10-31

Baseline `ed7604a8`. Three separate PHP 8.1/WP 6.4.3/Woo 8.5.2 CI cells run the DB-only DDL-fault, access-token and session-adapter smokes. Each has its own disposable MariaDB service, named schema and random `qa_<12 hex>_` prefix. Token/adapter use the existing fixture-only `synthetic_qa` service user and explicit loopback port 3306. DDL-fault creates a `synthetic_fault` user with CREATE only on its one prefixed `carmilla_web_families` table; the test must confirm the second DDL fails and a retry fails closed. The fixture exists only for that CI job and GitHub Actions removes its service container afterward.

All three tests exited 2 when invoked without fixture environment under Docker PHP 8.1 with no network. PHP 8.1 lint on each exited 0. GitNexus cannot resolve the workflow YAML path (`risk: UNKNOWN`), so exact case dispatch, schema and credential scope were text-reviewed. Actual positive CI is pending; no DB-only PASS is claimed yet.
