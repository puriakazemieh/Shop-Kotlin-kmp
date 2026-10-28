# P10-RELEASE-SEC-021 — اعتبارسنجی HTTPS/TLS کلاینت پیش از انتشار محدود

> پیگیری چک‌باکس‌های فازهای قبل: پس از تکمیل کنترل‌های مرتبط این کارت و ثبت Evidence و تأیید لازم، طبق نگاشت `docs/evidence/P01-PLATFORM-SCOPE-TRANSFER-20261028/summary.md` نتیجه را به چک‌باکس متناظر راهنمای تست دستی و Gate در Master بازگردانید. چک‌باکس مشترک فقط با تکمیل تمام بخش‌های مربوط به همهٔ مقصدها تیک می‌خورد؛ Evidence جزئی را ثبت کنید.

- Status: TODO
- Phase/Area/Type: P10 / RELEASE / SECURITY
- Priority/Risk/Size: P0/HIGH / S
- Owner: BOTH
- Depends on: P10-RELEASE-SEC-020

## هدف

روی artifact انتشار و endpoint واقعی HTTPS، transport security کلاینت‌های Carmilla بررسی شود. این کارت جایگزین هیچ Gate امنیتی پیش از انتشار نیست.

## پذیرش

- build انتشار به endpoint HTTPS معتبر متصل می‌شود.
- HTTP، certificate نامعتبر/منقضی و MITM رد می‌شوند.
- redirect یا override ناامن token را به host غیرمجاز ارسال نمی‌کند.
- برای Web/PWA: کوکی `__Host-shop_refresh` دارای `HttpOnly`،`Secure`،`SameSite=Strict` و `Path=/` است؛ refresh token در response body،Local Storage یا Session Storage دیده نمی‌شود؛ refresh cookie را rotate می‌کند و logout آن را حذف و refresh قبلی را بی‌اعتبار می‌کند.
- tester، تاریخ، build fingerprint و evidence redacted ثبت می‌شود؛ هیچ token، password، cookie یا داده مشتری در Evidence نیست.

## Manual QA

محیط release/staging با certificate معتبر و یک proxy تست کنترل‌شده لازم است. سناریوهای HTTP، certificate نامعتبر و MITM را اجرا کنید و فقط نتیجهٔ redacted را ثبت کنید. سپس برای Web/PWA login،refresh و logout را اجرا و در DevTools قرمز‌شده ویژگی‌های cookie و نبود token در browser storage را ثبت کنید.

## ورودی انتقال‌یافته از فاز ۰۱

- `P01-SECURITY-CODE-005C` در ۲۰۲۶-۱۰-۲۸ با تأیید مالک پروژه به این کارت منتقل شد، زیرا فقط HTTPS واقعی می‌تواند ویژگی `Secure` cookie و چرخهٔ refresh/logout را اعتبارسنجی کند.
- این کارت باید هم TLS/host binding و هم قرارداد session-cookie وب را در یک build release/staging واقعی بپوشاند؛ تأیید P01 جای Evidence این کارت نیست.
