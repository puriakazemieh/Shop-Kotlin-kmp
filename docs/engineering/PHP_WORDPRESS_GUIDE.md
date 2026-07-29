# PHP & WordPress Development Guide | راهنمای توسعه وردپرس و PHP

This guide explains how to use the Docker-based environment for PHP and WordPress testing within the Carmilla project.
این راهنما نحوه استفاده از محیط مبتنی بر Docker برای تست PHP و وردپرس در پروژه کارمیلا را توضیح می‌دهد.

---

## English Version

### Prerequisites
- Install **Docker Desktop** on Windows.
- Ensure the Docker daemon is running.

### Environment Configuration
The configuration is located at `tools/test-env/docker-compose.yml`. It defines three services:
1. **php-test**: For running PHP CLI commands (linting, scripts).
2. **wordpress-test**: A live WordPress instance mapped to your local source code.
3. **db**: A MariaDB database for WordPress.

### 1. Running PHP CLI Commands
To check for syntax errors (linting) in a PHP file without installing PHP locally:
```powershell
docker-compose -f tools/test-env/docker-compose.yml run --rm php-test php -l path/to/your/file.php
```

### 2. Running the Full WordPress Site
To start the WordPress site connected to your local `carmilla-theme` and `carmilla-bridge`:
```powershell
docker-compose -f tools/test-env/docker-compose.yml up -d wordpress-test db
```
- **Access**: The site will be available at `http://localhost:8080`.
- **Live Sync**: Any changes you make in the `wordpress/` directory are immediately reflected in the container.

### 3. Stopping the Environment
```powershell
docker-compose -f tools/test-env/docker-compose.yml down
```

---

## نسخه فارسی

### پیش‌نیازها
- نصب برنامه **Docker Desktop** روی ویندوز.
- اطمینان از اینکه سرویس Docker در حال اجرا (Running) است.

### تنظیمات محیط
تنظیمات در فایل `tools/test-env/docker-compose.yml` قرار دارد و شامل سه سرویس است:
۱. **php-test**: برای اجرای دستورات خط فرمان PHP (مانند بررسی خطاهای نگارشی).
۲. **wordpress-test**: یک نمونه زنده وردپرس که به کد منبع محلی شما متصل است.
۳. **db**: دیتابیس MariaDB برای وردپرس.

### ۱. اجرای دستورات PHP (بررسی خطا)
برای چک کردن فایل‌های PHP بدون نیاز به نصب PHP روی سیستم خودتان:
```powershell
docker-compose -f tools/test-env/docker-compose.yml run --rm php-test php -l path/to/your/file.php
```

### ۲. اجرای سایت وردپرس بصورت کامل
برای بالا آوردن سایت وردپرس متصل به پوسته (`carmilla-theme`) و افزونه (`carmilla-bridge`) پروژه:
```powershell
docker-compose -f tools/test-env/docker-compose.yml up -d wordpress-test db
```
- **دسترسی**: سایت از طریق آدرس `http://localhost:8080` در دسترس خواهد بود.
- **همگام‌سازی زنده**: هر تغییری در پوشه `wordpress/` بدهید، بلافاصله در سایت داخل کانتینر اعمال می‌شود.

### ۳. متوقف کردن محیط
```powershell
docker-compose -f tools/test-env/docker-compose.yml down
```
