# P00-T05 — repair of disposable Web/PWA commerce fixture

- **Date:** 2026-10-20
- **Environment:** local disposable Docker WordPress on `localhost:8080` and
  the retained local PWA on `localhost/carmilla-pwa-qa/`
- **Purpose:** restore synthetic catalogue data for the pending human Web/PWA
  smoke; no production system or customer data was used.

## Finding

The catalogue endpoint returned HTTP `503` with `WOOCOMMERCE_INACTIVE`. The
PWA therefore rendered its generic error state and showed no product to add to
the cart.

## Forward fix and verification

WooCommerce was installed and activated only in the disposable WordPress
environment. Its required local tables were initialized, and one published
synthetic product, **Carmilla P00 QA Product**, was created with a test price
of **100,000 IRR**. The endpoint
`/wp-json/carmilla/v1/api/products` then returned HTTP `200`. A fresh PWA
reload rendered the product card and price.

## Remaining manual check

This does not close P00-T05. A human must still verify the product-detail,
add-to-cart, login/registration, and persisted cart/session path in the PWA
and record the result without including credentials or personal data.

## Rollback

The data exists only in the disposable local WordPress Docker environment. It
can be discarded by recreating that explicitly named test environment; no
retained WordPress copy, production environment, or repository data was
changed.
