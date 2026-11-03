# WPCS admin/content/catalog triage — 2026-11-03

Pinned report: `.artifacts/p02-ci-010c2/wpcs-report.json`, SHA-256 `5F3C5C4E430C39C7704F9B3ADB34348F5A6E394312421830CA0FD6285D7C3A64` (WPCS 3.4.1, PHPCS 3.13.6). Filter: `WordPress.Security.*` and `WordPress.DB.*` in the six named files. Exactly 22 findings. Line numbers refer to the pinned report, not a future modified source.

| File | Line | Sniff | Disposition | Source-backed reason / follow-up |
| --- | ---: | --- | --- | --- |
| `class-cb-admin-content-controller.php` | 302 | `WordPress.DB.SlowDBQuery.slow_db_query_meta_key` | PERF REVIEW | `refund_requests` scans up to 500 users by `cb_course_refunds`; cardinality and omissions unmeasured. B4C. |
| same | 464 | `WordPress.DB.DirectDatabaseQuery.DirectQuery` | FIX REQUIRED | `therapist_slots` reads live booking locks; direct read is needed for freshness but DB error becomes empty bookings. B4A. |
| same | 464 | `WordPress.DB.DirectDatabaseQuery.NoCaching` | FIX REQUIRED | Caching live booking state could stale; verify failure handling, not a cache waiver. B4A. |
| same | 464 | `WordPress.DB.PreparedSQL.NotPrepared` | FIX REQUIRED | Table name comes from trusted `cb_bookings_table()` and therapist ID is prepared; no request-driven table injection, but failed read must fail closed. B4A. |
| same | 501 | `WordPress.Security.NonceVerification.Missing` | CONTEXT JUSTIFIED | `upload_media` is an admin REST POST, registered under `require_admin`; REST cookie nonce/bearer authentication precedes callback, and media upload is handled by WordPress. |
| same | 548 | `WordPress.Security.NonceVerification.Missing` | CONTEXT JUSTIFIED | `add_lesson_file` is another admin REST POST under `require_admin`, with the same REST authentication boundary. |
| same | 613 | `WordPress.DB.SlowDBQuery.slow_db_query_meta_key` | PERF REVIEW | `switch_requests` scans up to 500 users with `cb_switch_requests`; cardinality/omissions unmeasured. B4C. |
| same | 744 | `WordPress.DB.SlowDBQuery.slow_db_query_meta_key` | PERF REVIEW | `pending_interpretations` scans `cb_psych_attempts`, capped at 500 users; cardinality/omissions unmeasured. B4C. |
| same | 761 | `WordPress.DB.SlowDBQuery.slow_db_query_meta_key` | PERF REVIEW | `interpret` searches at most 500 users for an attempt ID, potentially missing an older user; B4C. |
| same | 860 | `WordPress.DB.SlowDBQuery.slow_db_query_meta_key` | PERF REVIEW | `review_user_meta_list` scans at most 500 users for a request ID; B4C. |
| `class-cb-admin-controller.php` | 443 | `WordPress.DB.SlowDBQuery.slow_db_query_meta_key` | PERF REVIEW | `return_requests` scans at most 500 users with `cb_returns`; B4C. |
| same | 459 | `WordPress.DB.SlowDBQuery.slow_db_query_meta_key` | PERF REVIEW | `update_return` searches at most 500 users for a request ID; B4C. |
| `class-cb-admin-product-controller.php` | 67 | `WordPress.Security.NonceVerification.Missing` | CONTEXT JUSTIFIED | `upload_attachment` is reached from admin REST image/video POST routes protected by `require_admin`; WordPress REST authenticates the cookie nonce or bearer token before callback. |
| `class-cb-blog-controller.php` | 146 | `WordPress.DB.SlowDBQuery.slow_db_query_meta_key` | PERF REVIEW | Featured-post query is page-bounded but meta scan may grow with posts. B4D. |
| same | 147 | `WordPress.DB.SlowDBQuery.slow_db_query_meta_value` | PERF REVIEW | Same featured-post query filters on unindexed `'1'` value; B4D. |
| `class-cb-catalog-controller.php` | 155 | `WordPress.DB.SlowDBQuery.slow_db_query_meta_query` | PERF REVIEW | Numeric `_price` BETWEEN filter on WooCommerce products; needs dataset/plan measurement. B4D. |
| same | 179 | `WordPress.DB.SlowDBQuery.slow_db_query_meta_key` | PERF REVIEW | Ascending `_price` meta sort; B4D. |
| same | 184 | `WordPress.DB.SlowDBQuery.slow_db_query_meta_key` | PERF REVIEW | Descending `_price` meta sort; B4D. |
| same | 491 | `WordPress.DB.SlowDBQuery.slow_db_query_meta_key` | PERF REVIEW | Active campaign lookup returns one post but may scan all matching metadata; B4D. |
| same | 492 | `WordPress.DB.SlowDBQuery.slow_db_query_meta_value` | PERF REVIEW | Same campaign lookup filters unindexed `'1'` value; B4D. |
| same | 527 | `WordPress.DB.SlowDBQuery.slow_db_query_meta_key` | PERF REVIEW | Banner list is unbounded (`numberposts => -1`) and sorts numeric `sort_order`; B4D. |
| `meta-boxes.php` | 134 | `WordPress.Security.ValidatedSanitizedInput.InputNotSanitized` | FIX REQUIRED | `save_post` checks nonce and `edit_post`, then unslashes the field and sanitizes by type before `update_post_meta`; however an array posted to scalar fields can reach scalar sanitizers. B4B adds input-shape validation. |

Disposition count: 3 context justified, 4 fix required, 15 performance review = 22. `B4A` and `B4B` are security/availability fixes. `B4C` and `B4D` require bounded synthetic cardinality, query plans and follow-up optimization if material. No performance finding is waived without measurement; no product source changed in this review.
