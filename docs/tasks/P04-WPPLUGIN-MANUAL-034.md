# P04-WPPLUGIN-MANUAL-034 — UAT Bridge روی قالب ثالث و Android/PWA

## Prompt اجرای همین Task

```text
نقش تو Implementer و Verifier فقط همین Task است.
Repository: D:\Android\AndroidStudioProjects\kmp-shop
Master checklist: D:\Android\AndroidStudioProjects\kmp-shop\docs\MASTER_IMPLEMENTATION_CHECKLIST_FA.md
Source audit: D:\Android\AndroidStudioProjects\kmp-shop\docs\PROJECT_AUDIT_AND_PUBLICATION_PLAN_FA.md
Task ID: P04-WPPLUGIN-MANUAL-034
AGENTS.md،dependency/scope/acceptance،git status و artifact checksum را بررسی کن.
این Task انسانی است؛AI فقط environment/data/steps/evidence را آماده و نتیجه واقعی را ثبت می‌کند.
تست اجرا‌نشده را تیک نزن؛تا تأیید انسان AWAITING_MANUAL_QA؛بدون Evidence DONE نکن و به Task بعدی نرو.
production،داده/credential واقعی و publish ممنوع.
پاسخ نهایی: Outcome،Environment،Steps/results،Evidence،Acceptance،Checklist،Risks،Rollback.
```

- Status: TODO
- Phase/Area/Type: P04 / WPPLUGIN / MANUAL
- Priority/Risk/Size: P0 / HIGH / M
- Owner: HUMAN
- Completion authority: HUMAN QA + Product Owner
- Depends on: P04-QA-MANUAL-021
- Blocks: P04-WORDPRESS-MANUAL-035 و P04-WPPLUGIN-GATE-024
- Requirement source: Master row P04-WPPLUGIN-MANUAL-034

## هدف قابل اندازه‌گیری

Bridge ZIP بدون Carmilla Theme روی Storefront و حداقل یک قالب ثالث واقعی به Android و PWA متصل و تمام capabilityهای declared به‌صورت دستی اعتبارسنجی شود.

## خروجی مورد انتظار

UAT matrix امضاشده برای install/onboarding/auth/content/store/academy/clinic/psych/support/feature toggle و App Builder fake control plane.

## خارج از محدوده

- publish،پرداخت/provider production،ساخت/sign واقعی اپ و اصلاح defect داخل همین Task.

## Preconditions

- artifact CI همان checksum تست خودکار؛دو WordPress تمیز؛داده synthetic؛client test build.

## Allowed files/directories

- `docs/evidence/P04-WPPLUGIN-MANUAL-034/**`
- status/checkbox همین Task در `docs/**`

## Forbidden actions

- تغییر source،استفاده از مشتری/PHI واقعی یا اعلام PASS برای مورد اجرا‌نشده.

## مراحل پیاده‌سازی

1. Bridge را جداگانه روی Storefront و قالب ثالث نصب و onboarding کن.
2. Android/PWA را pair و manifest/navigation را تطبیق بده.
3. CRUD مجاز/غیرمجاز و happy/error/empty/offline را برای verticalها اجرا کن.
4. Theme را عوض کن و data/API/client را دوباره بررسی کن.
5. fake build request و expiry artifact metadata را تست کن.
6. defect ID،severity،screenshot/video/log redacted و نتیجه را ثبت کن.

## Automated tests با command و expected result

- N/A برای اجرای انسانی؛قبل از شروع گزارش سبز `P04-QA-AUTO-020` و checksum artifact review شود.

## Manual tests با environment/data/steps/expected

- Environment: WordPress/PHP/Woo/Theme/Bridge versions،Android device و browser/PWA ثبت شود.
- Expected: Theme میزبان سالم،manifest و CRUD صحیح،unauthorized denied،data loss/fatal/P0 صفر.

## Acceptance Criteria

- [ ] هر دو Theme میزبان و هر دو client تست شده‌اند.
- [ ] همه capabilityهای declared نتیجه دارند.
- [ ] security negative paths و theme switch پاس‌اند.
- [ ] Product Owner/QA نتیجه را امضا کرده‌اند.

## Security/Privacy/Migration checks

- داده synthetic،logs redacted،CORS/ownership منفی و عدم native build واقعی بررسی شود.

## Evidence

- `docs/evidence/P04-WPPLUGIN-MANUAL-034/`: matrix،versions/checksum،screenshots/video،defects و sign-off.

## Rollback

در fail،Gate مسدود و defect Task ساخته شود؛environment staging reset شود و site data مشتری وجود نداشته باشد.

## Completion record

- Tested at:
- Artifact/checksum:
- Environments/devices:
- Tester/result:
- Evidence paths:
- Defects/blockers:
- Final status: TODO | AWAITING_MANUAL_QA | DONE | BLOCKED
