# P02-CI-CODE-010C3B3A2 — remaining naming triage

Date: 2026-11-10. `node docs/evidence/P02-CI-CODE-010C3B3A2/build-ledger.cjs` reproduced 15 exact pinned shipped-file naming findings outside `includes/helpers.php`, verified each named symbol against its source line, and retained path/line/column/code/message/severity in `ledger.json`.

Fourteen short `cb_` or bootstrap/CLI global names are assigned to `P04-WP-PREFIX-CODE-058` for compatibility-aware migration. The CLI utility has a non-CLI refusal guard, but that does not make its global names compliant. The bootstrap `$core_path` variable remains file-scope and can be isolated during that migration. One `the_content` hook finding is context-justified: this is the WordPress core content filter applied to course content, so changing its name would remove normal formatting and other registered filters. No product code, public hook name, or scanner baseline was changed here.
