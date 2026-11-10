# P02-CI-CODE-010C3D — Triage Theme Check required findings and artifact scope

- Status: TODO
- Phase/Area/Type: P02 / CI / REVIEW
- Priority/Risk/Size: P1 / HIGH / M
- Owner: AI
- Depends on: P02-CI-CODE-010C3A1 (pinned Theme Check report; independent of the Plugin Check parent gate)
- Blocks: P02-CI-CODE-010C3E

## Scope and acceptance

Using the pinned full Theme Check JSON and the actual Theme ZIP from each supported build path, inspect every REQUIRED/WARNING/RECOMMENDED finding. Separate active independent commercial-Theme behavior from any WordPress.org UI-only channel requirement using the active contract; do not infer permission to ship a security issue. Specifically trace copied `carmilla-core/tests`, possible missing core in the package workflow ZIP, `base64_encode`/`base64_decode` in message settings, `wp_link_pages`, `readme.txt`, and any forbidden API/secret/output finding. Record exact finding identity, severity, source-backed disposition, owner task and phase dependency. Create S/M package/product fix cards for genuine issues; do not edit product or package workflows in this review. Allowed: read-only Theme/core/package source, `tools/code-quality/**`, `docs/**`. Evidence: `docs/evidence/P02-CI-CODE-010C3D/` with complete report, artifact inventory, channel decision and follow-up cards.

## Completion record

- Final status: TODO.
