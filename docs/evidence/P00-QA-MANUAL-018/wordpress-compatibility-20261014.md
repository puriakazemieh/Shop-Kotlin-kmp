# P00-T07 — WordPress/PHP/WooCommerce compatibility revalidation

- **Date:** 2026-10-14
- **Status:** `BLOCKED — support contract mismatch`

## Declared support floor

`docs/qa/REFERENCE_ENVIRONMENTS_FA.md` declares WordPress 6.4+, PHP 8.1+
and WooCommerce 8.5+ (with HPOS).

## Executed QA matrix

The retained local matrix includes WordPress 6.4/PHP 8.1/WooCommerce 8.3.1,
WordPress 6.6/PHP 8.2, and current WordPress/PHP 8.3/WooCommerce 11.2.1.
The three local WordPress REST roots answered HTTP 200 during the earlier
P00–P09 revalidation.

## Result

The minimum matrix member uses WooCommerce 8.3.1, below the documented 8.5
floor. It is useful as a compatibility probe, but it cannot prove the stated
minimum contract. Before a release gate, the product owner must either:

1. update the supported WooCommerce floor with a dated business/support
   decision, or
2. rebuild the minimum matrix with WooCommerce 8.5+ and re-run the install,
   activation, cart, and checkout smoke.

No production endpoint or customer data was used.
