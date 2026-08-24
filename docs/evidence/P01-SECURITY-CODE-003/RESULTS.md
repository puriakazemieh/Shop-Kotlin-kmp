# P01-SECURITY-CODE-003 — Evidence

- Scope/size: M؛ logging مشترک KMP و client HTTP.
- تغییر: Ktor client با `LogLevel.NONE` اجرا می‌شود؛ body/status/exception raw از `ResultHandler` حذف شد و helperهای raw logging خروجی تولید نمی‌کنند.
- Test: `:core:network:jvmTest` با exit code 0؛ `ClientLoggingPolicyTest` تأیید می‌کند HTTP logging غیرفعال است.
- Baseline: `:composeApp:compileKotlinJvm :composeApp:compileKotlinJs` با exit code 1، به علت خطای مستقل `WalletBalanceResponse` در `core:data`؛ این تغییر آن فایل‌ها را لمس نکرده است.
- Manual QA: اجرا نشده. روی Android یا Web با credential و OTP کاملاً synthetic، login و یک درخواست ناموفق اجرا شود؛ Logcat/console نباید Authorization، token، OTP، body پرداخت یا health field نشان دهد.
- Rollback: بازگرداندن فقط فایل‌های این Task، اما این کار logging حساس را دوباره فعال می‌کند.
