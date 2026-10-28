# P01-QA-OPS-003A — fixture محلیِ نشست امن مرورگر برای QA ورود PWA

- Status: DONE
- Phase/Area/Type: P01 / QA / OPS
- Priority/Risk/Size: P0/HIGH / M
- Owner: AI
- Completion authority: AI
- Depends on: P01-SECURITY-OPS-002
- Blocks: P01-SECURITY-CODE-003

## هدف قابل اندازه‌گیری

محیط Docker محلی بتواند routeهای `api/auth/web/*` را تنها با تنظیمات صریح local-QA و migration دستی اجرا کند؛ ورود PWA با cookie/session مرورگر برقرار شود و مسیرهای عادی production تغییر نکنند.

## Scope مجاز

- `tools/test-env/**`
- `wordpress/carmilla-bridge/**`
- `docs/**`

## خارج از محدوده

- هر پیکربندی production، انتشار، secret ثابت، تغییر قرارداد endpoint کلاینت و هر migration خودکار.

## معیار پذیرش

- [ ] fixture فقط در Docker test environment فعال است و `CB_WEB_SESSION_*` را از request دریافت نمی‌کند.
- [ ] کلید session در source یا compose hardcode نشده و از saltهای runtime محلی مشتق می‌شود.
- [ ] migration تنها با فرمان CLI صریح و idempotent اجرا می‌شود.
- [ ] routeهای web login/register/refresh/logout پس از migration ثبت‌اند؛ خارج از fixture ثبت نمی‌شوند.
- [ ] ورود واقعی PWA با حساب synthetic به HTTP 200 می‌رسد؛ token/cookie/body در Evidence ثبت نمی‌شود.
- [ ] تست PHP محدود و بررسی routeهای HTTP پاس می‌شوند.

## Evidence

- مسیر: `docs/evidence/P01-QA-OPS-003A/`
- خروجی فرمان‌ها، وضعیت routeها، build fingerprint و نتیجهٔ redacted ثبت می‌شود.

## Manual QA

پس از تکمیل، انسان با حساب synthetic وارد PWA می‌شود و Console را فقط از نظر نبود markerهای حساس بررسی می‌کند؛ token، password یا body درخواست در Evidence ذخیره نمی‌شود.

## Rollback

containerهای `tools/test-env` را متوقف کنید یا mountهای test fixture را حذف کنید؛ migration هیچ جدول یا داده‌ای را حذف نمی‌کند.

## Blocker record — 2026-10-28

fixture موجود WordPress و MariaDB را در containerهای جدا اجرا می‌کند و `DB_HOST=db` است. `CB_Web_Session_WP_Adapter` به‌صورت fail-closed فقط `127.0.0.1` یا `localhost:<port>` را برای اتصال مستقل به session store می‌پذیرد و در Linux، `localhost` را نیز رد می‌کند تا اتصال socket/شبکه‌ای مبهم نباشد. بنابراین ثبت routeهای `api/auth/web/*` در این topology ممکن نیست. باز کردن validation در adapter ممنوع است؛ راه‌حل به طراحی یک fixture یک‌container یا topology loopback-verified و سپس یک migration صریح نیاز دارد. دو جدول session محلی با migration آزمایشی ایجاد شده‌اند؛ هیچ دادهٔ کاربر حذف یا تغییر نکرده است.
