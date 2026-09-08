# Manual QA Evidence Handoff: P09-QA-MANUAL-013

- Task ID: P09-QA-MANUAL-013
- Date: 2026-09-06
- Title: Closed Beta UAT Cohort (3-5 Design Partners with Sanitized Data)

## Test Instructions for User (Manual QA)
### Where to Look
- Design Partner Pilot Environments (Staging Sites for 3-5 merchants)
- WooCommerce Payment Reconciliation Reports & Gateway Portals

### How to Test
1. **Sanitized Onboarding:**
   - Deploy candidate build to cohort of 3 to 5 pilot merchant staging environments.
   - Verify sample products, test categories, and sanitized customer accounts are loaded.
2. **End-to-End Merchant Journey UAT:**
   - **Merchant A (Ecommerce):** Execute store management, product creation, cart checkout, and ZarinPal payment verification.
   - **Merchant B (Academy):** Execute course enrollment, lesson view, quiz completion.
   - **Merchant C (Clinic):** Execute appointment booking, provider slot management.
3. **Payment Reconciliation & Financial Audit:**
   - Audit WooCommerce order amounts vs payment gateway transaction logs for all UAT purchases.
   - Verify zero payment discrepancy or currency/total mismatches.
4. **Partner Sign-off Gathering:**
   - Collect formal UAT sign-off document/email from each design partner representative.

### Success Criteria
- Sign-off obtained from each of the 3-5 pilot design partners.
- Zero payment mismatch or order state discrepancies.

## Status
- Final Status: AWAITING_MANUAL_QA
