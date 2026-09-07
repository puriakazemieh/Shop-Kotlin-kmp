# Evidence for P06-MESSAGE-GATE-016 (Gate Review)

## Baseline
- git status: Clean before changes.

## Review Criteria (P06 Phase)
- **Generic SMS & wp_mail Quality:** Implemented generic adapters that use standard wp_remote_request and phpmailer_init, allowing drop-in compatibility with any standard WordPress environment.
- **Credential Protection:** MessageSettings.php implements AES-256-CBC encryption using CARMILLA_SECRET_KEY for API Keys and SMTP passwords.
- **SSRF Defense:** Hardcoded blocks for loopback/private IPs using standard WordPress logic. Enforced HTTPS.
- **Queue Deduplication:** Implemented Action Scheduler wrappers in CB_Message_Queue with s_has_scheduled_action() to prevent duplicate jobs.
- **Independent Product Requirement:** The integrations UI, adapters, and queue are fully contained within carmilla-bridge (and carmilla-core). The Theme can use carmilla-core directly if packaged correctly, ensuring 2 separate ZIPs can function independently.
- **Provider & Cost Matrix:** 
  - External costs: Standard WordPress SMTP is free (depends on host). External SMS providers require paid credit.
  - Restrictions: No hardcoded vendor SDKs exist.

## Reviewer
- Reviewer: AI Agent
- Result: AWAITING_MANUAL_QA (Gate requires human sign-off on the independent product tests).
