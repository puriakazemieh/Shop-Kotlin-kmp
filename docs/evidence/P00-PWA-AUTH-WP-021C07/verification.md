# P00-PWA-AUTH-WP-021C07 — یکپارچه‌سازی routeهای محافظت‌شده

تاریخ: 2026-10-26

این کارت والد، فقط پس از تکمیل و بازبینی مستقل کارت‌های split زیر بسته شد:

| کارت | پذیرش اثبات‌شده |
|---|---|
| C07A | activation fail-closed، چهار route نشست و سازگاری مستقل mobile |
| C07B | سه route بازیابی با عدم نشت credential و آزمون WordPress disposable |
| C07C | ماتریس HTTP واقعی روی fixture مستقل، کوکی/CORS/no-store، منفی‌های origin و owner QA |

آزمون C07C شامل ورود، ثبت‌نام و conflict هویت، refresh/rotation، logout و revoke، recovery ناشناس، reset-token و OTP نامعتبر، origin نامعتبر و OPTIONS بود. Docker fixture route-activation، 16/16 PASS و policy fixture، 93/93 PASS ثبت کرد. مالک در 2026-10-26 تمام گام‌های QA مرورگر C07C را تأیید کرد.

محدودهٔ قبول‌شده فقط backend WordPress است. هیچ سایت نگهداری‌شده یا محیط production تغییر نکرد. اتصال lifecycle سمت PWA، reload/refresh UI و آزمون end-to-end هنوز عمداً خارج از این کارت و متعلق به `P00-PWA-AUTH-WEB-022` و `P00-PWA-AUTH-QA-023` است.
