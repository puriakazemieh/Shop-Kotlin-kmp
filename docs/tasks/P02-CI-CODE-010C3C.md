# P02-CI-CODE-010C3C — Exact Plugin Check regression gate

- Status: BLOCKED (isolated CI gate proven; real package findings and wallet checkout acceptance remain)
- Phase/Area/Type: P02 / CI / CODE+QA
- Priority/Risk/Size: P1 / HIGH / L (checker, workflow and isolated PR; decomposed)
- Owner: AI
- Depends on: P02-CI-CODE-010C3C3, P02-SEC-CODE-010C3B3C1, P02-SEC-CODE-010C3B3D1, P02-CI-CODE-010C3C4D, and wallet checkout acceptance P02-SEC-CODE-010C3B2E1
- Blocks: P02-CI-CODE-010C3D

## Scope and acceptance

Build an exact, version-pinned baseline only for approved pre-existing Plugin Check findings from the delivery-shaped report. The checker must reject every new/unreviewed security, DB or other finding, malformed ledger, version drift and missing report; resolved findings may disappear. Keep source-backed dispositions and future-phase dependencies explicit. Run positive and synthetic new-finding negative checks locally and on an isolated PR; inspect job conclusions and uploaded full reports. Ensure action output/comment size cannot create false green or hide the report. Allowed: `.github/workflows/code-quality.yml`, `tools/code-quality/**`, `docs/**`, isolated synthetic CI branch. No product source fix. Evidence: `docs/evidence/P02-CI-CODE-010C3C/` with revisions, commands, run/job/artifact IDs and rollback. If any real security issue remains unapproved, stay BLOCKED.

## Completion record

- Current status: BLOCKED after C3C1–C3C3. The mechanism passed positive/negative synthetic CI, but the actual package has seven rejected fingerprints and wallet checkout remains unverified. `docs/evidence/P02-CI-CODE-010C3C/blocker-20261110.md` records ordered S/M corrective cards C3C4A–D and the exact remaining gate. No full regression gate or phase PASS is claimed.
