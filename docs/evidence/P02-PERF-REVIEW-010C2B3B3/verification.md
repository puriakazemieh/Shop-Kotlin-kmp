# Admin clinic patient message query measurement — 2026-11-03

`admin-clinic-meta-probe.php` ran against disposable installed WordPress/MariaDB in `test-env-wordpress-test-1`. It created only a synthetic user, therapist and `cb_msg` comments, invoked the real `CB_Admin_Clinic_Controller::list_messages` method, captured generated SQL, EXPLAIN and `ANALYZE FORMAT=JSON`, and measured method and SQL latency at three per-patient counts. PHP ran at 512 MiB for the 30,000-row case. `php -l` passed; probe exit 0, 3 PASS, 0 FAIL. Final cleanup verified zero fixture comments, therapist post and user. Raw plans and actual work: `raw-output.txt`.

| Patient messages | Returned | SQL ms | Method ms | Plan / actual work |
| ---: | ---: | ---: | ---: | --- |
| 30 | 30 | 0.374 | 4.430 | `wp_commentmeta` ALL, 30 comment lookups, temporary/filesort |
| 3,000 | 3,000 | 4.809 | 44.389 | `wp_commentmeta` ALL, 3,000 lookups, temporary/filesort |
| 30,000 | 30,000 | 71.978 | 459.582 | `wp_commentmeta` ALL, 30,000 lookups, temporary/filesort |

The two pinned `SlowDBQuery` findings identify the `meta_key`/`meta_value` filter in this one query. The 30,000-row method is materially slow and unbounded, so `P14-CLINIC-PERF-CODE-034D` is queued for a bounded contract and targeted query plan. The review is complete; the performance defect remains open. No personal or production data were used.
