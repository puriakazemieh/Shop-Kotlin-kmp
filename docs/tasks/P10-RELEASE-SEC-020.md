# P10-RELEASE-SEC-020 — اعتبارسنجی سطح خارجی Spring پیش از انتشار عمومی

- Status: TODO
- Phase/Area/Type: P10 / RELEASE / SECURITY
- Priority/Risk/Size: P0/HIGH / S
- Owner: BOTH
- Depends on: P10-BUSINESS-GATE-018
- Requirement source: follow-up of P01-SECURITY-OPS-002 local-only scope decision

## هدف

پیش از هر انتشار عمومی Spring، سطح خارجی واقعی را با hostname/IP/CDN/load balancer و firewall نهایی اعتبارسنجی کن؛ این کارت جایگزین تست محلی نیست.

## Preconditions

- یک محیط staging یا release-candidate عمومیِ مورد تأیید مالک، همراه با دامنه یا IP قطعی و دسترسی مشاهده‌ای به تنظیمات ingress/firewall فراهم باشد.
- دادهٔ آزمایشی و credentialهای موقت وجود داشته باشند؛ هیچ secret، توکن، دادهٔ مشتری یا خروجی حساس در Evidence ذخیره نشود.

## مراحل و معیار پذیرش

1. inventory دامنه، IP، CDN/WAF، load balancer، پورت‌های exposed و مالک هر جزء را ثبت کن.
2. از یک شبکه بیرونی، اسکن محدود و مجاز پورت‌ها/HTTP(S) را اجرا کن؛ فقط سطح‌های مصوب باید قابل مشاهده باشند.
3. عدم دسترسی عمومی PostgreSQL و هر پورت مدیریتی را تأیید کن.
4. HTTPS، certificate، redirect، CORS، security headers، allowlist/firewall و مسیرهای health/admin را با قرارداد release بررسی کن.
5. نتیجهٔ رد/قبول، تاریخ، محیط، ابزار و خروجی redacted را در `docs/evidence/P10-RELEASE-SEC-020/` ثبت کن.

## موفقیت

- هیچ دیتابیس یا پورت داخلی/مدیریتی از اینترنت قابل دسترسی نباشد.
- فقط ingressهای صریحاً تأییدشده در دسترس باشند و کنترل‌های HTTPS/CORS/firewall درست عمل کنند.
- یافتهٔ HIGH/CRITICAL پیش از انتشار برطرف یا با تصمیم صریح مالک block شود.

## Rollback

در صورت مشاهدهٔ exposure، endpoint یا rule جدید را فوراً از ingress/allowlist خارج کن و انتشار را متوقف نگه دار؛ تغییر production بدون مالک مجاز نیست.
