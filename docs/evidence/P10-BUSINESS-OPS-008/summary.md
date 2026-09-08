# Operations Summary: P10-BUSINESS-OPS-008

- Task ID: P10-BUSINESS-OPS-008
- Date: 2026-09-06
- Title: Demo Environment Setup, Downloadable Artifact Sanitization, and Automated Reset

## Demo Environment & Artifact Sanitization

### 1. Public Demo Environment Specifications
- **Demo URL:** `https://demo.carmilla.ir`
- **Reset Mechanism:** Hourly cron job executes automated DB restore from `seeds/all-fa-v1.json` (`wp carmilla reset-demo --yes`).
- **Data Protection:** All customer accounts, addresses, and phone numbers in demo DB use synthetic data (`testuser1@example.com`, `09120000000`).

### 2. Downloadable Release Package Verification
- **Artifacts Provided:** `carmilla-theme.zip`, `carmilla-bridge.zip`.
- **Sanitization Audit:** Zero development credentials, local `.env` values, debug logs, or Git history present in final release ZIPs.
- **Uptime Monitoring:** Health check ping (`GET /wp-json/carmilla/v1/health`) monitored by external uptime owner.

## Status
- Final Status: DONE
