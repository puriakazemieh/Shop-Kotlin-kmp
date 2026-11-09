# WPCS migration command triage — 2026-11-09

Pinned report `.artifacts/p02-ci-010c2/wpcs-report.json`, SHA-256 `5F3C5C4E430C39C7704F9B3ADB34348F5A6E394312421830CA0FD6285D7C3A64` (WPCS 3.4.1 / PHPCS 3.13.6). Filter: `WordPress.Security.*` or `WordPress.DB.*` in `Migration/Export_Command.php`, `Export_Overlay_Command.php`, `Import_Command.php`. Exactly 13 distinct line/column/sniff findings. Lines are from the pinned report.

| Path | Line:col | Sniff | Disposition | Source-backed reason / follow-up |
| --- | ---: | --- | --- | --- |
| `Migration/Export_Command.php` | 34:18 | `WordPress.DB.DirectDatabaseQuery.DirectQuery` | PERF REVIEW | One-shot export snapshots all allowed posts with `$wpdb->get_results`; no page bound. Measure scan/memory in P07-029. |
| same | 34:18 | `WordPress.DB.DirectDatabaseQuery.NoCaching` | PERF REVIEW | A migration snapshot should not reuse a stale object cache, but the uncached full read needs measured cost; P07-029. |
| same | 35:13 | `WordPress.DB.PreparedSQL.InterpolatedNotPrepared` | FIX REQUIRED | Denied types come from static `Migration_Config` and are `esc_sql` quoted, yet the SQL interpolates the list. Use value placeholders in P07-028. |
| same | 53:24 | `WordPress.DB.DirectDatabaseQuery.DirectQuery` | PERF REVIEW | Attachment snapshot reads all attachment rows and checks each file; measure cardinality/time in P07-029. |
| same | 53:24 | `WordPress.DB.DirectDatabaseQuery.NoCaching` | PERF REVIEW | One-shot attachment read need not be cached; memory and file checksum cost remain unmeasured, P07-029. |
| same | 102:22 | `WordPress.DB.DirectDatabaseQuery.DirectQuery` | PERF REVIEW | `exportable_meta` uses a prepared post-id query for every post; N+1 scaling needs measurement, P07-029. |
| same | 102:22 | `WordPress.DB.DirectDatabaseQuery.NoCaching` | PERF REVIEW | Meta snapshot must be fresh for export; repeated per-post uncached reads may dominate runtime, P07-029. |
| `Migration/Export_Overlay_Command.php` | 35:20 | `WordPress.DB.DirectDatabaseQuery.DirectQuery` | PERF REVIEW | Overlay snapshot joins posts to the trusted prefix seed table without a result bound; measure cost in P07-029. |
| same | 35:20 | `WordPress.DB.DirectDatabaseQuery.NoCaching` | PERF REVIEW | One-shot overlay query should see current seed changes, but uncached full scan needs a measured resource ceiling, P07-029. |
| same | 35:40 | `WordPress.DB.PreparedSQL.NotPrepared` | FIX REQUIRED | `$sql` contains trusted table identifiers but interpolates a quoted denied-type list; prepare value placeholders in P07-028. |
| same | 62:17 | `WordPress.DB.DirectDatabaseQuery.DirectQuery` | PERF REVIEW | Overlay `exportable_meta` reads per-post meta with a prepared ID, causing N+1 reads; P07-029. |
| same | 62:17 | `WordPress.DB.DirectDatabaseQuery.NoCaching` | PERF REVIEW | Fresh one-shot meta may be uncached, but cost is unmeasured; P07-029. |
| `Migration/Import_Command.php` | 61:81 | `WordPress.Security.EscapeOutput.ExceptionNotEscaped` | CONTEXT JUSTIFIED | `decode_ndjson` constructs an exception using only a local integer line index, and the result is sent to WP-CLI console, not HTML. This value cannot inject output markup. |

Count: 13; 2 FIX REQUIRED, 10 PERF REVIEW, 1 CONTEXT JUSTIFIED. P07-028 and P07-029 are queued before the migration gate; historical P07 statuses remain unchanged. This review does not claim the fixes or gate passed. No product source changed.
