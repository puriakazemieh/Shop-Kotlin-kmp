# P04-WPTHEME-CODE-025 — رفع Template Hierarchy برگه و سازگاری Elementor Page Layouts

## Prompt اجرای همین Task

```text
نقش تو Implementer و Verifier فقط همین Task است.
Repository: D:\Android\AndroidStudioProjects\kmp-shop
Master checklist: D:\Android\AndroidStudioProjects\kmp-shop\docs\MASTER_IMPLEMENTATION_CHECKLIST_FA.md
Source audit: D:\Android\AndroidStudioProjects\kmp-shop\docs\PROJECT_AUDIT_AND_PUBLICATION_PLAN_FA.md
Task ID: P04-WPTHEME-CODE-025

قبل از تغییر:
1. AGENTS.md و دستورهای موجود را بخوان.
2. Task،dependency،scope،acceptance و source reference را کامل بخوان.
3. git status را بررسی و تغییرات کاربر را حفظ کن.
4. baseline مشخص‌شده را اجرا کن.
5. اگر Task بزرگ‌تر از M یا مبهم است اجرا نکن و child Task پیشنهاد بده.

قواعد:
- فقط همین Task؛ کمترین diff؛ فقط Allowed scope.
- dependency upgrade،refactor جانبی یا تغییر API contract ممنوع.
- secret/PII/PHI و عملیات production/payment واقعی ممنوع.
- ابتدا reproduction و characterization test؛ سپس fix.
- همه verification commandها واقعاً اجرا شوند.
- تست دستی اجرا‌نشده تیک نخورد و Status برابر AWAITING_MANUAL_QA باشد.
- بدون Evidence،Task را DONE نکن؛ فقط status/evidence همین Task را تغییر بده؛ به Task بعدی نرو.

شرایط توقف: تداخل تغییر کاربر،نبود environment/contract،baseline failure مرتبط،عملیات مخرب یا تغییر خارج Scope.
پاسخ نهایی: Outcome،Changed files،Automated tests،Manual QA،Acceptance،Evidence،Checklist change،Risks و Rollback.
```

- Status: TODO
- Phase/Area/Type: P04 / WPTHEME / CODE
- Priority/Risk/Size: P0 / MEDIUM / M
- Owner: AI
- Completion authority: BOTH؛ QA دستی الزامی
- Depends on: P04-WPTHEME-CODE-017
- Blocks: P04-WPPLUGIN-CODE-018 و P04-WPTHEME-GATE-036
- Requirement source: Exploratory Baseline P00-QA-MANUAL-018، Master row P04-WPTHEME-CODE-025 و `plans/003-elementor-page-layout-compatibility.md`

## هدف قابل اندازه‌گیری

برگه‌های عادی و Elementor در layoutهای Default،Elementor Canvas،Elementor Full Width،Carmilla Full Width و Carmilla Blank محتوای صحیح و semantics مورد انتظار header/footer را نمایش دهند.

## خروجی مورد انتظار

root cause با template trace اثبات،کوچک‌ترین fix در Theme اعمال،ماتریس layout ثبت و rendering با Bridge خاموش/روشن یکسان باشد.

## خارج از محدوده

- تغییر Bridge REST/data contract یا الزام Bridge برای render.
- override Elementor core/Pro،افزودن Pro بدون مجوز یا refactor header/footer نامرتبط.
- تست قالب ثالث؛ آن مورد متعلق به Bridge Standalone QA است.

## Preconditions

- Task باید READY و dependency واقعاً DONE باشد.
- WordPress تمیز،Carmilla Theme و Elementor Free در دسترس باشند.
- قبل از fix بررسی شود نبود `page.php`/`singular.php` واقعاً باعث fallback برگه به `index.php` می‌شود یا نه.

## Allowed files/directories

- `wordpress/carmilla-theme/page.php`
- `wordpress/carmilla-theme/singular.php`
- `wordpress/carmilla-theme/page-templates/**`
- `wordpress/carmilla-theme/inc/elementor.php`
- CSS محدود مرتبط با wrapper/content
- `wordpress/**/tests/**`
- `tools/test-env/**`
- `docs/evidence/P04-WPTHEME-CODE-025/**`
- status/checkbox همین Task در `docs/**`

## Forbidden actions

- تغییر `wordpress/carmilla-bridge/**` جز fixture/test صریح این Task.
- تغییر dependency/API،داده واقعی،production یا حذف templateهای کاربر.

## مراحل پیاده‌سازی

1. برگه synthetic با heading/text/image/button بساز و پنج layout را قبل از تغییر ثبت کن.
2. template انتخاب‌شده و اجرای `the_content()` را trace کن؛ فرض fallback به `index.php` را تأیید یا رد کن.
3. characterization test شکست‌خورده برای layout معیوب اضافه کن.
4. در صورت اثبات،کوچک‌ترین `page.php`/`singular.php` استاندارد یا اصلاح template را اعمال کن.
5. `wp_head`،`wp_body_open`،`body_class`،loop،`the_content` و `wp_footer` را بررسی کن.
6. regression برگه عادی،post،archive و 404 را اجرا کن.
7. matrix را بدون Bridge و با Bridge تکرار و Evidence را ثبت کن.

## Automated tests با command و expected result

```powershell
$repo = (Get-Location).Path
docker run --rm -v "${repo}:/app" -w /app php:8.1-cli sh -lc "find wordpress/carmilla-theme -name '*.php' -print0 | xargs -0 -n1 php -l"
docker compose -f tools/test-env/docker-compose.yml config
git diff --check
```

- Expected: syntax و compose config بدون خطا؛ integration test جدید وجود `the_content()` و wrapper صحیح layoutها را اثبات کند.

## Manual tests با environment/data/steps/expected

- Environment: WordPress تمیز + Carmilla + Elementor Free؛ یک دور Bridge خاموش و یک دور روشن.
- Steps: صفحه Elementor QA را در پنج layout و viewportهای 360px/tablet/desktop باز کن؛ Canvas بدون header/footer و Full Width با header/footer و عرض صحیح باشد.
- Regression: یک برگه معمولی،post و archive باز شوند.
- Expected: محتوا کامل،header/footer تکراری صفر،PHP/JS error صفر و نتیجه با روشن‌شدن Bridge تغییر نکند.
- تا تأیید انسان Status برابر AWAITING_MANUAL_QA است.

## Acceptance Criteria

- [ ] root cause و template trace ثبت شده است.
- [ ] پنج layout نتیجه PASS یا unsupported مستند دارند.
- [ ] برگه معمولی،post،archive و 404 regression ندارند.
- [ ] Bridge بر rendering اثر ندارد.
- [ ] QA دستی و Evidence تأیید شده است.

## Security/Privacy/Migration checks

- داده QA synthetic؛ screenshot/log بدون PII و secret.
- migration و تغییر API در این Task وجود ندارد.

## Evidence

- `docs/evidence/P04-WPTHEME-CODE-025/`: before/after،version matrix،template trace،command output و Manual QA record.

## Rollback

فقط template/CSS/integration همین Task revert شود؛ برگه و داده Elementor حذف نشود؛ syntax و smoke برگه عادی دوباره اجرا شود.

## Completion record

- Started at:
- Completed at:
- Changed files:
- Commands and exit codes:
- Manual tester/date/result:
- Evidence paths:
- Remaining risks/blockers:
- Final status: TODO | CODE_COMPLETE | AWAITING_MANUAL_QA | DONE | BLOCKED
