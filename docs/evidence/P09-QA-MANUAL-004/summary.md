# Manual QA Evidence Handoff: P09-QA-MANUAL-004

- Task ID: P09-QA-MANUAL-004
- Date: 2026-09-06
- Title: Complete Shop-only Functional Test Suite on Release Candidate

## Test Instructions for User (Manual QA)
### Where to Look
- KMP Client Application (Android / Web / PWA)
- Storefront UI & Checkout Flow
- WordPress Admin Orders Page (`/wp-admin/edit.php?post_type=shop_order` or WooCommerce Orders)

### How to Test
1. **Authentication Flow:**
   - Test Login via OTP / Password. Verify token persistence.
2. **Catalog & Search:**
   - Browse Home screen, Categories, and Product Detail pages. Verify images and prices.
3. **Cart Operations:**
   - Add items to cart, modify quantities, remove item. Verify cart total computation.
4. **Checkout & Payment Return:**
   - Initiate checkout with ZarinPal / Direct Bank gateway.
   - Test successful payment return & verify order status is set to `Processing`/`Completed`.
   - Test canceled or failed payment: Verify cart items are **NOT** cleared upon failed or canceled payment attempt.

### Success Criteria
- 100% Critical functional test cases pass.
- Payment return state is authoritative from server.
- Failed payment does not clear cart items.

## Status
- Final Status: AWAITING_MANUAL_QA
