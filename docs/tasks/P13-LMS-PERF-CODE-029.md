# P13-LMS-PERF-CODE-029 — Add bounded WordPress lesson-question pages

- Status: TODO
- Phase/Area/Type: P13 / LMS / PERF-CODE
- Priority/Risk/Size: P2 / MEDIUM / M
- Owner: AI
- Depends on: P13-LMS-CODE-009
- Blocks: P13-LMS-GATE-027

## Scope and acceptance

Implement only the opt-in versioned WordPress lesson-question page route from `docs/evidence/P02-PERF-DISC-010C2B3B1A/decision.md`. Keep the existing bare-list response unchanged for old clients. Use deterministic ascending date/ID keyset ordering, validated lesson-bound opaque cursor, default 50/max 100 items and no silent omission within a page. On 30,000 synthetic course comments, verify each page is bounded, the SQL plan avoids a global commentmeta scan, and a 100-item page meets the local 20 ms SQL target; verify consecutive pages have no duplicates/gaps under a fixed snapshot. If the implementation exceeds M or the canonical P13 LMS contract differs, decompose or reconcile before coding. Allowed: WordPress Bridge LMS endpoint, focused tests and `docs/**`. Evidence: `docs/evidence/P13-LMS-PERF-CODE-029/`. Spring/KMP parity is a separate prerequisite to switching clients; this card does not claim legacy-route removal.

## Completion record

- Final status: TODO
