# P02-PERF-REVIEW-010C2B3B1 — lesson question query measurement

2026-11-02. Disposable installed WordPress fixture (WordPress 7.1.3, PHP 8.3.35, MariaDB 10.6.28). `CARMILLA_LOGIN_DISPOSABLE=yes php /tmp/cb-lesson-query-probe.php` inserted synthetic `cb_lesson_q` comments with `lesson_index` metadata on one new course, captured the actual SQL emitted by `CB_Academy_Controller::lesson_questions`, and ran `EXPLAIN` plus `ANALYZE FORMAT=JSON`. Exit 0, all measurement cases PASS. Raw plans and measurements: `raw-output.txt`; 3,000-row intermediate run: `raw-3000.txt`. PHP lint exit 0.

| Course comments | Returned | Controller latency | SQL latency | Plan |
| ---: | ---: | ---: | ---: | --- |
| 30 | 15 | 0.889 ms | 0.200 ms | `wp_commentmeta` full scan, estimated 30 rows; temporary table and filesort. |
| 3,000 | 1,500 | 10.205 ms | 2.728 ms | Same full scan, estimated 3,000 rows. |
| 30,000 | 15,000 | 119.908 ms | 42.147 ms | Same full scan, estimated 30,000 rows; actual query 40.004 ms and 15,000 output rows. |
| 30,000 with temporary `(meta_key(191),meta_value(20),comment_id)` index | 15,000 | 97.109 ms | 39.986 ms | Optimizer still chose full `wp_commentmeta` scan of 30,000 rows. |

Both pinned `slow_db_query_meta_key` and `slow_db_query_meta_value` findings refer to this unbounded comment-meta filter. The measured large fixture is materially slower and returns an unbounded payload. The temporary index did not resolve the plan; a contract-aware bounded pagination design is needed. Queued `P02-PERF-DISC-010C2B3B1A` to decide compatibility and a measured implementation scope before code. This review changed no product source.

Cleanup verified: synthetic course/comments removed; original fixture returned to 1 comment and 0 commentmeta rows, with no temporary `cb_qa_lesson_meta` index. No production data was touched.
