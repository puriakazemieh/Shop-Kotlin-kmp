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

## Correction recorded 2026-10-18

The PHP statement under “Declared support floor” above was imprecise: the
reference environment says PHP **8.1 is the reference version** and explicitly
lists **7.4 as minimum**; it does not declare 8.1 as the minimum. Both current
Theme and Bridge headers declare PHP 7.4 and WordPress 6.3 as minimum, whereas
the reference QA document specifies WordPress 6.4 or newer. The product support
contract and the tested reference matrix must be distinguished and reconciled
by an explicit decision and real compatibility tests. This correction does not
change any supported version or attest to a new test. The WooCommerce 8.3.1
versus documented 8.5 matrix mismatch remains unresolved.

## Additional isolated reference probe — 2026-10-18

WordPress 6.4.3/PHP 8.1.27/MariaDB 10.6.28 and official WooCommerce 8.5.2
were tested in a new disposable installation with current Carmilla source and
its shared core. Seven activation, synthetic product/cart and REST checks
passed; see [the exact source hash, commands and limitations](../P00-QA-COMPAT-024/verification.md).
The previously retained minimum matrix still uses Woo 8.3.1 and was not changed.
This does not establish a commercial minimum, ZIP compatibility, checkout,
upgrade/rollback, a full matrix or owner approval. P00-T07 remains open.
