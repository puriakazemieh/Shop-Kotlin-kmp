# سیاست پشتیبان‌گیری و بازیابی (Backup & Restore Policy)

> نسخه سند: ۱.۰  
> تاریخ: ۵ اوت ۲۰۲۶  
> وضعیت: پیش‌نویس (Task P00-PROGRAM-OPS-012)

هدف این سند تضمین پایداری داده‌ها و قابلیت بازیابی سیستم در صورت بروز حادثه در محیط‌های توسعه، Staging و Production است.

---

## ۱. محدوده پشتیبان‌گیری (Backup Scope)

| نوع داده | منبع | روش | فرکانس |
|---|---|---|---|
| **دیتابیس وردپرس** | MariaDB/MySQL | `mysqldump` | روزانه (تولید) / قبل از هر Migration |
| **فایل‌های وردپرس** | `wp-content/uploads` | File Sync / Restic | روزانه |
| **دیتابیس اسپرینگ** | PostgreSQL | `pg_dump` | روزانه |
| **Artifactها** | ZIPs, AAB, APK | GitHub Releases / S3 | پس از هر بیلد موفق Release |
| **مستندات و شواهد**| `docs/evidence/` | Git Versioning | بلافاصله پس از تکمیل تسک |

---

## ۲. محل نگهداری (Storage Locations)

- **Evidence & Logs**: شاخه `docs/evidence/` در مخزن اصلی Git.
- **Production Backups**: سرویس‌های Object Storage (مانند S3 یا Liara Storage) با قابلیت ورژن‌بندی و رمزنگاری در حالت استراحت (At Rest).
- **Staging Backups**: نگهداری محلی در کانتینر Docker و کپی روی درایو شبکه ایزوله.

---

## ۳. رویه بازیابی (Restore Procedure)

### ۳.۱ بازیابی وردپرس (Docker)
1. توقف سرویس: `docker-compose down`
2. پاکسازی Volume فعلی: `docker volume rm test-env_db_data` (در صورت نیاز به ریست کامل)
3. جایگزینی فایل SQL در پوشه `db/init`.
4. اجرای مجدد: `docker-compose up -d`

### ۳.۲ بازیابی اسپرینگ
1. توقف اپلیکیشن.
2. اجرای دستور `psql -f backup_file.sql`.
3. بررسی صحت سلامت (Health Check) اندپوینت `/actuator/health`.

---

## ۴. شواهد و Artifactها (Artifact Policy)
- تمامی شواهد اجرای تسک‌ها باید در مسیر `docs/evidence/<TASK-ID>/` قرار گیرند.
- هر Artifact خروجی (مانند فایل ZIP افزونه) باید دارای Checksum (SHA-256) باشد تا اصالت آن قابل ردیابی باشد.

---

## ۵. تست بازیابی آزمایشی (Restore Drill)
تیم فنی موظف است هر **۳۰ روز یک‌بار**، یکی از بک‌آپ‌های محیط Staging را روی یک محیط ایزوله بازیابی کرده و صحت داده‌های فروشگاه و حساب‌های کاربری را تأیید کند.
- **معیار موفقیت**: لاگین موفق `test_customer` و مشاهده آخرین سفارشات ثبت شده قبل از بک‌آپ.
