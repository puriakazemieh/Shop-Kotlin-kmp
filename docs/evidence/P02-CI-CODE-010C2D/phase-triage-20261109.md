# Exact WPCS Security/DB phase triage — 2026-11-09

The prior strict gate was correctly red: it rejected every Security/DB sniff without regard to reviewed context or phase. The owner requested source-backed partitioning and transfer of later-phase work. This record supersedes the *blocker decision* in `blocker.md`; it does not change that earlier scan result or prove a remote PR job.

Input: `.artifacts/p02-ci-010c2/wpcs-current.json` (ignored local report), SHA-256 `3B69EBDF440F252996D1343E80FE0B8432BC6788D17908AEF44E88997E33BBD7`, WPCS 3.4.1 / PHPCS 3.13.6, source revision `7dbc2321208b5516167d4ec13ad4882537cfe4ea`. Product PHP source has not changed since this scan. Exact path/line/column/sniff/message/type and disposition for **all 168** findings are in `tools/code-quality/wpcs-security-dispositions.json`. Every entry cites a source-backed evidence file. The checker validates exact identity, disposition, evidence existence and a queued future task for every deferral. An unlisted new Security/DB finding, a changed finding, invalid ledger row or new style finding fails CI; resolved findings may disappear without causing failure. The old `strict` mode remains available to show raw debt.

| Disposition | Count | Follow-up |
| --- | ---: | --- |
| Context justified in current P02 safety review | 131 | Prior B1–B5 triage and focused implementation/QA evidence cited per finding. Includes prepared atomic auth/deletion queries, deliberate live transactional reads, trusted DDL identifiers and REST-authenticated upload checks. These are scanner findings, not a claim that every feature or later clinic HTTP QA passed. |
| Deferred to P04 WordPress admin/catalog/content | 15 | `P04-WP-ADMIN-DATA-052` (7), `P04-WP-CATALOG-CODE-055` (3), `P04-WP-CONTENT-PERF-CODE-056` (1), `P04-WP-CONTENT-PERF-CODE-057` (4). The new 057 card covers featured/campaign metadata queries; P04 CI now depends on it. |
| Deferred to P07 migration | 12 | `P07-MIGRATION-SEC-CODE-028` (2 SQL preparation fixes), `P07-MIGRATION-PERF-REVIEW-029` (10 measured performance follow-ups), both before the migration gate. |
| Deferred to P13 LMS | 2 | `P13-LMS-PERF-CODE-029` bounded lesson-question query. |
| Deferred to P14 clinic | 8 | `P14-CLINIC-PERF-CODE-034A/B/C/D` (2 each), all before clinic QA/gate. |

The two remaining clinic **HTTP** checks from P02-SEC-CODE-010C2B3A1/A3 were verified directly over real HTTP in Docker on 2026-11-14 (probe-clinic-availability-http-real-wp.php 9/9 PASS, probe-clinic-cancellation-http-real-wp.php 6/6 PASS) and both cards advanced to DONE. P14-CLINIC-QA-025A/B retain notes linking to this verified evidence; P14 gate remains TODO.

Local proof from repo root, Docker `python:3.12-alpine` (exit 0):

```powershell
docker run --rm -v "${PWD}:/app" -w /app python:3.12-alpine python tools/code-quality/probe_wpcs_baseline.py .artifacts/p02-ci-010c2/wpcs-current.json tools/code-quality/wpcs-style-baseline.json tools/code-quality/wpcs-security-dispositions.json
```

Results: exact style 5,425/5,425, 0 new; Security/DB 168/168 reviewed, 0 new; synthetic new style rejected (exit 1), synthetic new Security rejected (exit 1), missing evidence rejected as invalid input (exit 2), raw strict gate remains red (exit 1). This removes the local WPCS prerequisite for `P02-CI-CODE-010C2D`, which is now READY for actual isolated PR proof. Do not mark D or parent C2 DONE until remote positive/negative workflow jobs and uploaded reports are captured.

Isolated PR: https://github.com/puriakazemieh/Shop-Kotlin-kmp/pull/33 from `codex/p02-ci-phase-triage` to `develop`. The initial `opened` event on commit `cf87d1aa` had no associated workflow run as of 10:29 UTC; a follow-up PR synchronize event is required to test whether Actions will schedule the workflow.
