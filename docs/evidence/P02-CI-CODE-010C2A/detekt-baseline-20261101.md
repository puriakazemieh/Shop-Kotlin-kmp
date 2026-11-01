# P02-CI-CODE-010C2A — detekt baseline evidence

- Source revision: `822cfa8a726c4d2173c1b2979fb5830e5e142426` (clean detached worktree, before this change).
- Tool: detekt CLI `1.23.8` release ZIP, SHA-256 `FF9F9258879FF2EC4349114740221498AFEC46A85CF6302C9F80B06EB4429501`.
- Inputs match `.github/workflows/code-quality.yml`: `composeApp,core,feature,androidApp,build-logic`, base path `.`.
- Baseline: `tools/code-quality/detekt-baseline.xml`, SHA-256 `B364BC18FC0A9382A5348372CA64E7676A84F83FE83D92148DF5F698A24EA3C4`, 2,373 `<ID>` entries and zero manual suppressions. Existing findings are bounded to native issue IDs; rules remain active.
- Generate: `detekt-cli --input composeApp,core,feature,androidApp,build-logic --base-path . --create-baseline --baseline tools/code-quality/detekt-baseline.xml` in the clean worktree: exit 0, 17 seconds.
- Positive: same CLI/input/base path with `--baseline tools/code-quality/detekt-baseline.xml --report sarif:.../detekt-positive.sarif`: exit 0, SARIF results 0.
- Negative: add an isolated synthetic Kotlin file with a wildcard import, invalid package and constant-return function; same CLI command: exit 1, SARIF results 3, diagnostics include all three. Removed the synthetic file afterward. This proves unseen findings fail.
- Local raw logs and SARIF: ignored `.artifacts/p02-ci-010c2/`; no synthetic file or generated binary is committed.
- GitHub Actions proof: disposable draft PR [#32](https://github.com/puriakazemieh/Shop-Kotlin-kmp/pull/32), based on source commit `e1d9e47bdc5703387cb90058886c85cebebcc37b`, adds only a text marker. [Lint & Code Quality run 41287611860](https://github.com/puriakazemieh/Shop-Kotlin-kmp/actions/runs/41287611860), `detekt` job `124792002525`: completed, success; CLI install, Run detekt, and SARIF upload steps all success. This verifies the committed baseline in the real CI runner. The overall workflow and other jobs are separate follow-up subtasks and are not claimed to pass here. Do not merge the disposable PR.
