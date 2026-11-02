# P02-SEC-REVIEW-010C2B2A2B2D — Resolve native comment deletion conflict

- Status: DONE
- Phase/Area/Type: P02 / Security / REVIEW
- Priority/Risk/Size: P1 / HIGH / S
- Owner: AI
- Depends on: P02-SEC-CODE-010C2B2A2B2B
- Blocks: P02-SEC-CODE-010C2B2A2B2E

## Scope and acceptance

Read installed WordPress `wp_delete_user` behavior and evaluate bounded ways to preserve native clean deletion and reassignment while preventing nonzero comment `user_id` orphans under independent SQL writes and both host activation states. Compare a WordPress lifecycle adapter with database-enforced alternatives and failure/rollback behavior. Select a provable fail-closed design or document unsupported topology; do not change product code or schema in this review. Evidence: `docs/evidence/P02-SEC-REVIEW-010C2B2A2B2D/`.

## Completion record

- Completed at: 2026-11-02
- Evidence: `docs/evidence/P02-SEC-REVIEW-010C2B2A2B2D/design.md` and `trigger-probe.php`
- Verification: installed WordPress `wp_delete_user` source inspected; disposable MariaDB 10.6 scratch trigger/FK probe exit 0 and cleaned up
- Final status: DONE
