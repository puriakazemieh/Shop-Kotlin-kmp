# P02-PERF-DISC-010C2B3B1A — Design bounded lesson-question retrieval

- Status: DONE
- Phase/Area/Type: P02 / Performance / DISC
- Priority/Risk/Size: P2 / MEDIUM / S
- Owner: AI
- Depends on: P02-PERF-REVIEW-010C2B3B1
- Blocks: P02-PERF-REVIEW-010C2B3B2

## Scope and acceptance

Inspect the WordPress lesson-question response and all current KMP/WordPress consumers. Define an explicit page/cursor bound and backward-compatible transition that avoids silently dropping existing questions. Choose indexed SQL access or WordPress query arguments, ordering, and exact acceptance threshold using the 30/3,000/30,000 synthetic benchmark. Decompose the implementation into ordered XS/S/M cards if it crosses server/client contracts. Read-only product source; `docs/**` evidence. Do not claim the SlowDBQuery finding fixed by this design.

## Completion record

- Completed at: 2026-11-02
- Verification: inspected WordPress/Spring bare-list route and KMP network, repository, ViewModel and UI consumers; compared 30,000-row query plan and temporary-index experiment
- Evidence: `docs/evidence/P02-PERF-DISC-010C2B3B1A/decision.md`
- Follow-up: scoped WordPress fix `P13-LMS-PERF-CODE-029`; Spring/client parity remains separate P13/P15 work
- Final status: DONE
