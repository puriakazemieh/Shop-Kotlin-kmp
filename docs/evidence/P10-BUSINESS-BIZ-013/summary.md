# Business Summary: P10-BUSINESS-BIZ-013

- Task ID: P10-BUSINESS-BIZ-013
- Date: 2026-09-06
- Title: Capacity-Based Limited Launch Policy and Emergency Stop Switch

## Capacity & Emergency Stop Switch Governance

### 1. Weekly Sales Quota
- **Maximum Weekly Sales Quota:** 10 new merchant licenses / week during initial launch month to match support team capacity.
- **Quota Control Mechanism:** Marketplace listing setting updated automatically when weekly quota limit reached ("Sold Out for This Week").

### 2. Emergency Stop Switch (Kill Switch)
- **Kill Switch Endpoint / Option:** `update_option('carmilla_kill_switch_active', true)`
- **Behavior When Active:**
  - Blocks new builder job execution gracefully with `503 Service Temporarily Paused for Maintenance`.
  - Preserves existing active sites and builds without disrupting live store operations.

## Status
- Final Status: AWAITING_MANUAL_QA
