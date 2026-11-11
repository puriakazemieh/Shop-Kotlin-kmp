# P02-SEC-CODE-010C3B2E1 — WooCommerce Checkout Acceptance Evidence

Date: 2026-11-11  
Commit: `c30aea4280b8e296d9d1eb280654b32df67d2adf`  
Package: Carmilla Bridge v1.0.0 built via `wordpress/build-bridge-zip.sh`  
Package SHA-256: `b3c581111a2709e821d86d3ab35619089ef3eb6fdc87b7fdcb8116a6e6da2290`  

## 1. Test Environment

- **WordPress:** 6.4.3
- **WooCommerce:** 8.6.0 (installed and activated)
- **Carmilla Bridge:** 1.0.0 (active)
- **PHP:** 8.1.27
- **Database:** MariaDB 10.6.28
- **Network / Containers:** Disposable Docker containers (`carmilla-wallet-qa-wp`, `carmilla-wallet-qa-db`) on dedicated bridge network `carmilla-wallet-qa-net`. Removed after evidence capture.

## 2. Test Execution & Results

### Step 1: Payment Gateway Settings in WooCommerce

- Ordinary test payment method Cash on Delivery (`cod`) enabled.
- Registered payment gateways in WooCommerce: `['bacs', 'cheque', 'cod']`.
- Available gateways: `['cod']`.
- **Carmilla Wallet (`carmilla_wallet`) in registered gateways:** **ABSENT (PASS)**
- **Carmilla Wallet (`carmilla_wallet`) in available gateways:** **ABSENT (PASS)**

### Step 2: Checkout Page & Store API Inspection

- Checkout page URL (`/checkout/`): inspected HTML and embedded JSON settings.
- `carmilla_wallet` or `Carmilla Wallet` in checkout page: **ABSENT (PASS)**
- Store API (`/?rest_route=/wc/store/v1/cart`) payment methods: `["cod"]`.
- `carmilla_wallet` in Store API payment methods: **ABSENT (PASS)**

### Step 3: Purchase Completion via Normal Payment Method (COD)

- **Synthetic Customer:** ID 4 (`buyer@example.test`)
- **Synthetic Product:** ID 14 ("Synthetic Test Widget", Price: $35.00)
- **Customer Wallet Balance Before Purchase:** 500.00
- **Wallet Ledger Table (`wp_carmilla_wallet_logs`) Records Before:** 0
- **Checkout Request:** Store API checkout with `payment_method = "cod"`
- **Result:**
  - Order ID: **#18**
  - Order Status: `processing`
  - Payment Method: `cod`
  - Customer Wallet Balance After Purchase: **500.00 (unchanged, PASS)**
  - Wallet Ledger Records After Purchase: **0 (unchanged, PASS)**

### Step 4: Checkout Attempt with `payment_method = carmilla_wallet`

- Product ID 14 re-added to cart.
- Customer wallet balance before attempt: 500.00
- Ledger records before attempt: 0
- **Checkout Request:** Store API checkout with `payment_method = "carmilla_wallet"`
- **Result:**
  - **HTTP Response Status:** `400 Bad Request`
  - **Error Code:** `rest_invalid_param`
  - **Error Message:** `Invalid parameter(s): payment_method`
  - **Rejected:** **YES (PASS)**
  - **Orders Created with `carmilla_wallet`:** **0 (PASS)**
  - **Customer Wallet Balance After Attempt:** **500.00 (unchanged, PASS)**
  - **Wallet Ledger Records After Attempt:** **0 (unchanged, PASS)**

### Step 5: Direct Gateway Invocation Guard (`process_payment`)

- Direct invocation of `WC_Gateway_Carmilla_Wallet::process_payment(18)`
- **Result:** Returned `null` (failed closed, PASS)
- **Notice Added:** `خطا در کسر موجودی.`
- **Customer Wallet Balance After Direct Call:** **500.00 (unchanged, PASS)**
- **Wallet Ledger Records After Direct Call:** **0 (unchanged, PASS)**

## 3. Summary & Conclusion

All 5 verification gates passed. Carmilla Wallet is completely unregistered and absent from WooCommerce settings, checkout blocks, and Store API. Checkout requests specifying `payment_method=carmilla_wallet` are rejected with HTTP 400 Bad Request, creating zero orders and leaving synthetic customer wallet balance and ledger records completely untouched. Direct gateway calls fail closed.

The technical acceptance scenario required by `P02-SEC-CODE-010C3B2E1` is fully verified.
