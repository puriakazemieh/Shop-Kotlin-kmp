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

## Useful commands
- Git state: `git status --short; git log -1 --oneline`
- KMP checks (Windows): `./gradlew.bat :composeApp:compileKotlinJs :composeApp:compileKotlinJvm`
- WordPress/PHP Docker procedure: `RTK.md`
- Token measurement + GitNexus setup: `docs/engineering/CODEX_TOKEN_OPTIMIZATION.md`

Report task outcome, actual tests, blockers, and required manual QA concisely.