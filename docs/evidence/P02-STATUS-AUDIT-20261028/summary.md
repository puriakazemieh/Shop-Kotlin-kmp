# ممیزی وضعیت فاز ۰۲ — ۲۸ اکتبر ۲۰۲۶

<div dir="rtl" align="right">

## دامنه و نتیجه

صف، جدول فاز ۰۲ در Master، راهنمای تست دستی، ۲۲ کارت فاز ۰۲ و شواهدشان با پیاده‌سازی مرتبط در KMP، Android، WordPress، گردش‌کارهای GitHub و مخزن جداگانهٔ ShopServer تطبیق داده شد. این بررسی جای اجرای CI روی PR، آزمون Android و WordPress یا تأیید انسانی را نمی‌گیرد. فایل‌های از پیش تغییریافتهٔ کاربر دست‌نخورده ماندند.

نتیجهٔ وضعیت جاری: ۱۲ کارت `DONE` و ۱۰ کارت `TODO`. از ۱۰ کارت `TODO`، دو کارت `P02-CORE-CODE-015B` و `P02-CI-OPS-016` از قبل باز بودند؛ هشت تیک `DONE` بدون معیار پذیرش کافی به `TODO` برگشتند. تصمیم تاریخی مالک برای `P02-CORE-GATE-018` یک `CONDITIONAL_PASS` برای ادامهٔ فازها بود، نه تأیید تکمیل همهٔ کنترل‌ها یا مجوز انتشار.

## موارد دارای پیاده‌سازی قابل مشاهده و وضعیت `DONE` حفظ‌شده

| کارت | آنچه دیده شد | حد اثبات |
|---|---|---|
| `P02-CORE-DISC-001` | نقشهٔ ۲۹ ماژول و couplingها در Evidence ثبت است. | مستند تاریخی؛ با تغییر ماژول‌ها ممکن است نیاز به تازه‌سازی داشته باشد. |
| `P02-CORE-CODE-002` | Version Catalog و ارجاع‌های `libs.*` در Gradle موجود است. | ادعای عدم تغییر resolution با اجرای تازهٔ clean build تأیید نشد. |
| `P02-CORE-ADR-003` | ADR convention plugin موجود است. | تأیید معماری مستند است. |
| `P02-CORE-CODE-004/005A/005B/005C` | `build-logic` و conventionها موجود و به core، feature و `composeApp` اعمال شده‌اند. | همهٔ targetها، Android و رفتار قبلی در این ممیزی دوباره آزموده نشدند؛ Evidence این کارت‌ها بیشتر شرح کار است. |
| `P02-QA-CODE-008` | تست سرور با `@Testcontainers`، `@ServiceConnection` و profile آزمایشی در ShopServer موجود است. | اجرای تازهٔ سرور وابسته به Docker در این ممیزی انجام نشد. |
| `P02-ARCH-ADR-012` | ADR جداسازی Android shell و rollback موجود است. | سند، نه آزمون upgrade. |
| `P02-CORE-CODE-015` | کار به دو کارت `015A` و `015B` شکسته شده است. | تکمیل parent به معنی تکمیل `015B` نیست. |
| `P02-CORE-CODE-015A` | `architectureCheck` وجود دارد و در اجرای فعلی سبز شد؛ چهار استثنای فعلی را گزارش کرد. | اجرای موفق به معنی رفع چهار استثنا نیست؛ اجرای آن به PR gate متصل نشده است. |
| `P02-CORE-GATE-018` | مصوبهٔ مشروط مالک در کارت و Evidence ثبت است. | خود Evidence همچنان rollback سازگار، integration وردپرس و شواهد CI را باز می‌داند. |

## تیک‌های اصلاح‌شده از `DONE` به `TODO`

| کارت | دلیل مشخص |
|---|---|
| `P02-QA-CODE-006` | `EmailValidatorTest` و `KtorClientTest` فقط assertion ثابت دارند؛ unit واقعی domain و network contract وعده‌داده‌شده را نمی‌آزمایند. سبز شدن این تست‌ها معیار را ثابت نمی‌کند. |
| `P02-QA-CODE-007` | workflow ماتریس PHP/WP/Woo را تعریف کرده، اما گزارش اجرای clean install و smoke در CI ندارد. دست‌کم smoke واقعی `smoke-web-login-real-wp.php` به `CARMILLA_LOGIN_DISPOSABLE=yes`، prefix تصادفی و نصب WP در `/usr/src/wordpress/` نیاز دارد؛ workflow آن‌ها را فراهم نمی‌کند و تنها `php smoke*.php` را در runner اجرا می‌کند. |
| `P02-CI-CODE-009` | `pr-gate.yml` lint/test/package را تعریف کرده اما integration وردپرس را اجرا نمی‌کند و شاهد PR آزمایشی با شکست مسدودکننده یا report واقعی ندارد. |
| `P02-CI-CODE-010` | `code-quality.yml` فاقد Theme Check است و `detekt-config.yml` ارجاع‌شده در مخزن یافت نشد. شاهد اجرای موفق یا negative gate نیز نیست. |
| `P02-CI-CODE-011` | `security.yml` مدعی dependency verification است، اما `gradle/verification-metadata.xml` و lockfile وجود ندارند. آزمون منفی tampered dependency و secret fixture ثبت نشده است. |
| `P02-ARCH-CODE-013` | ماژول thin `androidApp` موجود است؛ Completion record فقط `gradlew tasks` را ثبت می‌کند. آزمون Android build، مقایسهٔ applicationId/flavorها و upgrade test ثبت نشده‌اند. `targetSdk=34` در `androidApp` نیز با `RTK.md` که ۳۶ می‌خواهد ناسازگار است. |
| `P02-ARCH-CODE-014` | گراف‌های feature جدا شده‌اند، ولی Evidence فقط compile را مطرح می‌کند و حتی اجرای آن را «در حال اجرا» می‌نامد؛ نتیجهٔ characterization/deep-link قبل و بعد ثبت نشده است. |
| `P02-QA-MANUAL-017` | مصوبهٔ تاریخی مالک برای بستن کارت ثبت است، اما Smoke واقعی `auth/home/product/cart/payment-return` با fixture، tester، تاریخ، build fingerprint و نتیجه وجود ندارد. خود completion اجرای این smoke را به فاز ۰۹ موکول می‌کند. |

## مواردی که از قبل `TODO` بودند

- `P02-CORE-CODE-015B`: کد Profile و Admin دیگر dependency مستقیم feature ندارد و `architectureCheck` سبز است، اما کارت هنوز Completion record معتبر ندارد؛ چهار استثنای دیگر باقی‌اند و معیار کامل این کارت با آزمون منفی و شواهد کامل بسته نشده است.
- `P02-CI-OPS-016`: workflow فعلی با تگ، `assembleDebug` می‌سازد و `android-debug-apks` بارگذاری می‌کند. نسخه، checksum و SBOM ندارد؛ جداسازی release از debug انجام نشده است.

## کنترل‌های باز و آزمون لازم

در راهنمای دستی، `P02-T01` تا `P02-T09` و دو معیار عبور فاز همگی باز هستند. برای بستن ادعاهای فاز، از checkout تمیز build و testهای KMP/Android را با گزارش و fingerprint اجرا کنید؛ matrix وردپرس را واقعاً در CI اجرا کنید؛ تست Spring را با Docker بدون DB دستی اجرا کنید؛ روی PR آزمایشی هر gate را با خطای عمدی fail کنید؛ dependency verification/locking را از checkout تازه و با fixture دستکاری‌شده بیازمایید؛ APK را نصب و مسیرهای `auth/home/product/cart/payment-return` را با دادهٔ مصنوعی و نسخهٔ قبل مقایسه کنید؛ rollback/forward-fix را روی artifact و DB سازگار تمرین و exceptionهای معماری را با owner و deadline ثبت کنید. نتیجهٔ هر گام باید command، محیط، exit code، build/commit، tester و گزارش redacted داشته باشد. تا تأیید انسانی، smoke و gate انتشار نباید کامل اعلام شوند.

## آزمون انجام‌شده در این ممیزی

- `./gradlew.bat architectureCheck :core:domain:jvmTest :core:network:jvmTest --console=plain` — exit ۰؛ `BUILD SUCCESSFUL` در ۲۹ ثانیه. `architectureCheck` چهار استثنای موجود (`academy → details`، `clinic → details`، `main → cart/catalog`) را چاپ کرد. دو تست commonTest مورد ادعای کارت ۰۰۶ با وجود سبز بودن assertion ثابت دارند.
- `git diff --check` — بدون خطای whitespace. این آزمون‌ها CI واقعی، Android، WordPress یا ShopServer را تأیید نمی‌کنند.

## محدودیت تصمیم صف

هیچ ردیف `READY` در صف فعلی پیدا نشد. این ممیزی فقط اصلاح وضعیت و Evidence است؛ اجرای کارت بعدی یا عبور phase gate انجام نشده است. رابطهٔ dependency کارت `017` و Gate `018` پس از تنزل `017` ناسازگار است؛ مصوبهٔ مشروط تاریخی مالک حفظ شده، ولی برای ادعای عبور نهایی باید دوباره با شواهد و تأیید انسانی رسیدگی شود.

</div>
