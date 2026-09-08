# Operations Evidence Summary: P09-QA-OPS-017

- Task ID: P09-QA-OPS-017
- Date: 2026-09-06
- Title: Release Drill, Rollback Procedure, Database Restore, and Incident Simulation

## Operations Verification Results

### 1. Release Drill
- Build ZIP artifacts (`carmilla-theme.zip`, `carmilla-bridge.zip`) generated with SHA256 checksum manifests.
- Signature verification passed.

### 2. Rollback Procedure Test
- Simulated rollback from RC build to baseline version.
- WordPress CPTs, custom database tables (`carmilla_orders`, `carmilla_entitlements`), and user sessions preserved without data loss.

### 3. Database Restore Test
- Full backup export and restore drill executed on test database.
- Schema integrity verified 100%.

### 4. Incident Simulation & Support Bundle Redaction
- System support bundle generator tested (`wp-json/carmilla/v1/debug-bundle`).
- **PII / Secret Redaction Audit:** Database passwords, JWT secret keys, API nonces, and customer phone numbers successfully scrubbed / replaced with `[REDACTED]`.

## Status
- Final Status: DONE
