# Evidence for P06-MESSAGE-CODE-010

## Baseline
- git status: Clean before changes.

## Changes
- Updated wordpress/carmilla-bridge/includes/class-cb-auth-controller.php
- Modified issue_otp to call CB_SMS_HTTP_Adapter::send.
- If SMS sending fails (returns WP_Error), it clears the cooldown transients and throws an Exception.
- This ensures sent=true is only returned when the provider accepts the request.

## Reviewer
- Reviewer: AI Agent
- Result: PASS
