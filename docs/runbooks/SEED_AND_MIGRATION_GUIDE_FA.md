# راهنمای ساخت Seed Pack و مهاجرت داده‌ها (Carmilla Runbook)

این سند برای مدیران و توسعه‌دهندگان سیستم طراحی شده است تا نحوه ساخت، مدیریت، و دیباگ سیستم Seed (داده‌های دمو/اولیه) و Migration (انتقال مشتریان واقعی) را آموزش دهد. این سند مکمل ADR-011 است.

## ۱. ساخت Seed Packها (Pack Authoring)

سیستم Seed فقط برای محصولات **مستقل** (مانند روانشناسی، پزشکی، فروشگاهی) و تولید محتوای نمایشی (Demo Data) استفاده می‌شود.

### قوانین نگارش
- **هیچ** داده‌ای از کاربران، پرداخت‌ها یا بیماران واقعی در این فایل‌ها قرار نگیرد.
- نام فایل باید نسخه داشته باشد (مثلاً clinic-public-fa-v1.json).
- تمامی Seed Packها در پوشه wordpress/carmilla-bridge/seeds/ نگهداری می‌شوند.

### ساختار فایل (JSON)
\\\json
{
  "version": "1.0",
  "name": "clinic-public-fa",
  "type": "carmilla_seed_pack",
  "dependencies": ["carmilla-core"],
  "content": {
    "posts": [
      {
        "post_type": "page",
        "post_title": "رزرو نوبت پزشک",
        "post_content": "[carmilla_clinic_booking]",
        "meta": {
          "_carmilla_sku": "clinic_booking_base"
        }
      }
    ],
    "options": {
      "carmilla_clinic_active": "yes"
    }
  },
  "includes": []
}
\\\

---

## ۲. لایسنس و دسترسی‌ها (SKU Binding)

برای اینکه یک Seed Pack روی سایت نصب شود، باید در بخش meta هر پست، کلید _carmilla_sku مشخص شود.
هنگام اجرای Import، سیستم بررسی می‌کند که آیا لایسنس مربوط به آن SKU برای این دامنه/مشتری خریداری شده است یا خیر (از طریق سرور مرکزی یا توکن لایسنس محلی).

---

## ۳. مهاجرت مشتریان قدیمی (Migration Runbook)

### پیش‌نیازها
- تهیه فول بکاپ از دیتابیس سایت قدیمی (Legacy).
- توکن لایسنس (Customer UUID) مشتری.

### مرحله ۱: خروجی گرفتن (Export) از سرور قدیمی
وارد ترمینال سرور قدیمی شوید و دستور زیر را اجرا کنید:
\\\ash
wp carmilla export-legacy --encryption_key="<SECRET_32_BYTES>" --customer_uuid="<UUID>"
\\\
- سیستم به‌صورت خودکار داده‌های مالی، پسوردها و اطلاعات سلامت را (در صورت وجود) فیلتر می‌کند.
- فایل خروجی در wp-content/uploads/carmilla-export/posts.enc ذخیره می‌شود.

### مرحله ۲: استخراج فایل‌های شخصی‌سازی شده (Overlay)
اگر مشتری قبلاً از Seed استفاده می‌کرده اما محتوای صفحات را ویرایش کرده است:
\\\ash
wp carmilla export-overlay
\\\

### مرحله ۳: وارد کردن داده‌ها (Import) به سرور جدید
وارد ترمینال سایت جدید (Carmilla) شوید:
\\\ash
wp carmilla import-legacy --encryption_key="<SECRET_32_BYTES>" --site_uuid="<UUID>" --legacy_domain="old-site.com" --new_domain="new-site.com" --consent=yes
\\\
> **هشدار امنیتی:** وارد کردن --consent=yes الزامی است. این تأیید می‌کند که شما با مالکیت معنوی این داده‌ها موافقت کرده و بکاپ گرفته‌اید.

---

## ۴. خطایابی (Troubleshooting)

| خطا | علت و راه‌حل |
|-----|-------------|
| \Encryption failed\ | طول \encryption_key\ دقیقاً 32 بایت نیست. کلید را اصلاح کنید. |
| \Wrong customer binding\ | شما تلاش می‌کنید دیتای مشتری A را در سایت مشتری B وارد کنید. \customer_uuid\ هنگام Export با \site_uuid\ هنگام Import یکی نیست. |
| \Migration package has expired\ | از زمان ایجاد فایل Export بیش از ۲۴ ساعت گذشته است. مجدداً از سرور قدیمی Export بگیرید. |
| \Import skipped (Delta)\ | سیستم متوجه شده که محتوای این رکورد تغییری نکرده است و آن را نادیده گرفته تا سرعت بالا برود. این خطا نیست. |
