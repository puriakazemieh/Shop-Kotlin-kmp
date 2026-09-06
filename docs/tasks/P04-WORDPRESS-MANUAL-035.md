<div dir="rtl" align="right">

# P04-WORDPRESS-MANUAL-035 — UAT دو محصول با مانیفست‌ها و نسخه‌های متفاوت

## Prompt اجرای همین Task

```text
نقش تو Implementer و Verifier فقط همین Task است.
Repository: D:\Android\AndroidStudioProjects\kmp-shop
Master checklist: D:\Android\AndroidStudioProjects\kmp-shop\docs\MASTER_IMPLEMENTATION_CHECKLIST_FA.md
Source audit: D:\Android\AndroidStudioProjects\kmp-shop\docs\PROJECT_AUDIT_AND_PUBLICATION_PLAN_FA.md
Task ID: P04-WORDPRESS-MANUAL-035
AGENTS.md،dependency/scope/acceptance،git status و artifact checksum را بررسی کن.
Task انسانی است؛تست اجرا‌نشده تیک نخورد و Status AWAITING_MANUAL_QA؛بدون Evidence DONE نکن.
source/production/داده واقعی/migration destructive را تغییر نده و به Task بعدی نرو.
پاسخ نهایی: Outcome،Environment،Steps/results،Evidence،Acceptance،Checklist،Risks،Rollback.
```

- Status: TODO
- Phase/Area/Type: P04 / WORDPRESS / MANUAL
- Priority/Risk/Size: P0 / CRITICAL / M
- Owner: HUMAN
- Completion authority: HUMAN QA + WordPress Lead
- Depends on: P04-WPPLUGIN-MANUAL-034
- Blocks: P04-QA-MANUAL-022, P04-WORDPRESS-GATE-037
- Requirement source: Master row P04-WORDPRESS-MANUAL-035

## هدف قابل اندازه‌گیری

ترتیب نصب/upgrade،دو SKU متفاوت،خاموشی یکی از میزبان‌ها و mismatch core/schema/entitlement را با snapshot synthetic بررسی کن.

## خروجی مورد انتظار

کجا: پنل هر دو میزبان و داده قبل/بعد. چگونه: Theme Booking + Plugin Academy نصب؛ toggle و جابه‌جایی میزبان؛ دو درخواست build با key یکسان. موفقیت: داده/ID حفظ،یک job/write،مجوز سایت دیگر رد و mismatch قابل اقدام.

## خارج از محدوده

- production upgrade،downgrade destructive،اصلاح defect و publish.

## Preconditions

- Theme و Bridge standalone UAT پاس؛backup staging؛ZIPهای current/previous با checksum؛fixture synthetic.

## Allowed files/directories

- `docs/evidence/P04-WORDPRESS-MANUAL-035/**`
- status/checkbox همین Task در `docs/**`

## Forbidden actions

- اجرای سناریو روی سایت مشتری/production،حذف داده،ویرایش source یا تأیید بدون restore test.

## مراحل پیاده‌سازی

1. قرارداد `docs/INDEPENDENT_PRODUCTS_SPEC_FA.md` و مانیفست بسته را با inventory ZIP همین SKU تطبیق بده؛ نتیجه این کنترل را همراه مراحل زیر ثبت کن.

1. Theme→Bridge و Bridge→Theme activation order را اجرا کن.
2. route/CPT/hook/schema inventory و log را ثبت کن.
3. Theme را به Storefront و برعکس تغییر بده و داده/client را بررسی کن.
4. upgrade از دو fixture قبلی و deactivate/reactivate را اجرا کن.
5. نسخه kernel ناسازگار را نصب و notice/fail-closed/no-write را بررسی کن.
6. backup/rollback یا forward-fix مسیر تأییدشده را restore-test کن.

## Automated tests با command و expected result

- گزارش خودکار `P04-QA-AUTO-020` باید برای همان artifact سبز باشد؛UAT جایگزین آن نیست.

## Manual tests با environment/data/steps/expected

کجا: پنل هر دو میزبان و داده قبل/بعد. چگونه: Theme Booking + Plugin Academy نصب؛ toggle و جابه‌جایی میزبان؛ دو درخواست build با key یکسان. موفقیت: داده/ID حفظ،یک job/write،مجوز سایت دیگر رد و mismatch قابل اقدام.

- محیط staging،داده synthetic،build fingerprint و tester/date/result؛ تا تأیید واقعی AWAITING_MANUAL_QA.

## Acceptance Criteria

- [ ] کجا: پنل هر دو میزبان و داده قبل/بعد. چگونه: Theme Booking + Plugin Academy نصب؛ toggle و جابه‌جایی میزبان؛ دو درخواست build با key یکسان. موفقیت: داده/ID حفظ،یک job/write،مجوز سایت دیگر رد و mismatch قابل اقدام.

- [ ] هر دو activation order و Theme switch پاس‌اند.
- [ ] دو upgrade fixture و mismatch تست شده‌اند.
- [ ] data checksum و inventory قبل/بعد ثبت شده است.
- [ ] rollback/restore و sign-off انسانی موجود است.

## Security/Privacy/Migration checks

- backup،migration lock،idempotency،redacted logs و عدم schema downgrade destructive الزامی.

## Evidence

- `docs/evidence/P04-WORDPRESS-MANUAL-035/`: version matrix،inventories،checksums،logs،restore report و sign-off.

## Rollback

از staging backup تأییدشده restore؛در failure Gate نهایی BLOCKED و defect Task جدا ساخته شود.

## Completion record

- Tested at:
- Artifact matrix/checksums:
- Tester/result:
- Restore result:
- Evidence paths:
- Defects/blockers:
- Final status: TODO | AWAITING_MANUAL_QA | DONE | BLOCKED

</div>
