# Blog/catalog meta query measurement — 2026-11-03

`blog-catalog-meta-probe.php` ran against disposable installed WordPress with WooCommerce 11.2.1. It inserted uniquely titled synthetic blog posts, campaigns and banners (30 then 3,000 of each), and created synthetic WooCommerce products through WC CRUD (30 then 300). It invoked the real `CB_Blog_Controller::featured` and `CB_Catalog_Controller::active_campaign`, `banners`, and `products` with price range/ascending/descending sort. It captured generated SQL, EXPLAIN and `ANALYZE FORMAT=JSON`, query latency and method latency. PHP lint passed; final probe exit 0, 12 PASS, 0 probe failures. All synthetic posts/products were removed; cleanup count zero. Raw SQL/plans: `raw-output.txt`. This is a review of behavior, not a claim that all product behavior passed.

| Path | Synthetic cardinality | SQL ms | Method ms | Plan / result |
| --- | ---: | ---: | ---: | --- |
| Featured blog | 3,000 | 7.721 | 15.772 | `wp_posts.type_status_date`, about 3,000 postmeta lookups, filesort; page size 20 |
| Active campaign | 3,000 | 7.390 | 9.103 | same post/meta join and filesort, limit 1 |
| Banners | 3,000 | 15.108 | 52.728 | `wp_postmeta` ALL (estimated 13,972 metadata rows), 3,000 banner lookups and numeric filesort; no output limit |
| Price range | 300 products | 0.871 | 10.755 | Generated product SQL has no price predicate. Returned `totalElements=301` and all 20 products on the first page outside requested 110–130 price range. At 30 products, `totalElements=31`, also 20/20 outside range. The extra one is pre-existing database content and was not modified. |
| Price ascending | 300 products | 2.046 | 9.514 | `_price` meta sort, 300-product bounded fixture; plan in raw output |
| Price descending | 300 products | 2.877 | 11.146 | `_price` meta sort, 300-product bounded fixture; plan in raw output |

The eight pinned WPCS findings map to featured blog (2), `_price` range (1), ascending/descending price sort (2), active campaign (2) and banners (1). The range warning points to a more serious correctness defect: WooCommerce 11.2.1 did not apply the controller's `meta_query` argument. `P04-WP-CATALOG-CODE-055` is queued. The banner path is materially unbounded at 3,000 rows; `P04-WP-CONTENT-PERF-CODE-056` is queued. Featured/campaign and price sorts were measured at the stated cardinalities; their observed latencies do not establish a production-scale waiver, and future performance gates must re-evaluate against representative budgets.

No personal/production data or WooCommerce orders were changed.
