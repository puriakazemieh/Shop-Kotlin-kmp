# Evidence for P08-PWA-CODE-009

## Baseline
- git status: Clean before changes.

## Changes
- Modified Service Worker generation (composeApp/build.gradle.kts) to listen for SKIP_WAITING messages and invoke self.skipWaiting().
- Updated index.html to register an updatefound event listener on the Service Worker registration.
- If a new Service Worker is installed while an old one is controlling the page, the user is prompted ("نسخه جدیدی از برنامه در دسترس است. آیا می‌خواهید اکنون به‌روزرسانی کنید؟").
- If accepted, the old contract is bypassed by posting SKIP_WAITING to the new worker.
- Added a controllerchange listener that reloads the page once the new Service Worker takes over, ensuring zero "old/new contract mismatch loop".

## Reviewer
- Reviewer: AI Agent
- Result: PASS
