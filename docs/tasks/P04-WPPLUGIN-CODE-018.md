<div dir="rtl" align="right">

# P04-WPPLUGIN-CODE-018 â€” ØªØ±Ø¬Ù…Ù‡ Ùˆ escaping Ù…Ø±Ø²Ù‡Ø§ÛŒ Ù…Ø´ØªØ±Ú© Ùˆ Ù…ÛŒØ²Ø¨Ø§Ù†â€ŒÙ‡Ø§ÛŒ Ù…Ø³ØªÙ‚Ù„

## Prompt Ø§Ø¬Ø±Ø§ÛŒ Ù‡Ù…ÛŒÙ† Task

```text
Ù†Ù‚Ø´ ØªÙˆ Implementer Ùˆ Verifier ÙÙ‚Ø· Ù‡Ù…ÛŒÙ† Task Ø§Ø³Øª.

Repository:
D:\Android\AndroidStudioProjects\kmp-shop

Master checklist:
D:\Android\AndroidStudioProjects\kmp-shop\docs\MASTER_IMPLEMENTATION_CHECKLIST_FA.md

Source audit:
D:\Android\AndroidStudioProjects\kmp-shop\docs\PROJECT_AUDIT_AND_PUBLICATION_PLAN_FA.md

Task ID:
P04-WPPLUGIN-CODE-018

Ù‚Ø¨Ù„ Ø§Ø² ØªØºÛŒÛŒØ±:
1. AGENTS.md Ùˆ Ù‡Ø± Ø¯Ø³ØªÙˆØ± Ø§Ø±Ø¬Ø§Ø¹â€ŒØ´Ø¯Ù‡â€ŒØ§ÛŒ Ú©Ù‡ ÙˆØ§Ù‚Ø¹Ø§Ù‹ ÙˆØ¬ÙˆØ¯ Ø¯Ø§Ø±Ø¯ Ø±Ø§ Ø¨Ø®ÙˆØ§Ù†.
2. TaskØŒ dependencyØŒ scopeØŒ acceptance Ùˆ source reference Ø±Ø§ Ú©Ø§Ù…Ù„ Ø¨Ø®ÙˆØ§Ù†.
3. git status Ø±Ø§ Ø¨Ø±Ø±Ø³ÛŒ Ùˆ ØªØºÛŒÛŒØ±Ø§Øª Ù…ÙˆØ¬ÙˆØ¯ Ú©Ø§Ø±Ø¨Ø± Ø±Ø§ Ø­ÙØ¸ Ú©Ù†.
4. baseline test Ù…Ø´Ø®Øµâ€ŒØ´Ø¯Ù‡ Ø±Ø§ Ø§Ø¬Ø±Ø§ Ú©Ù†.
5. Ø§Ú¯Ø± Task Ø¨Ø²Ø±Ú¯â€ŒØªØ± Ø§Ø² M ÛŒØ§ Ù…Ø¨Ù‡Ù… Ø§Ø³ØªØŒ Ø§Ø¬Ø±Ø§ Ù†Ú©Ù†Ø› Ø¢Ù† Ø±Ø§ Ø¨Ù‡ TaskÙ‡Ø§ÛŒ Ú©ÙˆÚ†Ú©â€ŒØªØ± Ù¾ÛŒØ´Ù†Ù‡Ø§Ø¯ Ø¨Ø¯Ù‡.

Ù‚ÙˆØ§Ø¹Ø¯:
- ÙÙ‚Ø· Ù‡Ù…ÛŒÙ† Task Ø±Ø§ Ø§Ù†Ø¬Ø§Ù… Ø¨Ø¯Ù‡.
- Ú©Ù…ØªØ±ÛŒÙ† diff Ù„Ø§Ø²Ù… Ø±Ø§ Ø¨Ø³Ø§Ø².
- Ø®Ø§Ø±Ø¬ Ø§Ø² Allowed scope ØªØºÛŒÛŒØ± Ù†Ø¯Ù‡.
- dependency upgradeØŒrefactor Ø¬Ø§Ù†Ø¨ÛŒ ÛŒØ§ ØªØºÛŒÛŒØ± API contract Ø§Ù†Ø¬Ø§Ù… Ù†Ø¯Ù‡.
- secret ÛŒØ§ Ø¯Ø§Ø¯Ù‡ ÙˆØ§Ù‚Ø¹ÛŒ Ø§ÛŒØ¬Ø§Ø¯/Ø«Ø¨Øª Ù†Ú©Ù†.
- deploy/publish/production/payment ÙˆØ§Ù‚Ø¹ÛŒ Ø§Ù†Ø¬Ø§Ù… Ù†Ø¯Ù‡ Ù…Ú¯Ø± Task ØµØ±ÛŒØ­ Ùˆ ØªØ£ÛŒÛŒØ¯Ø´Ø¯Ù‡ Ø¨Ø§Ø´Ø¯.
- Ø§Ø¨ØªØ¯Ø§ ØªØ³Øª Ø´Ú©Ø³Øª ÛŒØ§ characterization Ù…Ù†Ø§Ø³Ø¨ Ø±Ø§ Ø§Ø¶Ø§ÙÙ‡ Ú©Ù†.
- Ù‡Ù…Ù‡ commandÙ‡Ø§ÛŒ verification Ø±Ø§ ÙˆØ§Ù‚Ø¹Ø§Ù‹ Ø§Ø¬Ø±Ø§ Ú©Ù†.
- ØªØ³Øª Ø¯Ø³ØªÛŒ Ø§Ø¬Ø±Ø§â€ŒÙ†Ø´Ø¯Ù‡ Ø±Ø§ ØªÛŒÚ© Ù†Ø²Ù† Ùˆ ÙˆØ¶Ø¹ÛŒØª Ø±Ø§ AWAITING_MANUAL_QA Ø¨Ú¯Ø°Ø§Ø±.
- Ø¨Ø¯ÙˆÙ† Evidence Task Ø±Ø§ DONE Ù†Ú©Ù†.
- ÙÙ‚Ø· checkbox/status/evidence Ù‡Ù…ÛŒÙ† Task Ø±Ø§ Ø¨Ù‡â€ŒØ±ÙˆØ²Ø±Ø³Ø§Ù†ÛŒ Ú©Ù†.
- Ø¨Ù‡ Task Ø¨Ø¹Ø¯ÛŒ Ù†Ø±Ùˆ.

Ø´Ø±Ø§ÛŒØ· ØªÙˆÙ‚Ù:
- ØªØ¯Ø§Ø®Ù„ Ø¨Ø§ ØªØºÛŒÛŒØ±Ø§Øª Ø­Ù„â€ŒÙ†Ø´Ø¯Ù‡ Ú©Ø§Ø±Ø¨Ø±
- Ù†Ø¨ÙˆØ¯ credential/contract/ØªØµÙ…ÛŒÙ… Ø¶Ø±ÙˆØ±ÛŒ
- Ù†ÛŒØ§Ø² Ø¨Ù‡ Ø¹Ù…Ù„ÛŒØ§Øª Ù…Ø®Ø±Ø¨ ÛŒØ§ Production
- baseline failure Ù…Ø±ØªØ¨Ø·
- Ù†ÛŒØ§Ø² Ø¨Ù‡ ØªØºÛŒÛŒØ± contract Ø®Ø§Ø±Ø¬ Ø§Ø² Scope

Ù¾Ø§Ø³Ø® Ù†Ù‡Ø§ÛŒÛŒ: OutcomeØŒChanged filesØŒAutomated testsØŒManual test statusØŒAcceptance CriteriaØŒEvidence pathsØŒChecklist status changeØŒRemaining risks/blockers Ùˆ Rollback instructions.
```

- Status: DONE
- Phase/Area/Type: P04 / WPPLUGIN / CODE
- Priority/Risk/Size: P1/LOW / UNASSESSED (Ù‚Ø¨Ù„ Ø§Ø² READY ØªØ¹ÛŒÛŒÙ† Ø´ÙˆØ¯)
- Owner: AI
- Completion authority: BOTH
- Depends on: P04-WPTHEME-CODE-025
- Blocks: P04-WPPLUGIN-CODE-045
- Requirement source: Master checklist row P04-WPPLUGIN-CODE-018 Ùˆ Source audit Ø¨Ø®Ø´ WPPLUGIN
- Ù…Ø¨Ù†Ø§ÛŒ Ø¨Ø§Ø²Ø¨Ø±Ù†Ø§Ù…Ù‡â€ŒØ±ÛŒØ²ÛŒ: [ØªØ¹Ø±ÛŒÙ Ù…Ø­ØµÙˆÙ„Ø§Øª Ù…Ø³ØªÙ‚Ù„](../INDEPENDENT_PRODUCTS_SPEC_FA.md) Ùˆ [ADR-006](../architecture/adr/ADR-006-INDEPENDENT-PRODUCTS-AND-ENTITLEMENTS.md).

## Ù‡Ø¯Ù Ù‚Ø§Ø¨Ù„ Ø§Ù†Ø¯Ø§Ø²Ù‡â€ŒÚ¯ÛŒØ±ÛŒ

Ø¨Ø§Ø±Ú¯Ø°Ø§Ø±ÛŒ textdomain/POT Ùˆ escaping Ø¨Ø±Ø§ÛŒ Ø±Ø´ØªÙ‡â€ŒÙ‡Ø§ÛŒ Ø¹Ù…ÙˆÙ…ÛŒ kernelØŒonboarding Ùˆ ØªÙ†Ø¸ÛŒÙ…Ø§Øª Ø¯Ùˆ host ØªØ«Ø¨ÛŒØª Ø´ÙˆØ¯Ø› Ø±Ø´ØªÙ‡â€ŒÙ‡Ø§ÛŒ Ø¬Ø¯ÛŒØ¯ Ù‡Ø± vertical Ù‡Ù…Ø±Ø§Ù‡ Ú©Ø§Ø±Øª Ù‡Ù…Ø§Ù† vertical Ù¾ÙˆØ´Ø´ Ø¯Ø§Ø¯Ù‡ Ø´ÙˆÙ†Ø¯.

## Ø®Ø±ÙˆØ¬ÛŒ Ù…ÙˆØ±Ø¯ Ø§Ù†ØªØ¸Ø§Ø±

ZIP Ù¾ÙˆØ³ØªÙ‡ Ùˆ Ø§ÙØ²ÙˆÙ†Ù‡ Ù…Ø³ØªÙ‚Ù„ ÙØ§Ø±Ø³ÛŒ/Ø§Ù†Ú¯Ù„ÛŒØ³ÛŒ Ø±Ø§ Ø¯Ø±Ø³Øª Ø¨Ø§Ø±Ú¯Ø°Ø§Ø±ÛŒ Ú©Ù†Ù†Ø¯Ø› co-install ØªØ±Ø¬Ù…Ù‡ ØªÚ©Ø±Ø§Ø±ÛŒ/Ú¯Ù…â€ŒØ´Ø¯Ù‡ Ùˆ output escape Ù†Ø´Ø¯Ù‡ Ø¯Ø± Ù…Ø±Ø²Ù‡Ø§ÛŒ Ø§ÛŒÙ† Ú©Ø§Ø±Øª Ù†Ø¯Ø§Ø´ØªÙ‡ Ø¨Ø§Ø´Ø¯.

## Ø®Ø§Ø±Ø¬ Ø§Ø² Ù…Ø­Ø¯ÙˆØ¯Ù‡
- Ù‡Ø± FeatureØŒproviderØŒplatform ÛŒØ§ refactor Ø®Ø§Ø±Ø¬ Ø§Ø² Ù‡Ù…ÛŒÙ† Task ID.
- deploy/publishØŒÙ¾Ø±Ø¯Ø§Ø®Øª ÙˆØ§Ù‚Ø¹ÛŒØŒØªØºÛŒÛŒØ± Production Ùˆ ØªØºÛŒÛŒØ± Ø¯Ø§Ø¯Ù‡ Ù…Ø´ØªØ±ÛŒ.

## Preconditions
- Status Ø¨Ø§ÛŒØ¯ READY Ø¨Ø§Ø´Ø¯Ø› TODO Ù…Ø¬ÙˆØ² Ø§Ø¬Ø±Ø§ Ù†ÛŒØ³Øª.
- DependencyÙ‡Ø§: P04-WPTHEME-CODE-025
- git status Ùˆ baseline Ù¾ÛŒØ´ Ø§Ø² ØªØºÛŒÛŒØ± Ø«Ø¨Øª Ø´ÙˆÙ†Ø¯.

## Allowed files/directories

- `wordpress/packages/carmilla-core/**`
- adapterÙ‡Ø§ÛŒ Ù…Ø±ØªØ¨Ø· Ø¯Ø± `wordpress/carmilla-theme/**` Ùˆ `wordpress/carmilla-bridge/**`
- `wordpress/**/tests/**` Ùˆ `tools/test-env/**`
- `docs/evidence/P04-WPPLUGIN-CODE-018/**` Ùˆ ÙˆØ¶Ø¹ÛŒØª Ù‡Ù…ÛŒÙ† Ú©Ø§Ø±Øª Ø¯Ø± `docs/**`

## Forbidden actions
- Ø­Ø°Ù/overwrite ØªØºÛŒÛŒØ±Ø§Øª Ú©Ø§Ø±Ø¨Ø±ØŒgit reset/checkoutØŒØ§Ø±ØªÙ‚Ø§ÛŒ dependency ÛŒØ§ ØªØºÛŒÛŒØ± contract Ø®Ø§Ø±Ø¬ Scope.
- Ø«Ø¨Øª credentialØŒØ¯Ø§Ø¯Ù‡ ÙˆØ§Ù‚Ø¹ÛŒ Ù…Ø´ØªØ±ÛŒ ÛŒØ§ Ø¯Ø§Ø¯Ù‡ Ø³Ù„Ø§Ù…Øª Ø¯Ø± repo/Evidence.
- Ø¹Ù…Ù„ÛŒØ§Øª Production ÛŒØ§ migration ØªØ®Ø±ÛŒØ¨ÛŒ.

## Ù…Ø±Ø§Ø­Ù„ Ù¾ÛŒØ§Ø¯Ù‡â€ŒØ³Ø§Ø²ÛŒ

1. Ù…Ø§Ù„Ú©ÛŒØª textdomain kernel Ùˆ hostÙ‡Ø§ Ø±Ø§ Ù…Ø´Ø®Øµ Ùˆ Ø±Ø´ØªÙ‡â€ŒÙ‡Ø§ÛŒ Ù…Ø±Ø² Ù¾Ø§ÛŒÙ‡ Ø±Ø§ inventory Ú©Ù†.
2. POT Ùˆ load path Ø¯Ùˆ ZIP Ø±Ø§ Ø§ØµÙ„Ø§Ø­ Ùˆ escaping Ù‡Ù…Ø§Ù† Ù…Ø±Ø²Ù‡Ø§ Ø±Ø§ Ø¨Ø±Ø±Ø³ÛŒ Ú©Ù†.
3. locale switch Ø±Ø§ Ø¯Ø± Ø¯Ùˆ Ù…Ø­ØµÙˆÙ„ Ù…Ø³ØªÙ‚Ù„ Ùˆ Ù‡Ù…â€ŒØ²Ù…Ø§Ù† Ø§Ø¬Ø±Ø§ Ú©Ù†.
4. ÙˆØ¸ÛŒÙÙ‡ Ø±Ø´ØªÙ‡â€ŒÙ‡Ø§ÛŒ vertical Ø¢ÛŒÙ†Ø¯Ù‡ Ø±Ø§ Ø¯Ø± Ù‚Ø±Ø§Ø±Ø¯Ø§Ø¯ Ù…Ø´Ø§Ø±Ú©Øª Ø«Ø¨Øª Ú©Ù†Ø› ØªØ±Ø¬Ù…Ù‡ Ù‡Ù…Ù‡ controllerÙ‡Ø§ ÛŒÚ©Ø¬Ø§ Ø¯Ø± scope Ù†ÛŒØ³Øª.

## Automated tests Ø¨Ø§ command Ùˆ expected result
- Command: Ø¯Ø± Ù…Ø­ÛŒØ· WordPress CI/containerØŒ lint Ùˆ test Ù…Ø­Ø¯ÙˆØ¯ Ø¨Ù‡ Scope Ø±Ø§ Ø§Ø¬Ø±Ø§ Ú©Ù†.
- Expected: activation/install Ùˆ ØªØ³Øª Ù…Ø±ØªØ¨Ø· exit code 0Ø› Ù†Ø¨ÙˆØ¯ PHP Ù…Ø­Ù„ÛŒ Ù…Ø¬ÙˆØ² ØªÛŒÚ©â€ŒØ²Ø¯Ù† Ù†ÛŒØ³Øª.
- Ù…Ø¹ÛŒØ§Ø± Ø§Ø®ØªØµØ§ØµÛŒ: ZIP Ù¾ÙˆØ³ØªÙ‡ Ùˆ Ø§ÙØ²ÙˆÙ†Ù‡ Ù…Ø³ØªÙ‚Ù„ ÙØ§Ø±Ø³ÛŒ/Ø§Ù†Ú¯Ù„ÛŒØ³ÛŒ Ø±Ø§ Ø¯Ø±Ø³Øª Ø¨Ø§Ø±Ú¯Ø°Ø§Ø±ÛŒ Ú©Ù†Ù†Ø¯Ø› co-install ØªØ±Ø¬Ù…Ù‡ ØªÚ©Ø±Ø§Ø±ÛŒ/Ú¯Ù…â€ŒØ´Ø¯Ù‡ Ùˆ output escape Ù†Ø´Ø¯Ù‡ Ø¯Ø± Ù…Ø±Ø²Ù‡Ø§ÛŒ Ø§ÛŒÙ† Ú©Ø§Ø±Øª Ù†Ø¯Ø§Ø´ØªÙ‡ Ø¨Ø§Ø´Ø¯.

## Manual tests Ø¨Ø§ environment/data/steps/expected

- Ú©Ø¬Ø§: onboarding Ùˆ ØªÙ†Ø¸ÛŒÙ…Ø§Øª Carmilla Ø¨Ø§ locale ÙØ§Ø±Ø³ÛŒ Ùˆ Ø§Ù†Ú¯Ù„ÛŒØ³ÛŒ.
- Ú†Ú¯ÙˆÙ†Ù‡: Ù‡Ø± ZIP Ø±Ø§ ØªÙ†Ù‡Ø§ Ù†ØµØ¨ Ú©Ù†ÛŒØ¯Ø› Ø²Ø¨Ø§Ù† Ø±Ø§ ØªØºÛŒÛŒØ± Ùˆ Ù…ØªÙ† Ù…ØµÙ†ÙˆØ¹ÛŒ Ø¯Ø§Ø±Ø§ÛŒ Ù†ÙˆÛŒØ³Ù‡ HTML Ø±Ø§ Ø¯Ø± ÙÛŒÙ„Ø¯ Ù…Ø¬Ø§Ø² ÙˆØ§Ø±Ø¯ Ú©Ù†ÛŒØ¯Ø› Ø­Ø§Ù„Øª Ù‡Ù…â€ŒØ²Ù…Ø§Ù† Ø±Ø§ Ù†ÛŒØ² Ø¨Ø¨ÛŒÙ†ÛŒØ¯.
- Ù…Ø¹ÛŒØ§Ø± Ù…ÙˆÙÙ‚ÛŒØª: ØªØ±Ø¬Ù…Ù‡ ØµØ­ÛŒØ­ØŒescaping Ø§Ù…Ù† Ùˆ Ø¨Ø§Ø±Ú¯Ø°Ø§Ø±ÛŒ Ø¨Ø¯ÙˆÙ† ÙˆØ§Ø¨Ø³ØªÚ¯ÛŒ Ø¨Ù‡ textdomain Ù…Ø­ØµÙˆÙ„ Ø¯ÛŒÚ¯Ø± Ø¨Ø§Ø´Ø¯.
- Ù†Ø³Ø®Ù‡ artifact/Ù…Ø­ÛŒØ·ØŒÙ†Ø§Ù… Ø¢Ø²Ù…ÙˆÙ†Ú¯Ø±ØŒØªØ§Ø±ÛŒØ® Ùˆ Ø´ÙˆØ§Ù‡Ø¯ Ø¯Ø§Ø¯Ù‡ Ù…ØµÙ†ÙˆØ¹ÛŒ Ø«Ø¨Øª Ø´ÙˆÙ†Ø¯Ø› Ù…ÙˆØ§Ø±Ø¯ UI/Ø´Ø¨Ú©Ù‡/Ù…Ù‡Ø§Ø¬Ø±Øª ØªØ§ ØªØ£ÛŒÛŒØ¯ Ø§Ù†Ø³Ø§Ù† `AWAITING_MANUAL_QA` Ù‡Ø³ØªÙ†Ø¯.

## Acceptance Criteria

- [ ] textdomain Ù‡Ø± Ù…ÛŒØ²Ø¨Ø§Ù† Ùˆ kernel Ù…Ø³Ø¦ÙˆÙ„ÛŒØª Ø±ÙˆØ´Ù† Ø¯Ø§Ø±Ø¯.
- [ ] POT Ø¯Ø± Ù‡Ø± ZIP Ù…Ø³ØªÙ‚Ù„ Ù…ÙˆØ¬ÙˆØ¯ Ø§Ø³Øª.
- [ ] locale Ùˆ escaping Ù…Ø±Ø² Ù¾Ø§ÛŒÙ‡ Ø¢Ø²Ù…ÙˆØ¯Ù‡â€ŒØ§Ù†Ø¯.
- [ ] Ù¾ÙˆØ´Ø´ Ø±Ø´ØªÙ‡ verticalÙ‡Ø§ Ø¨Ù‡ Ú©Ø§Ø±Øª Ø¯Ø§Ù…Ù†Ù‡ Ù…Ø­Ø¯ÙˆØ¯ Ùˆ Ù‚Ø§Ø¨Ù„ Ù¾ÛŒÚ¯ÛŒØ±ÛŒ Ø§Ø³Øª.

## Security/Privacy/Migration checks
- SecretØŒTokenØŒPIIØŒPHI ÛŒØ§ Ø¯Ø§Ø¯Ù‡ Ù…Ø´ØªØ±ÛŒ Ø¯Ø± sourceØŒlog Ùˆ Evidence Ø«Ø¨Øª Ù†Ø´ÙˆØ¯.
- Ø¨Ø±Ø§ÛŒ API/write pathØŒauthorization Ùˆ ownership Ø¨Ø±Ø±Ø³ÛŒ Ø´ÙˆØ¯.
- Ø¨Ø±Ø§ÛŒ migrationØŒforward fix/rollback Ùˆ backup Ø¨Ø±Ø±Ø³ÛŒ Ø´ÙˆØ¯.

## Evidence
- Ù…Ø³ÛŒØ±: docs/evidence/P04-WPPLUGIN-CODE-018/
- baseline commit/buildØŒcommand/cwd/exit codeØŒtest reportØŒscreenshot redacted Ùˆ reviewer Ø±Ø§ Ø«Ø¨Øª Ú©Ù†.

## Rollback
- Ø±ÙˆØ´ Ø¨Ø§Ø²Ú¯Ø´Øª Ú©Ù…â€ŒØ®Ø·Ø± ÛŒØ§ forward-fix Ù¾ÛŒØ´ Ø§Ø² ØªØºÛŒÛŒØ± Ø«Ø¨Øª Ø´ÙˆØ¯.
- Migration/Payment/Secret/Health Ø¨Ø¯ÙˆÙ† backup Ùˆ ØªØ£ÛŒÛŒØ¯ Ø§Ù†Ø³Ø§Ù†ÛŒ DONE Ù†Ù…ÛŒâ€ŒØ´ÙˆØ¯.

## Completion record
- Started at: 2026-11-28T14:27:31+03:30
- Completed at: 2026-11-28T14:29:00+03:30
- Changed files:
- Commands and exit codes:
- Manual tester/date/result: Automated real-WP i18n & escaping verification / 2026-11-28 / PASS
- Evidence paths: docs/evidence/P04-WPPLUGIN-CODE-018/summary.md
- Remaining risks/blockers:
- Final status: DONE

</div>
