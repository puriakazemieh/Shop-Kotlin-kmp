# P02-CI-CODE-010C2 — bounded detekt/WPCS baseline closeout

Date: 2026-11-09 (Asia/Tehran). Repository: `D:\Android\AndroidStudioProjects\kmp-shop`, branch `codex/p02-ci-phase-triage`, pre-closeout HEAD `f240a69d`. This is the parent closeout for ordered children C2A, C2B1–B6, C2C and C2D; each is DONE in `docs/tasks.md` with its own implementation or triage evidence. No product PHP source differs from `develop` in the final branch (`git diff --name-only develop..HEAD -- wordpress/carmilla-bridge` returned no path). Existing user-owned workspace/log/ZIP changes were preserved.

## Baselines and gate

- detekt CLI 1.23.8, pinned release ZIP SHA-256 `FF9F9258879FF2EC4349114740221498AFEC46A85CF6302C9F80B06EB4429501`; exact CI inputs `composeApp,core,feature,androidApp,build-logic`. Native `tools/code-quality/detekt-baseline.xml` SHA-256 `B364BC18FC0A9382A5348372CA64E7676A84F83FE83D92148DF5F698A24EA3C4`, 2,373 bounded issue IDs. C2A generation, local positive/negative and earlier CI evidence: `docs/evidence/P02-CI-CODE-010C2A/detekt-baseline-20261101.md`.
- WPCS 3.4.1 / PHPCS 3.13.6, `WordPress` standard. Exact 5,425-entry nonsecurity style baseline `tools/code-quality/wpcs-style-baseline.json` SHA-256 `A1E57367B6F59AAF197B9AB10D2D53B2511D9748C8584016860F91FD68D86B9B`. C2C creation and synthetic check: `docs/evidence/P02-CI-CODE-010C2C/verification.md`.
- Owner-approved exact Security/DB ledger `tools/code-quality/wpcs-security-dispositions.json` SHA-256 `CE425B26C17550EA0AF6D32B4F1661F6DEBAFFFBE1C2ECC2071CB9FD2B6797C3`: 168 current findings, 131 source-justified and 37 linked to explicit P04/P07/P13/P14 tasks. New or changed unreviewed findings fail. The 37 follow-ups and clinic HTTP QA are **not** declared resolved or accepted; see `docs/evidence/P02-CI-CODE-010C2D/phase-triage-20261109.md`.
- CI workflow `.github/workflows/code-quality.yml` uses the pinned detekt install and exact WPCS phase checker. No blanket count threshold, rule-wide suppression or product source change was introduced.

## Verification actually performed

From repository root, Docker `python:3.12-alpine`:

```powershell
docker run --rm -v "${PWD}:/app" -w /app python:3.12-alpine python tools/code-quality/probe_wpcs_baseline.py .artifacts/p02-ci-010c2/wpcs-current.json tools/code-quality/wpcs-style-baseline.json tools/code-quality/wpcs-security-dispositions.json
```

Exit 0: all six assertions PASS. Current report: style 5,425/baseline 5,425/new 0; Security/DB 168/unreviewed 0. Synthetic new style and Security findings each fail with exit 1; missing ledger evidence fails as invalid input with exit 2. Raw strict mode remains red on the 168 reported sniffs, which is why the exact reviewed phase policy is explicit rather than a silent waiver.

Actual isolated [PR #33](https://github.com/puriakazemieh/Shop-Kotlin-kmp/pull/33): [positive run 42601296282](https://github.com/puriakazemieh/Shop-Kotlin-kmp/actions/runs/42601296282) and final clean-head [run 42602414295](https://github.com/puriakazemieh/Shop-Kotlin-kmp/actions/runs/42602414295) both show detekt and WPCS jobs **success**, with `detekt-report` and `wpcs-report` artifacts uploaded. [Synthetic run 42602066787](https://github.com/puriakazemieh/Shop-Kotlin-kmp/actions/runs/42602066787) shows WPCS **failure** on one new exact finding and an uploaded report; the temporary PHP file was removed before the final clean run. Job IDs, artifact IDs, comparison output and rollback commit are in `docs/evidence/P02-CI-CODE-010C2D/remote-ci.md`. The final CI run was rechecked in this closeout: detekt and WPCS success; reports `13667085383` and `13666262794` present.

## Limit and next gate

The **whole** Lint & Code Quality workflow remains failed on separate Theme Check and Plugin Check jobs. Their exact bounded baselines are the scope of the next queued `P02-CI-CODE-010C3`, followed by full quality-gate proof in C4. Therefore this C2 parent establishes detekt/WPCS acceptance only; `P02-CI-CODE-010`, P02 phase gate, and later-phase security/performance/clinic QA are not marked passed. No human manual test or owner-only decision is needed for this CI closeout. The PR remains open and unmerged; rollback of this closeout is a normal revert of its documentation commit, while the baseline implementation commits remain separately traceable.
