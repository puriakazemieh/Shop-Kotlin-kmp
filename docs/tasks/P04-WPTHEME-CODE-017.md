# P04-WPTHEME-CODE-017 â€” accessibility ÙØ±Ù…/checkout/menu/account

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
P04-WPTHEME-CODE-017

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

- Status: AWAITING_MANUAL_QA
- Phase/Area/Type: P04 / WPTHEME / CODE
- Priority/Risk/Size: P1/MEDIUM / UNASSESSED (Ù‚Ø¨Ù„ Ø§Ø² READY ØªØ¹ÛŒÛŒÙ† Ø´ÙˆØ¯)
- Owner: BOTH
- Completion authority: BOTH
- Depends on: P04-WPTHEME-CODE-016
- Blocks: P04-WPTHEME-CODE-025
- Requirement source: Master checklist row P04-WPTHEME-CODE-017 Ùˆ Source audit Ø¨Ø®Ø´ WPTHEME

## Ù‡Ø¯Ù Ù‚Ø§Ø¨Ù„ Ø§Ù†Ø¯Ø§Ø²Ù‡â€ŒÚ¯ÛŒØ±ÛŒ
accessibility ÙØ±Ù…/checkout/menu/account

## Ø®Ø±ÙˆØ¬ÛŒ Ù…ÙˆØ±Ø¯ Ø§Ù†ØªØ¸Ø§Ø±
keyboardØŒ focusØŒ labelØŒ contrastØŒ zoom 200%

## Ø®Ø§Ø±Ø¬ Ø§Ø² Ù…Ø­Ø¯ÙˆØ¯Ù‡
- Ù‡Ø± FeatureØŒproviderØŒplatform ÛŒØ§ refactor Ø®Ø§Ø±Ø¬ Ø§Ø² Ù‡Ù…ÛŒÙ† Task ID.
- deploy/publishØŒÙ¾Ø±Ø¯Ø§Ø®Øª ÙˆØ§Ù‚Ø¹ÛŒØŒØªØºÛŒÛŒØ± Production Ùˆ ØªØºÛŒÛŒØ± Ø¯Ø§Ø¯Ù‡ Ù…Ø´ØªØ±ÛŒ.

## Preconditions
- Status Ø¨Ø§ÛŒØ¯ READY Ø¨Ø§Ø´Ø¯Ø› TODO Ù…Ø¬ÙˆØ² Ø§Ø¬Ø±Ø§ Ù†ÛŒØ³Øª.
- DependencyÙ‡Ø§: P04-WPTHEME-CODE-016
- git status Ùˆ baseline Ù¾ÛŒØ´ Ø§Ø² ØªØºÛŒÛŒØ± Ø«Ø¨Øª Ø´ÙˆÙ†Ø¯.

## Allowed files/directories
- wordpress/carmilla-theme/**
- wordpress/**/tests/**
- docs/**
- Ø§Ú¯Ø± Ù…Ø³ÛŒØ± Ù„Ø§Ø²Ù… Ø®Ø§Ø±Ø¬ Ø§Ø² Ø§ÛŒÙ† ÙÙ‡Ø±Ø³Øª Ø¨ÙˆØ¯ØŒTask Ø±Ø§ BLOCKED Ú©Ù† Ùˆ Scope Ø¨Ø®ÙˆØ§Ù‡.

## Forbidden actions
- Ø­Ø°Ù/overwrite ØªØºÛŒÛŒØ±Ø§Øª Ú©Ø§Ø±Ø¨Ø±ØŒgit reset/checkoutØŒØ§Ø±ØªÙ‚Ø§ÛŒ dependency ÛŒØ§ ØªØºÛŒÛŒØ± contract Ø®Ø§Ø±Ø¬ Scope.
- Ø«Ø¨Øª credentialØŒØ¯Ø§Ø¯Ù‡ ÙˆØ§Ù‚Ø¹ÛŒ Ù…Ø´ØªØ±ÛŒ ÛŒØ§ Ø¯Ø§Ø¯Ù‡ Ø³Ù„Ø§Ù…Øª Ø¯Ø± repo/Evidence.
- Ø¹Ù…Ù„ÛŒØ§Øª Production ÛŒØ§ migration ØªØ®Ø±ÛŒØ¨ÛŒ.

## Ù…Ø±Ø§Ø­Ù„ Ù¾ÛŒØ§Ø¯Ù‡â€ŒØ³Ø§Ø²ÛŒ
1. Ø¨Ø®Ø´ P04 Ø¯Ø± Master checklist Ùˆ Source audit Ù…Ø±ØªØ¨Ø· Ø±Ø§ Ø¨Ø®ÙˆØ§Ù†.
2. ÙˆØ¶Ø¹ÛŒØª Ù…ÙˆØ¬ÙˆØ¯ Ùˆ baseline Ù…Ø­Ø¯ÙˆØ¯ Ø¨Ù‡ Scope Ø±Ø§ Ú©Ø´Ù Ùˆ Ø«Ø¨Øª Ú©Ù†.
3. Size Ø±Ø§ ØªØ¹ÛŒÛŒÙ† Ú©Ù†Ø› Ø§Ú¯Ø± Ø¨Ø²Ø±Ú¯â€ŒØªØ± Ø§Ø² M Ø§Ø³Øª child Task Ù¾ÛŒØ´Ù†Ù‡Ø§Ø¯ Ø¨Ø¯Ù‡ Ùˆ Ù…ØªÙˆÙ‚Ù Ø´Ùˆ.
4. characterization/test Ù…Ù†ÙÛŒ Ù„Ø§Ø²Ù… Ø±Ø§ Ø§Ø¶Ø§ÙÙ‡ Ú©Ù† ÛŒØ§ Ø¯Ù„ÛŒÙ„ Ù…Ø³ØªÙ†Ø¯ Ù†Ø¨ÙˆØ¯ Ø¢Ù† Ø±Ø§ Ø«Ø¨Øª Ú©Ù†.
5. ÙÙ‚Ø· ØªØºÛŒÛŒØ± Ù„Ø§Ø²Ù… Ø¨Ø±Ø§ÛŒ Ù‡Ø¯Ù Ø±Ø§ Ù¾ÛŒØ§Ø¯Ù‡â€ŒØ³Ø§Ø²ÛŒ Ú©Ù†.
6. validation Ùˆ ØªØ³Øªâ€ŒÙ‡Ø§ Ø±Ø§ Ø§Ø¬Ø±Ø§ØŒEvidence Ø±Ø§ Ø°Ø®ÛŒØ±Ù‡ Ùˆ Status ØµØ­ÛŒØ­ Ø±Ø§ Ø«Ø¨Øª Ú©Ù†.

## Automated tests Ø¨Ø§ command Ùˆ expected result
- Command: Ø¯Ø± Ù…Ø­ÛŒØ· WordPress CI/containerØŒ lint Ùˆ test Ù…Ø­Ø¯ÙˆØ¯ Ø¨Ù‡ Scope Ø±Ø§ Ø§Ø¬Ø±Ø§ Ú©Ù†.
- Expected: activation/install Ùˆ ØªØ³Øª Ù…Ø±ØªØ¨Ø· exit code 0Ø› Ù†Ø¨ÙˆØ¯ PHP Ù…Ø­Ù„ÛŒ Ù…Ø¬ÙˆØ² ØªÛŒÚ©â€ŒØ²Ø¯Ù† Ù†ÛŒØ³Øª.
- Ù…Ø¹ÛŒØ§Ø± Ø§Ø®ØªØµØ§ØµÛŒ: keyboardØŒ focusØŒ labelØŒ contrastØŒ zoom 200%

## Manual tests Ø¨Ø§ environment/data/steps/expected
- Ø§Ú¯Ø± ØªØºÛŒÛŒØ± UI/network/migration Ø¯Ø§Ø±Ø¯ØŒ Ø§Ù†Ø³Ø§Ù† happy pathØŒØ®Ø·Ø§ Ùˆ accessibility Ù…Ø±ØªØ¨Ø· Ø±Ø§ Ø§Ø¬Ø±Ø§ Ù…ÛŒâ€ŒÚ©Ù†Ø¯Ø› Ø¯Ø± ØºÛŒØ± Ø§ÛŒÙ† ØµÙˆØ±Øª N/A Ø±Ø§ Ù…Ø³ØªÙ†Ø¯ Ú©Ù†.
- Environment/device/browser Ùˆ Ø¯Ø§Ø¯Ù‡ synthetic Ø±Ø§ Ø«Ø¨Øª Ú©Ù†.
- Ø§Ù†ØªØ¸Ø§Ø±: keyboardØŒ focusØŒ labelØŒ contrastØŒ zoom 200%
- TesterØŒØªØ§Ø±ÛŒØ®ØŒbuild fingerprintØŒÙ†ØªÛŒØ¬Ù‡ Ùˆ Evidence Ø§Ù„Ø²Ø§Ù…ÛŒ Ø§Ø³Øª.

## Acceptance Criteria
- [ ] Ø®Ø±ÙˆØ¬ÛŒ Ø¨Ø§ Ù‡Ø¯Ù Ùˆ validation Ø§ÛŒÙ† Ú©Ø§Ø±Øª Ù…Ù†Ø·Ø¨Ù‚ Ø§Ø³Øª.
- [ ] Scope Ø®Ø§Ø±Ø¬ Ø§Ø² Allowed files/directories Ú¯Ø³ØªØ±Ø´ Ù†ÛŒØ§ÙØªÙ‡ Ø§Ø³Øª.
- [ ] ØªØ³Øª Ø®ÙˆØ¯Ú©Ø§Ø±/Ø¨Ø§Ø²Ø¨ÛŒÙ†ÛŒ Ù„Ø§Ø²Ù… ÙˆØ§Ù‚Ø¹Ø§Ù‹ Ø§Ø¬Ø±Ø§ Ùˆ Ù†ØªÛŒØ¬Ù‡ Ø«Ø¨Øª Ø´Ø¯Ù‡ Ø§Ø³Øª.
- [ ] Ø§Ú¯Ø± ØªØ³Øª Ø¯Ø³ØªÛŒ Ù„Ø§Ø²Ù… Ø§Ø³ØªØŒEvidence Ø§Ù†Ø³Ø§Ù†ÛŒ Ø«Ø¨Øª Ø´Ø¯Ù‡ ÛŒØ§ Status Ø¨Ø±Ø§Ø¨Ø± AWAITING_MANUAL_QA Ø§Ø³Øª.

## Security/Privacy/Migration checks
- SecretØŒTokenØŒPIIØŒPHI ÛŒØ§ Ø¯Ø§Ø¯Ù‡ Ù…Ø´ØªØ±ÛŒ Ø¯Ø± sourceØŒlog Ùˆ Evidence Ø«Ø¨Øª Ù†Ø´ÙˆØ¯.
- Ø¨Ø±Ø§ÛŒ API/write pathØŒauthorization Ùˆ ownership Ø¨Ø±Ø±Ø³ÛŒ Ø´ÙˆØ¯.
- Ø¨Ø±Ø§ÛŒ migrationØŒforward fix/rollback Ùˆ backup Ø¨Ø±Ø±Ø³ÛŒ Ø´ÙˆØ¯.

## Evidence
- Ù…Ø³ÛŒØ±: docs/evidence/P04-WPTHEME-CODE-017/
- baseline commit/buildØŒcommand/cwd/exit codeØŒtest reportØŒscreenshot redacted Ùˆ reviewer Ø±Ø§ Ø«Ø¨Øª Ú©Ù†.

## Rollback
- Ø±ÙˆØ´ Ø¨Ø§Ø²Ú¯Ø´Øª Ú©Ù…â€ŒØ®Ø·Ø± ÛŒØ§ forward-fix Ù¾ÛŒØ´ Ø§Ø² ØªØºÛŒÛŒØ± Ø«Ø¨Øª Ø´ÙˆØ¯.
- Migration/Payment/Secret/Health Ø¨Ø¯ÙˆÙ† backup Ùˆ ØªØ£ÛŒÛŒØ¯ Ø§Ù†Ø³Ø§Ù†ÛŒ DONE Ù†Ù…ÛŒâ€ŒØ´ÙˆØ¯.

## Completion record
- Started at: 2026-11-28T14:14:11+03:30
- Completed at: 2026-11-28T14:16:20+03:30
- Changed files:
- Commands and exit codes:
- Manual tester/date/result: AWAITING_MANUAL_QA (keyboard, focus, contrast, 200% zoom)
- Evidence paths: docs/evidence/P04-WPTHEME-CODE-017/summary.md
- Remaining risks/blockers:
- Final status: AWAITING_MANUAL_QA
