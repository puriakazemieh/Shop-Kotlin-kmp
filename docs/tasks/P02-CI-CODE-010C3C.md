# P02-CI-CODE-010C3C — Exact Plugin Check regression gate

- Status: TODO
- Phase/Area/Type: P02 / CI / CODE+QA
- Priority/Risk/Size: P1 / HIGH / M
- Owner: AI
- Depends on: P02-CI-CODE-010C3B3, P02-SEC-CODE-010C3B3C1, and any other genuine P02 fix cards produced by B1–B3
- Blocks: P02-CI-CODE-010C3D

## Scope and acceptance

Build an exact, version-pinned baseline only for approved pre-existing Plugin Check findings from the delivery-shaped report. The checker must reject every new/unreviewed security, DB or other finding, malformed ledger, version drift and missing report; resolved findings may disappear. Keep source-backed dispositions and future-phase dependencies explicit. Run positive and synthetic new-finding negative checks locally and on an isolated PR; inspect job conclusions and uploaded full reports. Ensure action output/comment size cannot create false green or hide the report. Allowed: `.github/workflows/code-quality.yml`, `tools/code-quality/**`, `docs/**`, isolated synthetic CI branch. No product source fix. Evidence: `docs/evidence/P02-CI-CODE-010C3C/` with revisions, commands, run/job/artifact IDs and rollback. If any real security issue remains unapproved, stay BLOCKED.

## Completion record

- Final status: TODO.
