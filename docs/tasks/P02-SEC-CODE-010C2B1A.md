# P02-SEC-CODE-010C2B1A — Redact public OTP provider errors

- Status: READY
- Phase/Area/Type: P02 / Security / CODE
- Priority/Risk/Size: P1 / HIGH / S
- Owner: AI
- Depends on: P02-CI-CODE-010C2B1
- Blocks: P02-CI-CODE-010C2C

## Scope and acceptance

The public OTP endpoints currently return filter or SMS provider error text through `cb_error`. Replace these with stable generic public errors without disclosing provider endpoint, credentials, or account details. Preserve safe operational diagnostics without secrets or personal data; keep retry and cooldown behavior. Cover override and direct adapter failure with synthetic automated tests. Allowed: `wordpress/carmilla-bridge/includes/class-cb-auth-controller.php`, focused auth tests, `docs/**`. For the changed network response, record exact manual QA environment, steps and expected output, and retain `AWAITING_MANUAL_QA` until owner confirmation. Evidence: `docs/evidence/P02-SEC-CODE-010C2B1A/`.

## Completion record

- Final status: READY
