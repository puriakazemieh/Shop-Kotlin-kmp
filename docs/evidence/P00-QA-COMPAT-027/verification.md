# P00-QA-COMPAT-027 — isolated browser checkout

2026-10-18. Automated result: PASS within the scope below. Human acceptance: **AWAITING_MANUAL_QA**. No P00 Gate or T07 approval is implied.

## Environment and results

- Disposable, synthetic WordPress 6.4.3 / PHP 8.1.27 / WooCommerce 8.5.2 / MariaDB 10.6.28, WP-CLI 2.12.0; actual Chromium-based Chrome driven by Playwright.
- Installed the Theme and Bridge ZIPs staged by [026](../P00-QA-COMPAT-026/verification.md), with recorded SHA-256 `839f43b678b82e783951535e59d2d47bd226edc78e7a548d3a427fec74aaa8c3` and `8d6cbc239a8f33079184aaaf2776583a88c6993b88df5f3360c0423169ed13e6` respectively. These predate later inactive Bridge prototypes and are not final release artifacts.
- `PASS browser cart contains synthetic product and quantity`; `PASS browser synthetic offline checkout reached order confirmation`; independently `PASS synthetic offline order persisted with expected item and total`. Fixture scripts passed PHP 7.4 syntax checks and browser script passed `node --check`.
- Test uses a fictional customer/address, one synthetic virtual product, and WooCommerce COD as an offline test method; no gateway transaction occurs. A must-use test filter suppresses email and `WP_HTTP_BLOCK_EXTERNAL` blocks WordPress external HTTP. This is not evidence of actual payment, email delivery, Iranian checkout validation, PWA authentication, Blocks checkout, upgrade/rollback, other versions or standalone modes.
- Source, package and fixture mounts were read-only. Both application and database storage are tmpfs in new named Docker containers, bound only to loopback. Existing preserved WordPress installations were not touched. The fixture remains running for manual review; it is temporary and will lose its synthetic data if containers stop or Docker restarts. It is not one of the user's persistent sites.
- Initial installation produced a malformed test-only home URL and repeated PHP warnings in `wp-includes/l10n.php:1134`; the browser fixture identity guard stopped before cart mutation after the redirect. The synthetic home and site URLs were corrected; the later successful browser run produced no such warnings in the inspected subsequent Apache log. This observation does not prove absence of other warnings in other environments.

## Manual QA for this exact temporary fixture

1. While Docker and these containers remain running, open `http://127.0.0.1:57873/?add-to-cart=12`. This is a separate synthetic site, not either saved WordPress site.
2. Open `http://127.0.0.1:57873/?page_id=10`; confirm `Synthetic QA Product`, quantity and total appear and can be adjusted. The browser's own cart is separate from the automation's cart.
3. Proceed to `http://127.0.0.1:57873/?page_id=11`; fill the required fields with fictional values, choose the synthetic offline method, and place the test order. Success: an order confirmation appears; no money is charged and no email is sent. Do not enter real personal or payment information.
4. Report the result and any screenshot without personal information. This confirms only this task's narrow manual scope; T07 and P00 acceptance remain open.

If the temporary URL no longer works, do not reset an existing WordPress site. Recreate a new isolated synthetic fixture using the read-only staged ZIPs and the guarded scripts under `wordpress/carmilla-bridge/tests/`, after confirming their guard and container isolation; rerun and record its new URL and evidence.
