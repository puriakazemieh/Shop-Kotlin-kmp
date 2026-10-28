# P01 — انتقال کنترل‌های باقی‌مانده به فازهای مالک

## تصمیم مالک

در ۲۰۲۶-۱۰-۲۸ مالک پروژه تأیید کرد که کارت‌های باقی‌ماندهٔ P01 به‌عنوان routing/baseline بسته شوند. اجرای واقعی، آزمون و Gate هر کنترل در فاز مالک آن انجام می‌شود. این سند، Completion record مشترک تمام کارت‌هایی است که در جدول P01 با `DONE` و بدون Evidence اجرایی جدید دیده می‌شوند.

## اصل معماری

- business rule،state-machine پرداخت،validation،authorization policy و DTO/contract تا حد ممکن یک‌بار در KMP `core/**`/feature مشترک یا backend/kernel مشترک نوشته می‌شود.
- Android،iOS،Desktop و Web/PWA فقط adapter ورودی/خروجی، lifecycle،storage/vault و قابلیت‌های واقعی OS/browser را دارند؛ کپی‌برداری business logic بین پلتفرم‌ها ممنوع است.
- Theme و Plugin WordPress یک kernel/versioned contract مشترک دارند؛ Spring و WordPress دو backend profile هستند، نه دو پیاده‌سازی مستقل کلاینت.

## نقشه انتقال

| کارت P01 بسته‌شده | فاز/کارت مالک | کنترل باقی‌مانده |
|---|---|---|
| `P01-QA-OPS-003A` | P08/P10 (`P08-QA-MANUAL-016`،`P10-RELEASE-SEC-021`) | QA مرورگر و HTTPS واقعی. |
| `P01-PAYMENT-CODE-008/009` | P05 + P08/P11/P16/P17 (`P05-PAYMENT-CODE-006/007/008`،`P08-PWA-SEC-020`،callbackهای هر platform) | verify authoritative،idempotency،opaque reference و lifecycle. |
| `P01-WPPLUGIN-SEC-010/011/012/013/014` | P04 (`P04-WPPLUGIN-CODE-009/010/014`،`P04-WORDPRESS-CODE-051`) | JWT/OTP/CORS/roles/ownership در kernel و REST مشترک Theme/Plugin. |
| `P01-WPPLUGIN-CODE-015` | P05 + P14 (`P05-PAYMENT-CODE-008/009`،`P14-CLINIC-CODE-008/009`) | ledger/idempotency و session-credit فقط در دامنهٔ مجاز. |
| `P01-WPPLUGIN-CODE-016` | P14 (`P14-CLINIC-CODE-007/008`) | booking و لغو فقط در capability/entitlement مجاز. |
| `P01-PAYMENT-SEC-017` | P05 (`P05-PAYMENT-CODE-007/008` و Gate 024) | amount/currency/reference/replay. |
| `P01-WPPLUGIN-SEC-018` | P13 + P14 (`P13-LMS-SEC-005/008`،`P14-CLINIC-SEC-004/020`) | entitlement و fail-closed LMS/Clinic/Psych. |
| `P01-SECURITY-CODE-019` | P10 (`P10-RELEASE-SEC-020/021`) | secret scan و artifact/release validation. |
| `P01-QA-AUTO-020/QA-MANUAL-021` | P08/P09/P10 و Gateهای پلتفرم | regression hermetic،manual QA و release evidence. |
| `P01-SPRING-SEC-024/025/026/DATA-027` | P15 (`P15-SPRING-SEC-008/009/010/014`،`P15-SPRING-DATA-006/007`) | authz،private files،idempotency و Flyway production. |
| `P01-WPPLUGIN-DATA-028/ARCH-029` | P04 + P05 (`P04-WPPLUGIN-CODE-003/004/032`،`P04-WORDPRESS-DATA-042`،`P05-PAYMENT-DATA-003`) | migration،ledger و kernel مشترک Theme/Plugin. |
| `P01-SECURITY-SEC-022/GATE-023` | P10 (`P10-RELEASE-SEC-020/021`) | threat review،external exposure و release decision. |

## بازگشت شواهد و تیک‌های فازهای قبل

فاز ۰۰ در بخش‌های فاز ۰۰ دو سند راهنما و Master در زمان این ثبت چک‌باکس بازِ انتقال‌یافته ندارد؛ بازآزمایی build جدید مانند P00-T05 نتیجهٔ تاریخی را خودکار تغییر نمی‌دهد. در فاز ۰۱، پس از تکمیل هر کنترل زیر، کارت مقصد Evidence شامل build، محیط، تاریخ و تأیید لازم را ثبت و چک‌باکس متناظر راهنما را به‌روز می‌کند. اگر سناریو چند backend/platform دارد، تا پوشش تمام دامنه‌های لازم تیک نهایی زده نشود؛ نتیجهٔ جزئی در Evidence ثبت شود.

| راهنمای فاز ۰۱ | کارت‌های مقصد برای پیگیری |
|---|---|
| T01، T06 | P04-WPPLUGIN-CODE-014، P04-WORDPRESS-CODE-051، P15-SPRING-SEC-009 |
| T02–T04 | P04-WPPLUGIN-CODE-010، P15-SPRING-SEC-010 |
| T05 | P04-WPPLUGIN-CODE-009، P15-SPRING-SEC-008 |
| T07 | P04-WORDPRESS-CODE-051، P15-SPRING-SEC-010 |
| T08 | P10-RELEASE-SEC-021 و Gateهای platform |
| T09 | P09-QA-GATE-018، P10-RELEASE-SEC-020/021 و Gateهای platform |
| T10–T11 | P05-PAYMENT-CODE-006/007/008، P08-PWA-SEC-020، P11-ANDROID-CODE-006، P16-IOS-CODE-007، P17-DESKTOP-CODE-006 |
| T12 | P13-LMS-SEC-005/008، P14-CLINIC-SEC-004/020 و P05-PAYMENT-CODE-008 |
| T13 | P10-RELEASE-SEC-020 و P15-SPRING-SEC-009 |

توقف‌گاه Clinic/Psych پس از اثبات fail-closed در P13/P14، و توقف‌گاه callback پس از پذیرش P05 و adapterهای مشمول تیک می‌خورند. دو توقف‌گاه Gate و دو معیار عبور باز راهنما تنها پس از جمع‌بندی P10 و Gateهای مرتبط بسته می‌شوند. در Master، بندهای callback و payment پس از P05 و platformهای مشمول؛ IDOR/RBAC پس از P04/P15؛ log/secret پس از P09/P10 و platformها؛ exposure Spring پس از P10/P15؛ و دو بند کلیِ P0 و QA فقط پس از تکمیل همهٔ شواهد و تصمیم انسانی Gate تیک می‌خورند. این نگاشت جایگزین acceptance مستقل کارت مقصد یا Gate انتشار نیست.

## محدودیت وضعیت

`DONE` فاز ۰۱ صرفاً انتقال برنامه‌ریزی است. تا زمانی که کارت مقصد Evidence مستقل و authority لازم ندارد، هیچ قابلیت پرداخت،دادهٔ خصوصی،storage امن،JWT/OTP یا انتشار عمومی PASS محسوب نمی‌شود.
