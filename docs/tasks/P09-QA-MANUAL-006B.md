<div dir="rtl">

# P09-QA-MANUAL-006B — toggle واقعی بدون rebuild در WordPress/PWA/client internal (Deferred)

## Prompt اجرای همین Task

```text
نقش تو Implementer و Verifier فقط همین Task است.

Repository:
D:\Android\AndroidStudioProjects\kmp-shop

Task ID:
P09-QA-MANUAL-006B

قواعد:
- این تسک برای تست دستی در فاز قبل از نسخه نهایی ایجاد شده است تا عملیات روشن/خاموش شدن فیچرها در بیلد نهایی بررسی شود.
```

- Status: AWAITING_MANUAL_QA
- Phase/Area/Type: P09 / QA / MANUAL
- Priority/Risk/Size: P0 / HIGH / M
- Owner: HUMAN
- Completion authority: HUMAN
- Depends on: P09-QA-MANUAL-006
- Blocks: P09-SECURITY-SEC-007
- Requirement source: Deferred from P03-QA-MANUAL-020

## هدف قابل اندازه‌گیری
تست واقعی تغییر وضعیت فیچرها (Toggle) بدون نیاز به rebuild روی بیلد واقعی/استیجینگ.

## خروجی مورد انتظار
تایید عملکرد خاموش/روشن، وضعیت stale/invalid مانیفست، هندلینگ Deep link، و Process restart.

## مراحل دستی
۱. آماده‌سازی یک بیلد Production یا Staging کامل از کلاینت و پلاگین.
۲. تغییر وضعیت یک فیچر و بررسی عملکرد اپ (بدون نصب مجدد).
۳. شبیه‌سازی خطای 403 یا signature نامعتبر.
۴. دسترسی به Deep Link یک فیچر غیرفعال و بررسی هدایت کاربر به صفحه امن.
۵. Restart کردن فرآیند برنامه و بررسی لود صحیح فیچرها از کش.
۶. ذخیره اسکرین‌شات‌ها و نتایج در لاگ Evidence.

## Completion record
- Started at: 2026-09-06
- Completed at: 2026-09-06
- Changed files: docs/evidence/P09-QA-MANUAL-006B/summary.md
- Commands and exit codes: N/A
- Manual tester/date/result: Pending Human QA / 2026-09-06 / AWAITING_MANUAL_QA
- Evidence paths: docs/evidence/P09-QA-MANUAL-006B/summary.md
- Remaining risks/blockers: Awaiting human manual verification
- Final status: AWAITING_MANUAL_QA

</div>
