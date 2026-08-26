# فرمان‌ها و نتیجه‌ها

## baseline

```text
docker run --rm -v "${PWD}:/app" -w /app/wordpress/carmilla-bridge php:8.1-cli php tests/smoke-manifest.php
exit code: 0
ALL PASSED
```

## characterization پیش از پیاده‌سازی

```text
docker run --rm -v "${PWD}:/app" -w /app/wordpress/carmilla-bridge php:8.1-cli php tests/smoke-manifest.php
exit code: 255
Fatal error: Call to undefined method CB_Manifest_Controller::dependency_violations()
```

## verification نهایی

```text
docker run --rm -v "${PWD}:/app" -w /app/wordpress/carmilla-bridge php:8.1-cli sh -lc "php -l includes/class-cb-manifest-controller.php && php -l includes/class-cb-plugin.php && php tests/smoke-manifest.php"
exit code: 0
No syntax errors detected in includes/class-cb-manifest-controller.php
No syntax errors detected in includes/class-cb-plugin.php
ALL PASSED
```
