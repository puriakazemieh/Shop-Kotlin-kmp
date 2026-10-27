# Carmilla — AI agent instructions

You are an implementer **and** verifier for the KMP client, WordPress products, and related Spring Boot server.

## Source of truth / task safety
- For queued product work, inspect `docs/tasks.md` and select **only the first READY task**. Read its `docs/tasks/<TASK-ID>.md` **in full** and the active `docs/INDEPENDENT_PRODUCTS_SPEC_FA.md` contract before implementation. Use relevant sections of `docs/MASTER_IMPLEMENTATION_CHECKLIST_FA.md` for acceptance; do not preload the entire master checklist.
- For an explicitly requested tooling/agent-maintenance change outside the product queue, keep it isolated; do not change task statuses or claim a phase gate passed.
- Check `git status --short` before editing. Preserve user-owned changes; never reset or delete them. Smallest scoped diff only; no unrelated upgrades.
- Follow `RTK.md` for applicable architecture, WordPress, financial, and security rules. Never expose secrets, customer data, or PII.
- Each queued task needs evidence in `docs/evidence/<TASK-ID>/` before DONE. If UI, network, or migration behavior changes, provide manual QA location, steps and success criteria; keep `AWAITING_MANUAL_QA` until the user confirms.

## Token-efficient navigation
- Start from the task's paths or a narrowly scoped `rg --files` / `rg -n` search; read matching symbols/regions rather than entire directories. Avoid bulk-reading `docs/evidence/**`, generated/build artifacts, and unrelated task cards.
- If GitNexus is connected **and its index is current**, use graph `query` / `context` / `impact` for nontrivial dependency tracing; verify edits against actual source. If unavailable, use normal repository search. Do not add graph overhead for an obvious single-file fix.
- Keep tool output bounded, avoid duplicate scans, and run focused checks first; run broader required gates when appropriate. Do not bypass verification to save tokens.

## Useful commands
- Git state: `git status --short; git log -1 --oneline`
- KMP checks (Windows): `./gradlew.bat :composeApp:compileKotlinJs :composeApp:compileKotlinJvm`
- WordPress/PHP Docker procedure: `RTK.md`
- Token baseline and GitNexus setup: `docs/engineering/CODEX_TOKEN_OPTIMIZATION.md`

Report completed work and actual test evidence concisely. If an item is blocked or beyond size M, explain the blocker or split into smaller tasks.
