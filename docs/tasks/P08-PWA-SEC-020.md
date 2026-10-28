# P08-PWA-SEC-020 — نتیجهٔ پرداخت Web/PWA فقط با query authoritative

> پیگیری چک‌باکس‌های فازهای قبل: پس از تکمیل کنترل‌های مرتبط این کارت و ثبت Evidence و تأیید لازم، طبق نگاشت `docs/evidence/P01-PLATFORM-SCOPE-TRANSFER-20261028/summary.md` نتیجه را به چک‌باکس متناظر راهنمای تست دستی و Gate در Master بازگردانید. چک‌باکس مشترک فقط با تکمیل تمام بخش‌های مربوط به همهٔ مقصدها تیک می‌خورد؛ Evidence جزئی را ثبت کنید.

- Status: TODO
- Phase/Area/Type: P08 / PWA / SEC
- Priority/Risk/Size: P0/HIGH / M
- Owner: BOTH
- Completion authority: BOTH
- Depends on: P08-PWA-GATE-019
- Requirement source: انتقال `P01-PAYMENT-CODE-007` در ۲۰۲۶-۱۰-۲۸

## هدف

URI یا query parameter نتیجهٔ پرداخت فقط trigger است. Web/PWA نباید `success`،`status`،amount یا cart را از URL trusted بداند؛ فقط reference opaque را برای query سمت server می‌پذیرد.

## معماری مشترک

parse/validation قرارداد payment-result و state-machine نتیجه باید در KMP `core/**`/feature مشترک باشد؛ adapter Web/PWA فقط browser URL،history و lifecycle را به آن منطق وصل می‌کند. نسخهٔ جداگانهٔ business rule برای مرورگر ساخته نمی‌شود.

## پذیرش

- `status=success` جعلی بدون پاسخ authoritative، status سفارش یا cart را تغییر نمی‌دهد.
- نتیجهٔ موفق فقط پس از verify/query معتبر نمایش داده و cart فقط طبق پاسخ authoritative تغییر می‌کند.
- cold/warm/reload و browser fallback با reference معتبر و نامعتبر آزموده می‌شوند.
- Evidence قرمز‌شده و آزمون دستی مرورگر روی build مشخص ثبت می‌شود.

## محدوده

- `composeApp/**`, `core/**`, `feature/**`, `docs/**`
- تغییر gateway/provider یا پرداخت واقعی خارج از محدوده است.
