# Admin user-meta scans — 2026-11-03

Disposable installed WordPress/MariaDB probe `admin-user-meta-probe.php` inserted uniquely prefixed synthetic users with four admin meta keys, invoked the real admin controller methods, captured generated SQL, EXPLAIN and `ANALYZE FORMAT=JSON`, and removed all fixtures. PHP lint passed. Run with `CARMILLA_LOGIN_DISPOSABLE=yes php -d memory_limit=512M /tmp/admin-user-meta-probe.php`: exit 0, 16 PASS, 0 FAIL; cleanup verified zero synthetic users. See `raw-output.txt` for every plan and actual row count.

| Synthetic users with each meta key | List output | Representative SQL ms | Method ms | Query plan |
| ---: | ---: | ---: | ---: | --- |
| 30 | 30 | 0.235–0.281 | 1.002–1.393 | `wp_usermeta.meta_key` ref, 30 user lookups, temporary/filesort |
| 501 | 500 | 1.175–1.338 | 5.566–7.244 | `wp_usermeta.meta_key` range, 501 lookups, filesort, LIMIT 500 |
| 3,000 | 500 | 4.107–4.833 | 11.330–11.467 | `wp_usermeta.meta_key` range, 3,000 user lookups, filesort, LIMIT 500 |

The seven pinned call sites cover `refund_requests`, `switch_requests`, `pending_interpretations`, `interpret`, `review_user_meta_list` (exercised via `review_refund`), `return_requests`, and `update_return`. At 3,000 users, the target item was deliberately placed in the last synthetic user. `interpret`, `review_refund`, and `update_return` each returned 404 even though the target existed; its meta status remained unchanged. Their SQL calls took 4.122–5.038 ms and the methods 11.772–12.998 ms. This is a correctness failure due to the hard 500-user cap, alongside scan growth; it is not a performance waiver.

Queued fixes: `P04-WP-ADMIN-DATA-052` creates a durable item-to-user lookup, `P04-WP-ADMIN-CODE-053` uses it for review mutations, and `P04-WP-ADMIN-CODE-054` exposes complete bounded list traversal. No product code changed in this review; no personal or production data was used.
