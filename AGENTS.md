# Carmilla — AI agent instructions

You are an **Implementer** and **Verifier** for the KMP client, WordPress products, and related Spring Boot server.

## Task execution protocol (STRICT — never relax to save tokens)
1. **Queue first:** For queued product work, inspect `docs/tasks.md` and select **only the first READY task** in queue order. Never take an arbitrary TODO, BLOCKED or later READY task. If none is READY, check dependencies and the last active task, **STOP**, and report the blocker; do not invent work.
2. **One task per turn:** Execute **exactly one** queued task per turn. Do not automatically implement the next task even if the current one finishes.
3. **Required task context:** Before implementation, read `docs/tasks/<TASK-ID>.md` **in full** plus the active `docs/INDEPENDENT_PRODUCTS_SPEC_FA.md` contract **in full**, as required by `docs/tasks.md`. Consult the relevant acceptance sections of `docs/MASTER_IMPLEMENTATION_CHECKLIST_FA.md` and applicable `RTK.md` rules. Follow task-specific scope, dependencies, and acceptance criteria.
4. **Mandatory size and ambiguity gate:** If a task is **L/XL (larger than M)**, spans too many boundaries, is ambiguous, or has unresolved dependencies, **STOP before coding**. Break down the oversized work into **ordered XS/S/M subtasks** with explicit scopes, prerequisites, acceptance criteria, and evidence requirements; propose the breakdown. Create or reorder queued subtask cards only when authorized by the task/owner and without invalidating dependencies. Do not execute a subtask until it is legitimately READY, and never in the same turn as decomposition. For ambiguity or missing approvals, report the open decision and wait.
5. **Scope and safety:** Before editing, check `git status --short`. Preserve user-owned changes; never reset or overwrite them. Implement only the selected task, make the smallest safe diff, and avoid unrelated refactors or upgrades. Never expose secrets, customer data, or PII; observe `RTK.md` stop-ship rules.
6. **Verification and evidence:** Run relevant automated checks; do not mark a queued task DONE without evidence in `docs/evidence/<TASK-ID>/` and applicable acceptance gates. For UI, network, or migration changes, provide **where to test, exact manual steps, and success criteria**; retain `AWAITING_MANUAL_QA` until the user confirms. Human/external blockers must be reported as `AWAITING_MANUAL_QA` or `BLOCKED`, not silently passed.
7. **Queue advancement:** Only after real completion, update **the current task's row** in `docs/tasks.md` and its card Completion record. Update the Master checklist only with verified evidence. If dependencies are satisfied, the next task may become READY, but **do not execute it in this turn**. Never mark historical tasks or phase gates complete without proof.
8. **Maintenance exception:** If the owner explicitly requests agent/tooling maintenance outside the product queue, isolate it and **do not change task statuses or claim a phase gate passed**.

## Token-efficient navigation (subordinate to the strict task protocol)
- Start from task paths or narrowly scoped `rg --files` / `rg -n`; read relevant symbols and code regions, not whole directories. Avoid bulk-reading unrelated task cards, `docs/evidence/**`, or generated/build output. **Never skip required full task-card and active-contract reads.**
- If GitNexus MCP is connected **and the index is current**, use graph `query` / `context` / `impact` for substantial dependency tracing and verify against actual code. If unavailable, search normally. Skip graph overhead for obvious single-file changes.
- Bound output, avoid repeated scans, and run focused checks first; run broader mandatory gates when needed. Never sacrifice correctness, queue discipline, security, or evidence for token savings.

## Post-commit graph refresh and weekly Codex usage reporting
- **After every successful Git commit created by this agent**, refresh the GitNexus index for **each repository in which a commit was made** before concluding the task. In that repository run `gitnexus analyze --index-only` and then `gitnexus status`; inspect the exit codes and freshness. Do not regenerate or overwrite `AGENTS.md`, do not commit generated `.gitnexus/` data, and do not create an extra commit just for indexing. If GitNexus is unavailable or indexing fails, report the precise blocker; do not claim the graph is current. This instruction covers **commits made by Codex**; Git commits made independently in an IDE/terminal use the local `post-commit` hook to run the same refresh without making another commit.
- **Once per ISO calendar week, on the next active local Codex session** (not a background job), check for the canonical report `<KMP repo>/.artifacts/codex-usage/weekly/YYYY-Www.md`. If absent and local ccusage/Codex logs are accessible, run `npx --yes ccusage@latest codex daily --last 7 --json --no-cost` (and `codex session --json --no-cost` only if needed for session-level analysis). Verify the returned JSON, save raw aggregate data and a concise Markdown summary alongside the marker under the ignored `.artifacts/codex-usage/weekly/` folder, and **show the user the weekly summary in the Codex chat**. Include the exact covered dates, measured input/output/cached token counts and total, limitations, and comparison to a prior matching period **only if valid and comparable**. The report is local Codex CLI-log usage, not ChatGPT subscription credits nor reliably restricted to Shop; do not invent savings. Create the weekly marker only after successful measurement and reporting. If unavailable, explain briefly without blocking the task or claiming a report was delivered. Reuse the shared KMP marker when working from the server repository to prevent duplicate reports.
- Reporting is best-effort **while Codex is actively used**, never a promise to send notifications when Codex is closed. This maintenance must never bypass queued-task constraints, required QA, security checks, or explicit owner approval.

## Useful commands
- Git state: `git status --short; git log -1 --oneline`
- KMP checks (Windows): `./gradlew.bat :composeApp:compileKotlinJs :composeApp:compileKotlinJvm`
- WordPress/PHP Docker procedure: `RTK.md`
- Token measurement + GitNexus setup: `docs/engineering/CODEX_TOKEN_OPTIMIZATION.md`

Report task outcome, actual tests, blockers, and required manual QA concisely.

<!-- gitnexus:start -->
# GitNexus — Code Intelligence

This project is indexed by GitNexus as **Shop-Kotlin-kmp** (85307 symbols, 131409 relationships, 387 execution flows).

> Index stale? Run `node .gitnexus/run.cjs analyze --index-only` from the project root — it auto-selects an available runner. No `.gitnexus/run.cjs` yet? Bootstrap with `npx`, `bunx`, or `pnpm dlx` — e.g. `bunx gitnexus@latest analyze` (npm 11 npx crash; #1939).

## Always Do

- **MUST run impact before editing.** Use `impact({target: "symbolName", direction: "upstream"})` or `node .gitnexus/run.cjs impact "symbolName" --direction upstream --repo .`; report callers, processes, and risk. Never substitute grep for graph analysis. For unified PDG impact, add `mode: "pdg"` with optional `line: <N>` — it returns statement-level `affectedStatements` over CDG + REACHING_DEF and inter-procedural symbols in `interproceduralByDepth`/`byDepth`; no-layer/degraded PDG results are UNKNOWN-risk notes (`--pdg` layer). CLI equivalent: `node .gitnexus/run.cjs impact "symbolName" --direction upstream --mode pdg --line <N> --repo .`.
- **MUST analyze graph changes before committing.** Use `detect_changes({scope: "all"})` (MCP) or `node .gitnexus/run.cjs detect-changes --scope all --repo .` (CLI fallback). `partial: true` or `truncated: true` is not a clean check — a zero means unseen, not unaffected; re-run it. For regression review: `detect_changes({scope: "compare", base_ref: "develop"})` or `node .gitnexus/run.cjs detect-changes --scope compare --base-ref "develop" --repo .`.
- MUST warn on HIGH/CRITICAL `risk` pre-edit; never use `riskSharedAxes` to waive a HIGH/CRITICAL `risk` warning. Compare File/symbol: MCP File omits axes; Graph-RAG expands File.
- **MUST treat `risk: UNKNOWN` as unresolved, not as low.** An empty caller set is not evidence the symbol is unused — it can also mean the callers are not resolvable by the index (plain-object property access, dynamic dispatch, cross-language calls). `impact` pairs `UNKNOWN` with a `riskNote` saying so. Confirm with a text search before treating the symbol as safe to change or delete; do not proceed on the strength of a zero.
- **MUST use `query({search_query: "concept"})` for concepts/flows, `context({name: "symbolName"})` for a named symbol, or `impact` for blast radius, on read-only callers, dependencies, imports, or execution flow.** Graph first; text search only for empty/`UNKNOWN`/literals.
- For security review, `explain({target: "fileOrSymbol"})` lists taint findings (source→sink flows; needs `analyze --pdg`).
- For control/data dependence, `pdg_query({mode: "controls", target: "fileOrSymbol"})` answers "under what condition does X run?" (CDG, incl. guard clauses) and `pdg_query({mode: "flows", target, variable})` traces "where does variable Y flow?" (REACHING_DEF). `--pdg` layer.

## Never Do

- NEVER edit a function, class, or method before MCP/CLI impact analysis.
- NEVER ignore HIGH or CRITICAL risk warnings from impact analysis, and never read `UNKNOWN` as an all-clear — it means the walk could not answer, which is the one verdict that requires confirming by other means.
- NEVER rename symbols with find-and-replace — use `rename` which understands the call graph.
- NEVER commit before MCP/CLI graph change analysis.

## Resources

| Resource | Use for |
| --- | --- |
| `gitnexus://repo/Shop-Kotlin-kmp/context` | Codebase overview, check index freshness |
| `gitnexus://repo/Shop-Kotlin-kmp/clusters` | All functional areas |
| `gitnexus://repo/Shop-Kotlin-kmp/processes` | All execution flows |
| `gitnexus://repo/Shop-Kotlin-kmp/process/{name}` | Step-by-step execution trace |

## CLI

| Task | Read this skill file |
| --- | --- |
| Understand architecture / "How does X work?" | `.claude/skills/gitnexus-exploring/SKILL.md` |
| Blast radius / "What breaks if I change X?" | `.claude/skills/gitnexus-impact-analysis/SKILL.md` |
| Trace bugs / "Why is X failing?" | `.claude/skills/gitnexus-debugging/SKILL.md` |
| Rename / extract / split / refactor | `.claude/skills/gitnexus-refactoring/SKILL.md` |
| Tools, resources, schema reference | `.claude/skills/gitnexus-guide/SKILL.md` |
| Index, status, clean, wiki CLI commands | `.claude/skills/gitnexus-cli/SKILL.md` |

<!-- gitnexus:end -->
