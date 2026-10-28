# P01-SECURITY-CODE-005C — Web session-cookie contract

- Status: DONE
- Owner: BOTH
- Depends on: P01-SECURITY-CODE-005B
- Goal: حذف persistent Bearer token وب و تعریف session cookie با HttpOnly, Secure, SameSite؛ نیازمند تغییر قرارداد backend.

## Implemented contract

- وب از `POST /api/auth/web/login`،`/register`،`/login-with-otp`،`/refresh` و `/logout` استفاده می‌کند؛قرارداد mobile در `/api/auth/*` بدون تغییر مانده است.
- پاسخ web فقط `accessToken` کوتاه‌عمر و `user` دارد؛refresh token در body،Settings،localStorage یا sessionStorage قرار نمی‌گیرد.
- سرور cookie با نام `__Host-shop_refresh`،ویژگی‌های `HttpOnly`،`Secure`،`SameSite=Strict`،`Path=/` و بدون `Domain` صادر و هنگام logout منقضی می‌کند.
- Ktor JS درخواست‌ها را با credential مرورگر ارسال می‌کند و Settings وب کاملاً in-memory است.

## Manual QA required

1. ShopServer را پشت HTTPS با `WEB_SESSION_COOKIE_SECURE=true` اجرا کنید و `app.cors-origins` را دقیقاً برابر origin وب واقعی قرار دهید.
2. در مرورگر،صفحهٔ وب را باز و login کنید. در DevTools > Application > Cookies فقط cookie `__Host-shop_refresh` را بررسی کنید؛نباید مقدار refresh token در response JSON،Local Storage یا Session Storage دیده شود.
3. یک درخواست authenticated اجرا کنید،سپس access token را منقضی کنید یا بعد از ۱۵ دقیقه refresh را بررسی کنید؛درخواست `POST /api/auth/web/refresh` باید cookie را rotate و درخواست اصلی را دوباره موفق کند.
4. logout کنید؛cookie باید حذف شود و refresh قبلی دیگر نباید access token بدهد.

Success: cookie دارای HttpOnly/Secure/SameSite=Strict است،token در browser storage وجود ندارد،rotation و logout مطابق انتظار عمل می‌کنند. Evidence قرمز‌شده از DevTools و build fingerprint ثبت شود.

## Completion record

- Completed at: 2026-10-28
- Disposition: مالک پروژه تأیید کرد که QA مرورگرِ وابسته به HTTPS در محیط محلی انجام‌پذیر نیست و باید در `P10-RELEASE-SEC-021`، روی staging/release HTTPS واقعی، انجام شود. این DONE فقط پایان scope فاز ۰۱ است و اجرای موفق آزمون HTTPS را ادعا نمی‌کند.
- Evidence: `docs/evidence/P01-SECURITY-CODE-005C/deferral-decision-20261028.md`
- Remaining control: کارت P10 باید ویژگی‌های cookie، نبود refresh token در browser storage، rotation و invalidation پس از logout را همراه آزمون TLS و host binding ثبت کند.
- Final status: DONE (routing to P10 Web/PWA HTTPS session-cookie validation)
