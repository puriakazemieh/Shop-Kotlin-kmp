# Evidence for P01-PAYMENT-SEC-017

## Changes Made
1. **Prevent Replay Attacks / Cross-Order Authority Sharing**:
   - In erify_payment (class-cb-payment-controller.php), added validation to ensure the incoming Authority matches the _cb_zp_authority metadata stored on the specific order.
   - This ensures that a single valid Authority generated for one order cannot be replayed or used to verify a different order.

2. **Prevent Wrong Amount Attacks**:
   - Calculated the $expected_amount exactly as it's done during equest_payment using the current order's total (cb_zp_amount( (float) ->get_total() )).
   - Verified that the $expected_amount matches the $saved_amount from _cb_zp_amount.
   - Used the $expected_amount during the actual cb_zp_verify call instead of blindly trusting the saved amount. This guarantees that ZarinPal verifies the amount that strictly matches the order's current total.

## Verification
- Lint check: php -l passed for class-cb-payment-controller.php.
- Baseline KMP build: .\gradlew.bat :composeApp:compileKotlinJvm executed successfully with BUILD SUCCESSFUL. No side-effects on the client side since the API contract didn't change (only internal validation).
