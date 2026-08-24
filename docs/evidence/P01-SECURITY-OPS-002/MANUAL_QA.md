# P01-SECURITY-OPS-002 — راهنمای QA دستی و Evidence

## وضعیت

این راهنما برای اجرای انسانی/دارای مجوز آماده شده است. هیچ تغییر firewall یا production توسط AI انجام نشده است.

## اجرای AI در 2026-08-24

- در workspace `D:\Android\AndroidStudioProjects\ShopServer\Shop` هیچ فرایند محلی `ngrok` یا `cloudflared` و هیچ listener محلی روی پورت‌های 5432، 8080، 8081 یا 8443 یافت نشد.
- `docker compose ps` هیچ کانتینر فعالی گزارش نکرد.
- درخواست HEAD بدون credential به hostname تونلِ سابق با خطای DNS `No such host is known` پایان یافت؛ در نتیجه در زمان بررسی، آن hostname از این شبکه قابل دسترس نبود.
- تغییرات اعمال‌شده: پیش‌فرض bind سرور `127.0.0.1`، Swagger/OpenAPI پیش‌فرض خاموش، URL تونل پیش‌فرض خالی، و bind پورت PostgreSQL Docker فقط روی `127.0.0.1`.
- هر deploy که `SERVER_ADDRESS=0.0.0.0`، `SWAGGER_ENABLED=true` یا `NGROK_URL` عمومی تنظیم کند، این محدودسازی‌ها را override می‌کند و نیازمند بازبینی و Evidence جدید است.
- Validation: `docker compose config --quiet` در `ShopServer\Shop` با exit code 0 و `gradlew.bat compileKotlin --console=plain` با exit code 0 اجرا شد.
- PostgreSQL با `docker compose up -d --wait --wait-timeout 60 db` بدون reset یا حذف volume قبلی بالا آمد و healthy شد؛ mapping تأییدشده `127.0.0.1:5432->5432/tcp` است.
- اتصال read-only با `docker compose exec -T db psql -U postgres -d shopdb -c "SELECT current_database() ..."` موفق بود و database برابر `shopdb` را برگرداند (exit code 0).
- `gradlew.bat test --console=plain` با PostgreSQL محلی اجرا و با exit code 0 موفق شد. log زمان‌بندی‌شدهٔ payment فقط سه شناسهٔ آزمایشیِ ناموفق را ثبت کرد؛ هیچ secret یا دادهٔ مشتری در این Evidence ذخیره نشده است.
- نام تنظیم callback از `NGROK_URL` به `PAYMENT_CALLBACK_BASE_URL` و property آن به `app.payment.callback-base-url` تغییر کرد. نبود یا نامعتبر بودن URL اکنون fail-closed است و قبل از هر درخواست به درگاه، پرداخت را متوقف می‌کند.
- تست `ZarinPalServiceTest` تأیید می‌کند در نبود URL callback هیچ درخواست HTTP به درگاه ارسال نمی‌شود. این تست و regression کامل `gradlew.bat test --console=plain` هر دو با exit code 0 اجرا شدند.
- مشاهدهٔ خارج از Scope این Task: در اجرای context test، job زمان‌بندی‌شدهٔ payment سه پرداخت آزمایشیِ unverified را بازیابی نکرد. این مورد برای Taskهای بعدی payment باقی می‌ماند و مانع صحت محدودسازی شبکه نیست.

## محیط و محدوده

- فقط hostname، IP و accountهای زیرساختی که مالکیت آن‌ها برای Carmilla تأیید شده است.
- یک اتصال خارج از شبکهٔ استقرار (مانند اینترنت همراه) و یک مرورگر یا ترمینال محلی.
- پنل firewall/security group/reverse proxy یا سرویس‌دهندهٔ میزبان مربوط به Spring.
- داده: نیاز به دادهٔ کاربری یا credential در artifact وجود ندارد. در Evidence فقط شناسهٔ redacted دارایی و زمان ثبت شود.

## مراحل

1. فهرست دارایی‌های ممکن Spring را از DNS، پنل hosting و تنظیمات deploy بررسی کنید. hostname/IP، پورت و مالک هر مورد را خارج از repository ثبت کنید؛ در Evidence فقط نام مستعار یا مقدار mask‌شده بنویسید.
2. از اتصال خارجی و فقط روی دارایی‌های تأییدشده، وضعیت دسترسی TCP پورت‌های منتشرشده را بررسی کنید. پورت‌های رایج HTTP(S) و پورت برنامه تنها در صورتی بررسی شوند که در تنظیمات deploy برای همان دارایی ثبت شده‌اند. endpointهای write، admin، actuator یا دادهٔ واقعی را فراخوانی نکنید.
3. تنظیمات ingress را در firewall/security group/reverse proxy بازبینی کنید. پورت database، cache و هر سرویس داخلی نباید از اینترنت عمومی قابل دسترس باشد.
4. اگر Spring عمومی است، یکی از این اقدامات را با change record انجام دهید: سرویس را تا hardening خاموش/scale-to-zero کنید، یا ingress را فقط به VPN/bastion/reverse proxy و CIDRهای لازم allowlist کنید. هیچ rule جدید `0.0.0.0/0` برای پورت برنامه ایجاد نکنید.
5. دوباره از همان اتصال خارجی بررسی کنید که دسترسی مستقیم به پورت Spring رد می‌شود. اگر reverse proxy عمومیِ موردنیاز باقی مانده است، فقط همان مسیر موردنیاز باید قابل دسترس باشد و پورت برنامه پشت آن قابل دسترسی مستقیم نباشد.
6. یک Evidence redacted ذخیره کنید: تاریخ/ساعت، tester، محیط، شناسهٔ mask‌شدهٔ دارایی، نتیجهٔ قبل و بعد، rule یا وضعیت deploy، و شمارهٔ change/ticket. credential، header، token، IP کامل یا دادهٔ مشتری ثبت نشود.

## معیار پذیرش

- یا هیچ Spring عمومیِ فعالی یافت نشده و inventory/config evidence آن را تأیید می‌کند؛ یا Spring عمومی تا hardening محدود یا خاموش شده است.
- هیچ پورت database یا سرویس داخلی از اینترنت عمومی قابل دسترسی نیست.
- بررسی خارجی پس از تغییر، دسترسی مستقیم به Spring را رد می‌کند.
- Evidence بالا توسط tester انسانی با تاریخ و نتیجه تکمیل شده است.

## بازگشت کم‌خطر

اگر محدودسازی مسیر ضروری را مختل کرد، فقط یک CIDR یا reverse proxy مشخص و موردنیاز را موقتاً allowlist کنید، زمان انقضا و owner تعیین کنید، و سپس برای hardening کامل پیگیری کنید. سرویس را به دسترسی عمومی گسترده بازنگردانید.
