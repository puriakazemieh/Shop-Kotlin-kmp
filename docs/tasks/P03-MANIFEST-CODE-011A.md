# P03-MANIFEST-CODE-011A — منبع local/generated و policy پایهٔ bootstrap

- Status: DONE
- Owner: AI
- Depends on: P03-WPPLUGIN-CODE-010
- Blocks: P03-MANIFEST-CODE-011B
- Size: M

## هدف

یک منبع local/generated قابل‌ویرایش برای feature manifest فراهم شود و policy پایه، فقط چهار feature اصلی را از آن بخواند. پیکربندی نامعتبر باید fail-closed شود.

## محدودهٔ مجاز

`core/config/capabilities/**`، `composeApp/**` و `docs/**`.

## پذیرش

- چهار flag `content.blog`، `commerce.core`، `commerce.physical` و `commerce.digital` بدون ویرایش sourceهای پراکنده از یک منبع local تنظیم شوند.
- schema/backend/tenant نامعتبر هیچ featureی را فعال نکند.
- test واحدِ source precedence محلی و fail-closed اضافه و اجرا شود.
- Evidence در `docs/evidence/P03-MANIFEST-CODE-011A/` ثبت شود.

## خارج از محدوده

شبکه، persistence، UI و تغییر production. این‌ها به 011B تا 011E واگذار شده‌اند.

## تکمیل

- Commands and exit codes: `./gradlew.bat --no-daemon :core:config:capabilities:jvmTest`، exit code 0؛ `./gradlew.bat --no-daemon :composeApp:compileKotlinJvm`، exit code 0.
- Evidence paths: `docs/evidence/P03-MANIFEST-CODE-011A/`
- Final status: DONE
