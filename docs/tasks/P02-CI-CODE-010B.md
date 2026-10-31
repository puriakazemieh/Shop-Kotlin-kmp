# P02-CI-CODE-010B — runnable WPCS and Theme Check jobs

- Status: DONE
- Phase/Area/Type: P02 / CI / CODE
- Priority/Risk/Size: P1 / MEDIUM / M
- Owner: AI; completion authority: BOTH for CI evidence
- Depends on: P02-CI-CODE-010A
- Blocks: P02-CI-CODE-010C
- Requirement: parent P02-CI-CODE-010 and active independent-products contract.

## Scope and acceptance

Replace the unavailable WPCS action, add Theme Check, and prove both run on the Bridge/Theme in a disposable PR. Record tool versions and baseline findings without waiving new violations or editing product code. Keep Plugin Check execution intact for 010C. Allowed: `.github/workflows/code-quality.yml`, `tools/**`, `docs/**`. No product source, dependency upgrade or production site. Run YAML and real CI; record job URLs, exits and redacted evidence in `docs/evidence/P02-CI-CODE-010B/`. Human product QA N/A.

## Completion record

- Started at: 2026-10-31
- Completed at: 2026-10-31
- Changed files: `.github/workflows/code-quality.yml`, this card, `docs/tasks.md`, and evidence checkpoint.
- Commands and exit codes: YAML parse and diff check exited 0. Actual CI runs 41221299123 and 41221594001 installed and executed both tools. Final WPCS exited 2 with existing Bridge findings; final Theme Check exited 1 with required/recommended Theme findings; both report artifacts uploaded. No clean checker result is claimed.
- Manual tester/date/result: N/A; disposable CI-only tooling change.
- Evidence paths: `docs/evidence/P02-CI-CODE-010B/checkpoint-20261031.md`; [final CI run](https://github.com/puriakazemieh/Shop-Kotlin-kmp/actions/runs/41221594001).
- Remaining risks/blockers: WPCS, Theme Check, detekt and Plugin Check are still red on baseline source findings. Regression policy is 010C; any product fix outside scope needs its own authorization/card.
- Final status: DONE (2026-10-31: tools actually executed; existing findings remain)
