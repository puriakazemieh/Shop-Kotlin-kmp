# Manual QA Evidence Handoff: P09-QA-MANUAL-015

- Task ID: P09-QA-MANUAL-015
- Date: 2026-09-06
- Title: Expanded Beta Rollout (8-12 Merchants) Following Initial Cohort Gate Sign-off

## Test Instructions for User (Manual QA)
### Where to Look
- Closed Beta Merchant Cohort 2 Staging & Production Environments (8 to 12 stores)
- Beta Issue Tracker & Blockers Backlog

### How to Test
1. **Gate Verification of Initial Cohort:**
   - Verify all top blockers identified in cohort 1 (3-5 partners) are resolved and deployed in RC build.
2. **Expand Cohort Onboarding:**
   - Expand closed beta access to additional 8 to 12 merchants across different hosting environments (cPanel, Nginx, Apache, LiteSpeed).
3. **Multi-Host Performance & Compatibility Check:**
   - Monitor store performance, API response times, and payment callback reliability across all 8-12 merchant environments.
4. **Top Blocker Verification:**
   - Confirm zero P0/P1 blockers remain open across the expanded merchant cohort.

### Success Criteria
- 8 to 12 active merchants running closed beta candidate.
- Zero open P0/P1 blockers.

## Status
- Final Status: AWAITING_MANUAL_QA
