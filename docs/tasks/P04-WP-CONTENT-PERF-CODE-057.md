# P04-WP-CONTENT-PERF-CODE-057 — Measure and bound featured/campaign metadata reads

- Status: TODO
- Phase/Area/Type: P04 / WordPress Content / PERF-CODE
- Priority/Risk/Size: P2 / MEDIUM / M
- Owner: AI
- Depends on: P04-WP-CONTENT-PERF-CODE-056
- Blocks: P04-CI-CODE-019

## Scope and acceptance

Resolve the deferred WPCS meta-filter findings at `CB_Blog_Controller` featured-post query and `CB_Catalog_Controller` active-campaign query. Use the measured P02 baseline in `docs/evidence/P02-PERF-REVIEW-010C2B4D/verification.md`. At 30/3,000 synthetic posts/campaigns, record SQL plan, scanned rows, latency, result order and response compatibility; apply a bounded indexed/query strategy if material, or document a measured safe bound and exact residual sniff justification. Preserve active/featured semantics and pagination. Allowed: the two WordPress controllers, focused tests and `docs/**`. Evidence: `docs/evidence/P04-WP-CONTENT-PERF-CODE-057/`. No live customer data or blanket WPCS suppression.

## Completion record

- Final status: TODO; source follow-up transferred from P02 CI triage on 2026-11-09.
