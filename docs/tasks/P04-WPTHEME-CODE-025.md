# P04-WPTHEME-CODE-025 â€” Ø±ÙØ¹ Template Hierarchy Ø¨Ø±Ú¯Ù‡ Ùˆ Ø³Ø§Ø²Ú¯Ø§Ø±ÛŒ Elementor Page Layouts

## Prompt Ø§Ø¬Ø±Ø§ÛŒ Ù‡Ù…ÛŒÙ† Task

```text
Ù†Ù‚Ø´ ØªÙˆ Implementer Ùˆ Verifier ÙÙ‚Ø· Ù‡Ù…ÛŒÙ† Task Ø§Ø³Øª.
Repository: D:\Android\AndroidStudioProjects\kmp-shop
Master checklist: D:\Android\AndroidStudioProjects\kmp-shop\docs\MASTER_IMPLEMENTATION_CHECKLIST_FA.md
Source audit: D:\Android\AndroidStudioProjects\kmp-shop\docs\PROJECT_AUDIT_AND_PUBLICATION_PLAN_FA.md
Task ID: P04-WPTHEME-CODE-025

Ù‚Ø¨Ù„ Ø§Ø² ØªØºÛŒÛŒØ±:
1. AGENTS.md Ùˆ Ø¯Ø³ØªÙˆØ±Ù‡Ø§ÛŒ Ù…ÙˆØ¬ÙˆØ¯ Ø±Ø§ Ø¨Ø®ÙˆØ§Ù†.
2. TaskØŒdependencyØŒscopeØŒacceptance Ùˆ source reference Ø±Ø§ Ú©Ø§Ù…Ù„ Ø¨Ø®ÙˆØ§Ù†.
3. git status Ø±Ø§ Ø¨Ø±Ø±Ø³ÛŒ Ùˆ ØªØºÛŒÛŒØ±Ø§Øª Ú©Ø§Ø±Ø¨Ø± Ø±Ø§ Ø­ÙØ¸ Ú©Ù†.
4. baseline Ù…Ø´Ø®Øµâ€ŒØ´Ø¯Ù‡ Ø±Ø§ Ø§Ø¬Ø±Ø§ Ú©Ù†.
5. Ø§Ú¯Ø± Task Ø¨Ø²Ø±Ú¯â€ŒØªØ± Ø§Ø² M ÛŒØ§ Ù…Ø¨Ù‡Ù… Ø§Ø³Øª Ø§Ø¬Ø±Ø§ Ù†Ú©Ù† Ùˆ child Task Ù¾ÛŒØ´Ù†Ù‡Ø§Ø¯ Ø¨Ø¯Ù‡.

Ù‚ÙˆØ§Ø¹Ø¯:
- ÙÙ‚Ø· Ù‡Ù…ÛŒÙ† TaskØ› Ú©Ù…ØªØ±ÛŒÙ† diffØ› ÙÙ‚Ø· Allowed scope.
- dependency upgradeØŒrefactor Ø¬Ø§Ù†Ø¨ÛŒ ÛŒØ§ ØªØºÛŒÛŒØ± API contract Ù…Ù…Ù†ÙˆØ¹.
- secret/PII/PHI Ùˆ Ø¹Ù…Ù„ÛŒØ§Øª production/payment ÙˆØ§Ù‚Ø¹ÛŒ Ù…Ù…Ù†ÙˆØ¹.
- Ø§Ø¨ØªØ¯Ø§ reproduction Ùˆ characterization testØ› Ø³Ù¾Ø³ fix.
- Ù‡Ù…Ù‡ verification commandÙ‡Ø§ ÙˆØ§Ù‚Ø¹Ø§Ù‹ Ø§Ø¬Ø±Ø§ Ø´ÙˆÙ†Ø¯.
- ØªØ³Øª Ø¯Ø³ØªÛŒ Ø§Ø¬Ø±Ø§â€ŒÙ†Ø´Ø¯Ù‡ ØªÛŒÚ© Ù†Ø®ÙˆØ±Ø¯ Ùˆ Status Ø¨Ø±Ø§Ø¨Ø± AWAITING_MANUAL_QA Ø¨Ø§Ø´Ø¯.
- Ø¨Ø¯ÙˆÙ† EvidenceØŒTask Ø±Ø§ DONE Ù†Ú©Ù†Ø› ÙÙ‚Ø· status/evidence Ù‡Ù…ÛŒÙ† Task Ø±Ø§ ØªØºÛŒÛŒØ± Ø¨Ø¯Ù‡Ø› Ø¨Ù‡ Task Ø¨Ø¹Ø¯ÛŒ Ù†Ø±Ùˆ.

Ø´Ø±Ø§ÛŒØ· ØªÙˆÙ‚Ù: ØªØ¯Ø§Ø®Ù„ ØªØºÛŒÛŒØ± Ú©Ø§Ø±Ø¨Ø±ØŒÙ†Ø¨ÙˆØ¯ environment/contractØŒbaseline failure Ù…Ø±ØªØ¨Ø·ØŒØ¹Ù…Ù„ÛŒØ§Øª Ù…Ø®Ø±Ø¨ ÛŒØ§ ØªØºÛŒÛŒØ± Ø®Ø§Ø±Ø¬ Scope.
Ù¾Ø§Ø³Ø® Ù†Ù‡Ø§ÛŒÛŒ: OutcomeØŒChanged filesØŒAutomated testsØŒManual QAØŒAcceptanceØŒEvidenceØŒChecklist changeØŒRisks Ùˆ Rollback.
```

- Status: AWAITING_MANUAL_QA
- Phase/Area/Type: P04 / WPTHEME / CODE
- Priority/Risk/Size: P0 / MEDIUM / M
- Owner: AI
- Completion authority: BOTHØ› QA Ø¯Ø³ØªÛŒ Ø§Ù„Ø²Ø§Ù…ÛŒ
- Depends on: P04-WPTHEME-CODE-017
- Blocks: P04-WPPLUGIN-CODE-018
- Requirement source: Exploratory Baseline P00-QA-MANUAL-018ØŒ Master row P04-WPTHEME-CODE-025 Ùˆ `plans/003-elementor-page-layout-compatibility.md`

## Ù‡Ø¯Ù Ù‚Ø§Ø¨Ù„ Ø§Ù†Ø¯Ø§Ø²Ù‡â€ŒÚ¯ÛŒØ±ÛŒ

Ø¨Ø±Ú¯Ù‡â€ŒÙ‡Ø§ÛŒ Ø¹Ø§Ø¯ÛŒ Ùˆ Elementor Ø¯Ø± layoutÙ‡Ø§ÛŒ DefaultØŒElementor CanvasØŒElementor Full WidthØŒCarmilla Full Width Ùˆ Carmilla Blank Ù…Ø­ØªÙˆØ§ÛŒ ØµØ­ÛŒØ­ Ùˆ semantics Ù…ÙˆØ±Ø¯ Ø§Ù†ØªØ¸Ø§Ø± header/footer Ø±Ø§ Ù†Ù…Ø§ÛŒØ´ Ø¯Ù‡Ù†Ø¯.

## Ø®Ø±ÙˆØ¬ÛŒ Ù…ÙˆØ±Ø¯ Ø§Ù†ØªØ¸Ø§Ø±

root cause Ø¨Ø§ template trace Ø§Ø«Ø¨Ø§ØªØŒÚ©ÙˆÚ†Ú©â€ŒØªØ±ÛŒÙ† fix Ø¯Ø± Theme Ø§Ø¹Ù…Ø§Ù„ØŒÙ…Ø§ØªØ±ÛŒØ³ layout Ø«Ø¨Øª Ùˆ rendering Ø¨Ø§ Bridge Ø®Ø§Ù…ÙˆØ´/Ø±ÙˆØ´Ù† ÛŒÚ©Ø³Ø§Ù† Ø¨Ø§Ø´Ø¯.

## Ø®Ø§Ø±Ø¬ Ø§Ø² Ù…Ø­Ø¯ÙˆØ¯Ù‡

- ØªØºÛŒÛŒØ± Bridge REST/data contract ÛŒØ§ Ø§Ù„Ø²Ø§Ù… Bridge Ø¨Ø±Ø§ÛŒ render.
- override Elementor core/ProØŒØ§ÙØ²ÙˆØ¯Ù† Pro Ø¨Ø¯ÙˆÙ† Ù…Ø¬ÙˆØ² ÛŒØ§ refactor header/footer Ù†Ø§Ù…Ø±ØªØ¨Ø·.
- ØªØ³Øª Ù‚Ø§Ù„Ø¨ Ø«Ø§Ù„Ø«Ø› Ø¢Ù† Ù…ÙˆØ±Ø¯ Ù…ØªØ¹Ù„Ù‚ Ø¨Ù‡ Bridge Standalone QA Ø§Ø³Øª.

## Preconditions

- Task Ø¨Ø§ÛŒØ¯ READY Ùˆ dependency ÙˆØ§Ù‚Ø¹Ø§Ù‹ DONE Ø¨Ø§Ø´Ø¯.
- WordPress ØªÙ…ÛŒØ²ØŒCarmilla Theme Ùˆ Elementor Free Ø¯Ø± Ø¯Ø³ØªØ±Ø³ Ø¨Ø§Ø´Ù†Ø¯.
- Ù‚Ø¨Ù„ Ø§Ø² fix Ø¨Ø±Ø±Ø³ÛŒ Ø´ÙˆØ¯ Ù†Ø¨ÙˆØ¯ `page.php`/`singular.php` ÙˆØ§Ù‚Ø¹Ø§Ù‹ Ø¨Ø§Ø¹Ø« fallback Ø¨Ø±Ú¯Ù‡ Ø¨Ù‡ `index.php` Ù…ÛŒâ€ŒØ´ÙˆØ¯ ÛŒØ§ Ù†Ù‡.

## Allowed files/directories

- `wordpress/carmilla-theme/page.php`
- `wordpress/carmilla-theme/singular.php`
- `wordpress/carmilla-theme/page-templates/**`
- `wordpress/carmilla-theme/inc/elementor.php`
- CSS Ù…Ø­Ø¯ÙˆØ¯ Ù…Ø±ØªØ¨Ø· Ø¨Ø§ wrapper/content
- `wordpress/**/tests/**`
- `tools/test-env/**`
- `docs/evidence/P04-WPTHEME-CODE-025/**`
- status/checkbox Ù‡Ù…ÛŒÙ† Task Ø¯Ø± `docs/**`

## Forbidden actions

- ØªØºÛŒÛŒØ± `wordpress/carmilla-bridge/**` Ø¬Ø² fixture/test ØµØ±ÛŒØ­ Ø§ÛŒÙ† Task.
- ØªØºÛŒÛŒØ± dependency/APIØŒØ¯Ø§Ø¯Ù‡ ÙˆØ§Ù‚Ø¹ÛŒØŒproduction ÛŒØ§ Ø­Ø°Ù templateÙ‡Ø§ÛŒ Ú©Ø§Ø±Ø¨Ø±.

## Ù…Ø±Ø§Ø­Ù„ Ù¾ÛŒØ§Ø¯Ù‡â€ŒØ³Ø§Ø²ÛŒ

1. Ø¨Ø±Ú¯Ù‡ synthetic Ø¨Ø§ heading/text/image/button Ø¨Ø³Ø§Ø² Ùˆ Ù¾Ù†Ø¬ layout Ø±Ø§ Ù‚Ø¨Ù„ Ø§Ø² ØªØºÛŒÛŒØ± Ø«Ø¨Øª Ú©Ù†.
2. template Ø§Ù†ØªØ®Ø§Ø¨â€ŒØ´Ø¯Ù‡ Ùˆ Ø§Ø¬Ø±Ø§ÛŒ `the_content()` Ø±Ø§ trace Ú©Ù†Ø› ÙØ±Ø¶ fallback Ø¨Ù‡ `index.php` Ø±Ø§ ØªØ£ÛŒÛŒØ¯ ÛŒØ§ Ø±Ø¯ Ú©Ù†.
3. characterization test Ø´Ú©Ø³Øªâ€ŒØ®ÙˆØ±Ø¯Ù‡ Ø¨Ø±Ø§ÛŒ layout Ù…Ø¹ÛŒÙˆØ¨ Ø§Ø¶Ø§ÙÙ‡ Ú©Ù†.
4. Ø¯Ø± ØµÙˆØ±Øª Ø§Ø«Ø¨Ø§ØªØŒÚ©ÙˆÚ†Ú©â€ŒØªØ±ÛŒÙ† `page.php`/`singular.php` Ø§Ø³ØªØ§Ù†Ø¯Ø§Ø±Ø¯ ÛŒØ§ Ø§ØµÙ„Ø§Ø­ template Ø±Ø§ Ø§Ø¹Ù…Ø§Ù„ Ú©Ù†.
5. `wp_head`ØŒ`wp_body_open`ØŒ`body_class`ØŒloopØŒ`the_content` Ùˆ `wp_footer` Ø±Ø§ Ø¨Ø±Ø±Ø³ÛŒ Ú©Ù†.
6. regression Ø¨Ø±Ú¯Ù‡ Ø¹Ø§Ø¯ÛŒØŒpostØŒarchive Ùˆ 404 Ø±Ø§ Ø§Ø¬Ø±Ø§ Ú©Ù†.
7. matrix Ø±Ø§ Ø¨Ø¯ÙˆÙ† Bridge Ùˆ Ø¨Ø§ Bridge ØªÚ©Ø±Ø§Ø± Ùˆ Evidence Ø±Ø§ Ø«Ø¨Øª Ú©Ù†.

## Automated tests Ø¨Ø§ command Ùˆ expected result

```powershell
$repo = (Get-Location).Path
docker run --rm -v "${repo}:/app" -w /app php:8.1-cli sh -lc "find wordpress/carmilla-theme -name '*.php' -print0 | xargs -0 -n1 php -l"
docker compose -f tools/test-env/docker-compose.yml config
git diff --check
```

- Expected: syntax Ùˆ compose config Ø¨Ø¯ÙˆÙ† Ø®Ø·Ø§Ø› integration test Ø¬Ø¯ÛŒØ¯ ÙˆØ¬ÙˆØ¯ `the_content()` Ùˆ wrapper ØµØ­ÛŒØ­ layoutÙ‡Ø§ Ø±Ø§ Ø§Ø«Ø¨Ø§Øª Ú©Ù†Ø¯.

## Manual tests Ø¨Ø§ environment/data/steps/expected

- Environment: WordPress ØªÙ…ÛŒØ² + Carmilla + Elementor FreeØ› ÛŒÚ© Ø¯ÙˆØ± Bridge Ø®Ø§Ù…ÙˆØ´ Ùˆ ÛŒÚ© Ø¯ÙˆØ± Ø±ÙˆØ´Ù†.
- Steps: ØµÙØ­Ù‡ Elementor QA Ø±Ø§ Ø¯Ø± Ù¾Ù†Ø¬ layout Ùˆ viewportÙ‡Ø§ÛŒ 360px/tablet/desktop Ø¨Ø§Ø² Ú©Ù†Ø› Canvas Ø¨Ø¯ÙˆÙ† header/footer Ùˆ Full Width Ø¨Ø§ header/footer Ùˆ Ø¹Ø±Ø¶ ØµØ­ÛŒØ­ Ø¨Ø§Ø´Ø¯.
- Regression: ÛŒÚ© Ø¨Ø±Ú¯Ù‡ Ù…Ø¹Ù…ÙˆÙ„ÛŒØŒpost Ùˆ archive Ø¨Ø§Ø² Ø´ÙˆÙ†Ø¯.
- Expected: Ù…Ø­ØªÙˆØ§ Ú©Ø§Ù…Ù„ØŒheader/footer ØªÚ©Ø±Ø§Ø±ÛŒ ØµÙØ±ØŒPHP/JS error ØµÙØ± Ùˆ Ù†ØªÛŒØ¬Ù‡ Ø¨Ø§ Ø±ÙˆØ´Ù†â€ŒØ´Ø¯Ù† Bridge ØªØºÛŒÛŒØ± Ù†Ú©Ù†Ø¯.
- ØªØ§ ØªØ£ÛŒÛŒØ¯ Ø§Ù†Ø³Ø§Ù† Status Ø¨Ø±Ø§Ø¨Ø± AWAITING_MANUAL_QA Ø§Ø³Øª.

## Acceptance Criteria

- [ ] root cause Ùˆ template trace Ø«Ø¨Øª Ø´Ø¯Ù‡ Ø§Ø³Øª.
- [ ] Ù¾Ù†Ø¬ layout Ù†ØªÛŒØ¬Ù‡ PASS ÛŒØ§ unsupported Ù…Ø³ØªÙ†Ø¯ Ø¯Ø§Ø±Ù†Ø¯.
- [ ] Ø¨Ø±Ú¯Ù‡ Ù…Ø¹Ù…ÙˆÙ„ÛŒØŒpostØŒarchive Ùˆ 404 regression Ù†Ø¯Ø§Ø±Ù†Ø¯.
- [ ] Bridge Ø¨Ø± rendering Ø§Ø«Ø± Ù†Ø¯Ø§Ø±Ø¯.
- [ ] QA Ø¯Ø³ØªÛŒ Ùˆ Evidence ØªØ£ÛŒÛŒØ¯ Ø´Ø¯Ù‡ Ø§Ø³Øª.

## Security/Privacy/Migration checks

- Ø¯Ø§Ø¯Ù‡ QA syntheticØ› screenshot/log Ø¨Ø¯ÙˆÙ† PII Ùˆ secret.
- migration Ùˆ ØªØºÛŒÛŒØ± API Ø¯Ø± Ø§ÛŒÙ† Task ÙˆØ¬ÙˆØ¯ Ù†Ø¯Ø§Ø±Ø¯.

## Evidence

- `docs/evidence/P04-WPTHEME-CODE-025/`: before/afterØŒversion matrixØŒtemplate traceØŒcommand output Ùˆ Manual QA record.

## Rollback

ÙÙ‚Ø· template/CSS/integration Ù‡Ù…ÛŒÙ† Task revert Ø´ÙˆØ¯Ø› Ø¨Ø±Ú¯Ù‡ Ùˆ Ø¯Ø§Ø¯Ù‡ Elementor Ø­Ø°Ù Ù†Ø´ÙˆØ¯Ø› syntax Ùˆ smoke Ø¨Ø±Ú¯Ù‡ Ø¹Ø§Ø¯ÛŒ Ø¯ÙˆØ¨Ø§Ø±Ù‡ Ø§Ø¬Ø±Ø§ Ø´ÙˆØ¯.

## Completion record

- Started at: 2026-11-28T14:21:16+03:30
- Completed at: 2026-11-28T14:22:30+03:30
- Changed files:
- Commands and exit codes:
- Manual tester/date/result: AWAITING_MANUAL_QA (Elementor Free Canvas/Full Width)
- Evidence paths: docs/evidence/P04-WPTHEME-CODE-025/summary.md
- Remaining risks/blockers:
- Final status: AWAITING_MANUAL_QA
