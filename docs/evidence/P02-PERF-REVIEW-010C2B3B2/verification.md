# Clinic meta query measurement — 2026-11-02

## Method

`clinic-meta-probe.php` created only synthetic user, therapist, appointment and `cb_msg` comment rows in the disposable Docker WordPress database. It invoked the three real controller methods at 30, 3,000 and 30,000 rows, captured the generated SQL, EXPLAIN, `ANALYZE FORMAT=JSON`, SQL latency and method latency. The final run used `php -d memory_limit=512M` to complete the 30,000-row case. `php -l` passed; final probe result: 9 PASS, 0 FAIL. Raw output: `raw-output.txt`; additional 3,000-row run: `raw-3000.txt`.

| Rows | Method | Returned | SQL ms | Method ms | Observed plan / actual work |
| ---: | --- | ---: | ---: | ---: | --- |
| 30 | my_appointments | 30 | 0.240 | 1.871 | `wp_posts.type_status_date`, 30 postmeta lookups, temporary/filesort |
| 3,000 | my_appointments | 100 | 3.327 | 6.237 | `wp_postmeta` ALL, 3,000 post lookups, filesort |
| 30,000 | my_appointments | 100 | 47.082 | 51.011 | `wp_postmeta.meta_key` range (estimated 15,010), 30,000 post lookups, filesort |
| 30 | list_messages | 30 | 0.249 | 2.277 | `wp_commentmeta.meta_key`, 30 comment lookups, filesort |
| 3,000 | list_messages | 3,000 | 4.378 | 39.866 | `wp_commentmeta` ALL, 3,000 lookups, filesort |
| 30,000 | list_messages | 30,000 | 72.527 | 443.103 | `wp_commentmeta` ALL, 30,000 lookups, filesort |
| 30 | messaging_status | 3 | 0.441 | 0.581 | `wp_commentmeta.meta_key`, 30 comment lookups |
| 3,000 | messaging_status | 3 | 2.758 | 3.395 | `wp_commentmeta` ALL, 3,000 lookups |
| 30,000 | messaging_status | 3 | 53.600 | 55.705 | `wp_commentmeta` ALL, 30,000 lookups |

The initial 30,000-row `list_messages` run at PHP's 128 MiB limit exhausted memory in `class-wp-object-cache.php`; see `raw-oom-128m.txt`. That run's finally cleanup did not execute. We identified and deleted **only its tagged synthetic fixtures**, verified zero remaining fixture posts/comments/users and orphan metadata, then reran at 512 MiB. The final probe cleanup also verified zero synthetic rows. This is a material failure, not a waived finding.

## Follow-up

- `P14-CLINIC-PERF-CODE-034A`: appointment ownership query still scans/sorts 30,000 rows for 100 results.
- `P14-CLINIC-PERF-CODE-034B`: unbounded message list scans all metadata, costs 443 ms, and exhausts 128 MiB at 30,000 rows.
- `P14-CLINIC-PERF-CODE-034C`: status count scans all metadata at 30,000 rows for three results.

This review is complete; the measured performance defects remain open in those implementation tasks. No production or personal data were used.
