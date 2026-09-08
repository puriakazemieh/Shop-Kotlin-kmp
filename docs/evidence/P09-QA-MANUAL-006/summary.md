# Manual QA Evidence Handoff: P09-QA-MANUAL-006

- Task ID: P09-QA-MANUAL-006
- Date: 2026-09-06
- Title: Resilience & Network Edge Cases (Offline, Timeout, Retry, Duplicate, Process Death)

## Test Instructions for User (Manual QA)
### Where to Look
- KMP Client Application (Android / Web)
- Network Simulator / Airplane Mode Toggle
- Device App Task Switcher & Force Close

### How to Test
1. **Offline State Handling:**
   - Turn on Airplane mode while navigating app or placing order.
   - Verify clear offline banner and retry action without app crash.
2. **Network Timeout & Retry:**
   - Simulate high latency / 3G network timeout during checkout or cart operations.
   - Verify idempotent retry mechanism prevents double order creation.
3. **Duplicate Request Defense:**
   - Double-tap "Submit Payment" or "Place Order" button rapidly.
   - Verify server deduplication handles idempotent requests correctly without double charges or duplicate database records.
4. **Process Death / App Backgrounding:**
   - Put app in background during checkout flow and kill process (Android Developer Options -> Don't keep activities or Force Stop).
   - Reopen app and verify state is restored cleanly without corrupted data loss.

### Success Criteria
- Zero duplicate writes or duplicate order records in DB.
- Zero data loss or state corruption after process death.

## Status
- Final Status: AWAITING_MANUAL_QA
