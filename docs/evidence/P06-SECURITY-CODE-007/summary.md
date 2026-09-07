# Evidence for P06-SECURITY-CODE-007

## Baseline
- git status: Clean before changes.

## Changes
- Updated wordpress/packages/carmilla-core/inc/Settings/MessageSettings.php
- Added encryption (encrypt_secret) and decryption (decrypt_secret) for secrets (sms_api_key, smtp_pass).
- Relies on CARMILLA_SECRET_KEY (outside DB) or falls back to SECURE_AUTH_KEY.

## Reviewer
- Reviewer: AI Agent
- Result: PASS
