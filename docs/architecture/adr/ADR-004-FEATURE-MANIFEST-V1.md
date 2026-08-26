# ADR-004: قرارداد `FeatureManifest v1`

- **وضعیت:** پذیرفته‌شده برای ادامهٔ اجرای P03
- **تاریخ:** 2026-08-26
- **تصمیم‌گیران:** مالک محصول، لید فنی (AI)
- **تسک:** `P03-MANIFEST-ADR-001`
- **جایگزین:** جزئیات Manifest در ADR-002؛ خود ADR-002 همچنان تصمیم معماری tenancy ترکیبی است.

## زمینه

`BrandConfig` فعلی هم‌زمان تنظیمات برند، آدرس backend و flagهای vertical را نگه می‌دارد. محصول باید فقط با دو پروفایل backend یعنی `WORDPRESS` و `SPRING` عرضه شود و قابلیت‌های هر tenant از طریق یک Manifest معتبر و فقط در محدودهٔ قابلیت‌های کامپایل‌شده کنترل شوند. این سند قرارداد نسخهٔ اول را برای KMP، WordPress و پیاده‌سازی بعدی Spring تثبیت می‌کند.

## تصمیم

### مرز اعتماد

- `BackendProfile` تنظیم bootstrap/build قابل اعتماد است و مالک `apiRoot`، `assetRoot`، `allowedAuthHosts`، نسخهٔ قرارداد و URL Manifest است.
- Manifest راه‌دور **حق ندارد** origin backend یا asset، hostهای مجاز احراز هویت، هویت package، هویت امضا یا سقف قابلیت‌های کامپایل‌شده را تغییر دهد.
- Android یک Manifest پیش‌فرض داخل برنامه دارد؛ Manifest راه‌دور فقط می‌تواند قابلیت‌ها را کمتر کند.
- PWA فقط Manifest همان origin قابل اعتماد یا endpoint امضاشدهٔ مشخص‌شده در پروفایل را دریافت می‌کند؛ تنظیم با query-string ممنوع است.

### پوشش داده و اعتبارسنجی

- `schemaVersion` عدد صحیح `1` است.
- `backendProfile` دقیقاً یکی از `WORDPRESS` یا `SPRING` است و باید با پروفایل تغییرناپذیر کامپایل‌شده در برنامه برابر باشد.
- `tenantId`، `manifestVersion`، `issuedAt`، `minimumAppVersion`، `features` و `integrity` اجباری‌اند.
- الگوریتم `integrity.algorithm` برابر `Ed25519` است؛ `keyId` کلید عمومی pin‌شده را انتخاب می‌کند و `signature` روی JSON canonical، بدون خود شیء `integrity`، محاسبه می‌شود.
- ترتیب اعتبارسنجی: parse کردن JSON ← نسخهٔ schema ← برابری profile ← امضا ← سیاست انقضا/نسخه ← شناسه‌های قابلیت شناخته‌شده ← حل dependency ← سقف کامپایل‌شده/سیاست پلتفرم.
- هر خطا برنامه را به حالت bootstrap امن می‌برد: هیچ قابلیت حساس، route، ماژول DI، use case یا endpoint backend در دسترس نمی‌شود. Clinic، psych، admin و payment همواره fail-closed هستند.
- قابلیت ناشناخته، الگوریتم integrity ناشناخته، امضای نامعتبر، dependency ناقص یا schema پشتیبانی‌نشده کل Manifest راه‌دور را رد می‌کند. Telemetry فقط کلاس خطا و revision را ثبت می‌کند؛ token، signature یا راز tenant نباید ثبت شود.

### وابستگی قابلیت‌ها

| قابلیت | پیش‌نیاز |
|---|---|
| `commerce.physical` | `commerce.core` |
| `commerce.digital` | `commerce.core` |
| `academy.core` | `content.blog` |
| `academy.quiz` | `academy.core` |
| `academy.certificate` | `academy.core` |
| `clinic.booking` | `content.blog` |
| `clinic.messaging` | `clinic.booking` |
| `psych.tests` | `content.blog` |
| `wallet` | `commerce.core` |
| `admin.mobile` | یک قابلیت domain فعال به‌همراه مجوز backend |

فعال‌بودن قابلیت، مجوز دسترسی نمی‌دهد. backend باید entitlement Manifest و مالکیت actor را برای همهٔ مسیرهای read/write نیز اعمال کند.

### سیاست سازگاری رو به عقب

- کلاینتی که v1 را پیاده‌سازی می‌کند فقط `schemaVersion: 1` را می‌پذیرد.
- فیلد اختیاری جدید به ADR تازه و default سازگار با v1 نیاز دارد؛ فیلد اجباری جدید یا تغییر معنا به `schemaVersion: 2` نیاز دارد.
- کلاینت پایین‌تر از `minimumAppVersion` از Manifest امن داخلی استفاده و حالت «نیاز به به‌روزرسانی» نشان می‌دهد؛ به flagهای قدیمی vertical برنمی‌گردد.
- آخرین Manifest معتبر راه‌دور فقط تا `expiresAt` قابل cache است؛ namespace آن `backendProfile + tenantId + normalizedOrigin` است و با تغییر هرکدام پاک می‌شود.

## JSON Schema (پیش‌نویس 2020-12)

```json
{
  "$schema": "https://json-schema.org/draft/2020-12/schema",
  "$id": "https://carmilla.example/contracts/feature-manifest-v1.schema.json",
  "title": "Carmilla FeatureManifest v1",
  "type": "object",
  "additionalProperties": false,
  "required": ["schemaVersion", "manifestVersion", "backendProfile", "tenantId", "minimumAppVersion", "issuedAt", "expiresAt", "features", "integrity"],
  "properties": {
    "schemaVersion": { "const": 1 },
    "manifestVersion": { "type": "string", "pattern": "^[0-9]{4}\\.[0-9]{2}\\.[0-9]+$" },
    "backendProfile": { "enum": ["WORDPRESS", "SPRING"] },
    "tenantId": { "type": "string", "pattern": "^[a-z0-9][a-z0-9-]{1,62}$" },
    "minimumAppVersion": { "type": "string", "pattern": "^[0-9]+\\.[0-9]+\\.[0-9]+$" },
    "issuedAt": { "type": "string", "format": "date-time" },
    "expiresAt": { "type": "string", "format": "date-time" },
    "seedPack": { "type": "string", "maxLength": 80 },
    "features": {
      "type": "object", "additionalProperties": false,
      "properties": {
        "content.blog": { "type": "boolean" }, "commerce.core": { "type": "boolean" },
        "commerce.physical": { "type": "boolean" }, "commerce.digital": { "type": "boolean" },
        "academy.core": { "type": "boolean" }, "academy.quiz": { "type": "boolean" },
        "academy.certificate": { "type": "boolean" }, "clinic.booking": { "type": "boolean" },
        "clinic.messaging": { "type": "boolean" }, "psych.tests": { "type": "boolean" },
        "wallet": { "type": "boolean" }, "admin.mobile": { "type": "boolean" }
      }
    },
    "integrity": {
      "type": "object", "additionalProperties": false,
      "required": ["algorithm", "keyId", "signature"],
      "properties": {
        "algorithm": { "const": "Ed25519" },
        "keyId": { "type": "string", "pattern": "^[A-Za-z0-9._-]{1,64}$" },
        "signature": { "type": "string", "minLength": 64, "maxLength": 512 }
      }
    }
  }
}
```

## تنظیمات مرجع F0 تا F4

همهٔ مثال‌ها از پوشش یکسان و امضای جایگزین (`<signed-by-pinned-key>`) استفاده می‌کنند. داده‌ها ساختگی‌اند و استفاده از آن‌ها به‌عنوان دادهٔ tenant تولیدی ممنوع است.

| تنظیم | قابلیت‌های روشن | هدف |
|---|---|---|
| F0 | `content.blog` | حداقل امن، فقط محتوا |
| F1 | F0 + `commerce.core`، `commerce.physical` | انتشار اول فروشگاه |
| F2 | F0 + `commerce.core`، `commerce.digital`، `academy.core`، `academy.quiz`، `academy.certificate` | tenant آکادمی |
| F3 | F0 + `clinic.booking`، `clinic.messaging` | tenant کلینیک؛ بررسی رابطه در backend لازم است |
| F4 | F0 + `psych.tests` | tenant روان‌شناسی؛ سیاست رضایت/نگه‌داری لازم است |

```json
{
  "schemaVersion": 1, "manifestVersion": "2026.08.1", "backendProfile": "WORDPRESS",
  "tenantId": "fixture-f1-shop", "minimumAppVersion": "1.0.0",
  "issuedAt": "2026-08-26T00:00:00Z", "expiresAt": "2026-09-02T00:00:00Z",
  "seedPack": "shop-fa-v1",
  "features": {
    "content.blog": true, "commerce.core": true, "commerce.physical": true,
    "commerce.digital": false, "academy.core": false, "academy.quiz": false,
    "academy.certificate": false, "clinic.booking": false, "clinic.messaging": false,
    "psych.tests": false, "wallet": false, "admin.mobile": false
  },
  "integrity": { "algorithm": "Ed25519", "keyId": "fixture-key-v1", "signature": "<signed-by-pinned-key>" }
}
```

تنظیم‌های F0، F2، F3 و F4 همین پوشش را با ماتریس قابلیت جدول بالا دارند. fixture کامل و آزمون امضا وظیفهٔ `P03-MANIFEST-CODE-007` و `P03-MANIFEST-CODE-008` است.

## پیامدها

- `P03-ARCH-CODE-003` Manifest، profile، branding و build identity را از `BrandConfig` جدا می‌کند.
- `P03-ARCH-CODE-004` مرز اعتماد پروفایل تغییرناپذیر را پیاده می‌کند.
- `P03-MANIFEST-CODE-007` و `P03-MANIFEST-CODE-008` catalog، dependency resolver، سقف کامپایل‌شده، parse schema و fixtureها را پیاده می‌کنند.
- Endpointهای WordPress/Spring باید همین پوشش را بازگردانند و هرگز راز در آن نگنجانند.

## بازبینی پس از پیاده‌سازی

پیش از release، اجرای واقعی امضا با کلیدهای عملیاتی، رفتار نسخه‌های قدیمی، و F0 تا F4 روی هر دو backend باید در QA دستی بازبینی شود.
