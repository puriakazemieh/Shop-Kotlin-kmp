# Business Summary: P09-BUSINESS-BIZ-012

- Task ID: P09-BUSINESS-BIZ-012
- Date: 2026-09-06
- Title: Closed Beta Feedback Taxonomy and Ticket-to-Task Triage Workflow

## Feedback Taxonomy & Categorization Rules

| Category | Definition & Scope | Example Issue | Priority / Target SLA | Conversion Target |
|---|---|---|---|---|
| **Product** | Feature gap, UX suggestion, or missing setting | "Desire option to hide product price when out of stock" | Medium / P2 | Backlog Feature Candidate |
| **Bug** | Functional error or unexpected crash | "Cart total calculation error on coupon apply" | High / P0-P1 | Bugfix Task Card in `docs/tasks/` |
| **Compatibility** | Issue specific to 3rd-party theme, plugin, or PHP version | "Conflict with Elementor footer script on WP 6.5" | High / P1 | Compatibility Patch in `carmilla-core` |
| **Docs** | Ambiguous guide, missing setup step, or API doc error | "Setup guide missing step for ZarinPal callback URL" | Low / P3 | Documentation Update in `docs/` |

## Ticket-to-Task Triage Workflow
1. Support ticket received from partner merchant.
2. Triage Lead assigns Category (`Product` / `Bug` / `Compatibility` / `Docs`) and Priority.
3. If valid bug/compatibility issue: Create traceable task card (`P09-FIX-*` or `CT-*`) in `docs/tasks/`.
4. Link ticket ID to task ID for full end-to-end traceability.

## Status
- Final Status: DONE
