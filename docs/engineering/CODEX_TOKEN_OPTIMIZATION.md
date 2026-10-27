# Codex token efficiency — client + Spring server (2026-10-27)

Scope: `puriakazemieh/Shop-Kotlin-kmp` and `puriakazemieh/Shop-Kotlin-Spring-Boot`, based on their `develop` branches. No application behavior, task queue, gate, or production data is changed by this tooling task.

## 1. Token baseline (local-only)

The GitHub connection has **no access to local Codex session logs**; a numeric before/after baseline cannot honestly be claimed from repository contents. This is instrumentation, not a completed measured comparison. From the KMP repository's Windows PowerShell directory, BEFORE enabling GitNexus:

```powershell
.\tools\codex\measure-codex-usage.ps1 -Label baseline
```

This uses `ccusage codex session --json --no-cost` and saves raw token statistics under `.artifacts/codex-usage/` (gitignored). It aggregates retained local Codex CLI sessions across projects; it does **not** determine how many tokens a specific Shop task consumed without matching sessions. Sessions through other Codex environments may not appear. Record representative task, model, reasoning effort, outcome, and session ID privately. Do not upload session logs.

After running comparable new tasks with the same model and effort:

```powershell
.\tools\codex\measure-codex-usage.ps1 -Label after
```

Compare only matched newly completed sessions and success rate; note that cached input, internal reasoning, multiple turns, and tool calls affect totals. Do not subtract two complete cumulative JSON reports as if the difference were reliably attributable to one task.

## 2. Agent instructions and bounded context

- The client `AGENTS.md` preserves queue order, complete task-card reading, evidence and manual QA rules, `RTK.md` guardrails, and the smallest-safe-diff requirement. It adds targeted search and optional up-to-date graph retrieval.
- The Spring server now has a concise `AGENTS.md` to apply matching navigation, data/security and verification rules.
- No arbitrary task was selected from `docs/tasks.md`. This maintenance work is outside the product-task queue.
- Avoid loading whole `docs/evidence`, master plans, or both repositories for small changes. Search code first and inspect actual sources even when graph results exist.
- Index only source in each repository: these are **separate** graphs, not an automatic cross-repository knowledge graph.

## 3. GitNexus installation / indexing on the Windows development machine

Requirements: `git`, `npx`, and Node.js **22.18+ in 22.x or 24.11+**. **License notice:** GitNexus is under the [PolyForm Noncommercial 1.0.0 license](https://github.com/abhigyanpatwari/GitNexus/blob/main/LICENSE). Check whether your planned commercial Shop development is permitted or obtain a commercial license **before running the installer**. On Windows, keyword/FTS queries can require the x64 Visual C++ Redistributable and OpenSSL 3 runtime; missing these may let indexing finish while searches return no results. From the checked-out KMP repository:

```powershell
.\tools\codex\setup-gitnexus.ps1 -ServerRepoPath 'D:\Android\AndroidStudioProjects\ShopServer\Shop'
```

The path shown comes from `docs/baseline/repository-state.md` (2026-07-29), **not** a live filesystem check. Replace if your checkout moved. The script validates both Git directories; runs `npx --yes gitnexus@latest analyze --index-only` in each; checks `gitnexus status`; and runs **one** `gitnexus setup -c codex` (global MCP + skills + hooks). It intentionally avoids unqualified `gitnexus analyze` because that may rewrite the existing `AGENTS.md`. The generated `.gitnexus/` folders are ignored by Git.

Verify in each repository:

```powershell
npx --yes gitnexus@latest status
```

Verify Codex MCP:

```powershell
codex mcp list
```

Restart Codex and approve new hooks via `/hooks` when requested. **Do not install the Codex GitNexus plugin as well as the setup integration**, or hooks may execute twice. If npm 11.x errors while fetching, consult the upstream GitNexus pnpm workaround rather than bypassing the failure silently.

Important: checking code into GitHub **does not install** software or MCP config on the user's Windows PC or into Codex Cloud. Those require an actual run on the machine/environment that uses Codex. Only use graph results if the index is fresh; refresh with `npx --yes gitnexus@latest analyze --index-only` after major changes.

Sources: [GitNexus](https://github.com/abhigyanpatwari/GitNexus), [ccusage Codex logs](https://ccusage.com/guide/codex/).
