# P04-PRODUCT-ADR-038 — evidence

- Date: 2026-11-22 (Asia/Tehran)
- Repository/cwd: `D:/Android/AndroidStudioProjects/kmp-shop`
- Baseline: `230d7857`, branch `develop`; pre-edit working tree had user changes only in `docs/.obsidian/appearance.json` and `docs/.obsidian/workspace.json`, preserved.
- Scope: documentation contract only. No UI, HTTP, database, ZIP or migration execution; manual product QA N/A.

## Characterization and result

The prior appendix of ADR-006 assigned business data solely to Bridge, allowed Theme only ordinary pages, made Plugin headless, and selected co-install kernel by first load or highest version. These contradicted the active product specification and ADR-006 body. Live source still registers `cb_course`, `cb_therapist`, `cb_appointment`, and `cb_psychtest` in both `wordpress/carmilla-theme/inc/post-types.php` and `wordpress/carmilla-bridge/includes/class-cb-cpt.php`; this is current implementation drift, not proof of the target contract. Bridge's manifest controller currently writes an option; that is not independent purchase entitlement proof.

The new ADR-038 names one site-owned record set and one canonical write authority per entity group, preserves published identifiers, defines Theme-only/Plugin-only/both and disabled-host behavior, and assigns unresolved version, SKU, migration and runtime verification to queued cards. ADR-006 links to it and removes conflicting appendix text. WooCommerce remains canonical for commerce; private consultation, case and psych data are separate; WordPress build requests use an external runner. No released identifier or runtime API changed.

## Checks

| Command or review | Result |
|---|---|
| `git status --short` before edit | Exit 0; only two `.obsidian` user files modified. |
| `node .gitnexus/run.cjs status` | Exit 0; index matched commit and covered files. |
| `node .gitnexus/run.cjs query 'booking' --repo .` | Exit 0, empty due missing FTS index; followed by narrow source search. No symbol edited. |
| `rg -n 'register_post_type\|register_rest_route\|update_option'` over named WordPress files | Exit 0; confirmed dual CPT registration and Bridge manifest option. |
| Matrix review against active spec, ADR-006, task and Master | PASS for documented ownership, host modes, builder paths, rejected unlicensed/disabled operations and follow-up owners. This is document QA, not runtime QA. |
| `git diff --check` | Exit 0; line-ending warnings only. |
| `node .gitnexus/run.cjs detect-changes --scope all --repo .` | Exit 0; 7 tracked files, 16 symbols, zero affected processes, low risk. New ADR file reviewed separately. |

## Limits and rollback

The matrix is a target architecture. Source extraction, schema adoption, license enforcement, independent ZIP installation and HTTP/UI acceptance are pending in their own cards. Revert only this task's document changes if needed; do not alter stored site data. No secrets or customer data were used.
