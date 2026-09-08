# Manual QA Evidence Handoff: P09-QA-MANUAL-005

- Task ID: P09-QA-MANUAL-005
- Date: 2026-09-06
- Title: UI / Visual / RTL / LTR / Accessibility / Browser Test Suite

## Test Instructions for User (Manual QA)
### Where to Look
- Web/PWA Application on Chrome, Firefox, Safari, Edge
- Android Mobile App on various screen sizes (small phone, tablet)
- Language/Direction switchers (Persian RTL vs English LTR)

### How to Test
1. **RTL / LTR Layout Checks:**
   - Switch language between Persian (RTL) and English (LTR).
   - Check alignment of navigation bars, cards, icons, margins, paddings, text directions.
2. **Visual & Responsive Testing:**
   - Resize browser window from 320px up to 1920px.
   - Verify no text clipping, overlapping elements, horizontal scrollbar glitches, or broken layouts.
3. **Accessibility (a11y):**
   - Test keyboard navigation (Tab key focus states on forms, buttons, links).
   - Check color contrast ratios and screen reader accessibility labels.
4. **Cross-Browser Verification:**
   - Verify rendering on Chrome, Edge, Safari, and Firefox.

### Success Criteria
- Zero Severity 0/1 UI, RTL/LTR or accessibility bugs.
- Consistent visual presentation across browsers and screen sizes.

## Status
- Final Status: AWAITING_MANUAL_QA
