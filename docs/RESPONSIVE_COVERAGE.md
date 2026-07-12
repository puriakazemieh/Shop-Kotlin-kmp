# پوششِ ریسپانسیو — فهرستِ کاملِ همه‌ی فایل‌های Compose

این سند وضعیتِ **ریسپانسیو (چند‌اندازه‌ای: موبایل / تبلت / لپ‌تاپ / دسکتاپ / وب)** را برای **تک‌تکِ فایل‌های Compose** فهرست می‌کند — هم آن‌هایی که ریسپانسیو شدند، هم آن‌هایی که نیازِ مستقل نداشتند.

## راهنمای نشانه‌ها

| نشان | معنی |
|---|---|
| ✅ | مستقیماً ریسپانسیو شد — تکنیک ذکر شده |
| ➖ | نیازِ مستقل نداشت — دلیل ذکر شده |

## خلاصه‌ی آماری

- **کلِ فایل‌های Compose:** 153
- **دارای نشانِ ریسپانسیوِ مستقیم:** 80
- **صفحات (`*Screen.kt`):** 71 — **70** ریسپانسیو، **۱** (استوری) تمام‌صفحه‌ی عمدی
- **باتم‌شیت‌ها:** 5 — همه کپ‌وسط
- **دیالوگ‌ها:** 2 — یکی کپ‌وسط، بقیه خودکپِ فریم‌ورک
- **کامپوننت/کارت/بخش:** 75 — داخلِ والدِ ریسپانسیو رندر می‌شوند

## سازوکارِ ریسپانسیو (مرجع)

- **`WindowSizeClass`** (`core/designSystem/WindowSize.kt`): سه ردهٔ عرض با `BoxWithConstraints` — Compact `<۶۰۰dp` / Medium `۶۰۰–۸۴۰` / Expanded `≥۸۴۰` — بدونِ وابستگیِ اضافه، روی همه‌ی پلتفرم‌ها.
- **پوسته (`MainGraphScreen`+`SideNavRail`)**: نوارِ پایینِ موبایل ↔ نوارِ کناری روی صفحاتِ بزرگ.
- **`adaptiveGridColumns()`**: تعدادِ ستونِ گرید بر اساسِ عرض.
- **`responsiveMaxWidth()`**: کپ‌وسطِ محتوا روی دسکتاپ؛ روی موبایل no-op.

---

## `composeApp`  — 1/1 دارای نشانِ مستقیم

| فایل | وضعیت | تکنیک / دلیل |
|---|---|---|
| `App.kt` | ✅ | تزریقِ `ProvideWindowSizeClass` در ریشه‌ی اپ (منشأِ همه‌ی رفتارِ ریسپانسیو) |

## `core/navigation`  — 0/6 دارای نشانِ مستقیم

| فایل | وضعیت | تکنیک / دلیل |
|---|---|---|
| `AppNavigation.kt` | ➖ | میزبانِ ناوبری — چیدمانِ دیداریِ مستقل ندارد؛ صفحاتِ داخلش ریسپانسیو هستند |
| `HistoryHandler.android.kt` | ➖ | کامپوننت — داخلِ صفحه/والدِ ریسپانسیو رندر می‌شود؛ اندازه‌اش با شیارِ والد تعیین می‌شود |
| `HistoryHandler.ios.kt` | ➖ | کامپوننت — داخلِ صفحه/والدِ ریسپانسیو رندر می‌شود؛ اندازه‌اش با شیارِ والد تعیین می‌شود |
| `HistoryHandler.js.kt` | ➖ | کامپوننت — داخلِ صفحه/والدِ ریسپانسیو رندر می‌شود؛ اندازه‌اش با شیارِ والد تعیین می‌شود |
| `HistoryHandler.jvm.kt` | ➖ | کامپوننت — داخلِ صفحه/والدِ ریسپانسیو رندر می‌شود؛ اندازه‌اش با شیارِ والد تعیین می‌شود |
| `HistoryHandler.kt` | ➖ | کامپوننت — داخلِ صفحه/والدِ ریسپانسیو رندر می‌شود؛ اندازه‌اش با شیارِ والد تعیین می‌شود |

## `core/designSystem`  — 3/27 دارای نشانِ مستقیم

| فایل | وضعیت | تکنیک / دلیل |
|---|---|---|
| `AddressBottomSheet.kt` | ✅ | کپ‌وسط روی صفحاتِ بزرگ (`responsiveMaxWidth`) |
| `AlertTextField.kt` | ➖ | کامپوننت — داخلِ صفحه/والدِ ریسپانسیو رندر می‌شود؛ اندازه‌اش با شیارِ والد تعیین می‌شود |
| `AppLocale.android.kt` | ➖ | فایلِ غیرچیدمانی (تم / رنگ / فونت / توکن / منابع) — چیدمانِ بصری ندارد |
| `AppLocale.ios.kt` | ➖ | فایلِ غیرچیدمانی (تم / رنگ / فونت / توکن / منابع) — چیدمانِ بصری ندارد |
| `AppLocale.js.kt` | ➖ | فایلِ غیرچیدمانی (تم / رنگ / فونت / توکن / منابع) — چیدمانِ بصری ندارد |
| `AppLocale.jvm.kt` | ➖ | فایلِ غیرچیدمانی (تم / رنگ / فونت / توکن / منابع) — چیدمانِ بصری ندارد |
| `AppLocale.kt` | ➖ | فایلِ غیرچیدمانی (تم / رنگ / فونت / توکن / منابع) — چیدمانِ بصری ندارد |
| `AppTheme.kt` | ➖ | فایلِ غیرچیدمانی (تم / رنگ / فونت / توکن / منابع) — چیدمانِ بصری ندارد |
| `BlogContentRenderer.kt` | ➖ | کامپوننت — داخلِ صفحه/والدِ ریسپانسیو رندر می‌شود؛ اندازه‌اش با شیارِ والد تعیین می‌شود |
| `CarmillaBadge.kt` | ➖ | کامپوننت — داخلِ صفحه/والدِ ریسپانسیو رندر می‌شود؛ اندازه‌اش با شیارِ والد تعیین می‌شود |
| `CarmillaFilterChip.kt` | ➖ | کامپوننت — داخلِ صفحه/والدِ ریسپانسیو رندر می‌شود؛ اندازه‌اش با شیارِ والد تعیین می‌شود |
| `Colors.kt` | ➖ | فایلِ غیرچیدمانی (تم / رنگ / فونت / توکن / منابع) — چیدمانِ بصری ندارد |
| `CommonStringResource.kt` | ➖ | کامپوننت — داخلِ صفحه/والدِ ریسپانسیو رندر می‌شود؛ اندازه‌اش با شیارِ والد تعیین می‌شود |
| `ContentWithMessageBar.kt` | ➖ | کامپوننت — داخلِ صفحه/والدِ ریسپانسیو رندر می‌شود؛ اندازه‌اش با شیارِ والد تعیین می‌شود |
| `CustomTextField.kt` | ➖ | کامپوننت — داخلِ صفحه/والدِ ریسپانسیو رندر می‌شود؛ اندازه‌اش با شیارِ والد تعیین می‌شود |
| `DisplayResult.kt` | ➖ | کامپوننت — داخلِ صفحه/والدِ ریسپانسیو رندر می‌شود؛ اندازه‌اش با شیارِ والد تعیین می‌شود |
| `ErrorCard.kt` | ➖ | کامپوننت — داخلِ صفحه/والدِ ریسپانسیو رندر می‌شود؛ اندازه‌اش با شیارِ والد تعیین می‌شود |
| `Fonts.kt` | ➖ | فایلِ غیرچیدمانی (تم / رنگ / فونت / توکن / منابع) — چیدمانِ بصری ندارد |
| `InfoCard.kt` | ➖ | کامپوننت — داخلِ صفحه/والدِ ریسپانسیو رندر می‌شود؛ اندازه‌اش با شیارِ والد تعیین می‌شود |
| `LoadingCard.kt` | ➖ | کامپوننت — داخلِ صفحه/والدِ ریسپانسیو رندر می‌شود؛ اندازه‌اش با شیارِ والد تعیین می‌شود |
| `PrimaryButton.kt` | ➖ | کامپوننت — داخلِ صفحه/والدِ ریسپانسیو رندر می‌شود؛ اندازه‌اش با شیارِ والد تعیین می‌شود |
| `ProfileEditBottomSheet.kt` | ✅ | کپ‌وسط روی صفحاتِ بزرگ (`responsiveMaxWidth`) |
| `ProfileForm.kt` | ➖ | کامپوننت — داخلِ صفحه/والدِ ریسپانسیو رندر می‌شود؛ اندازه‌اش با شیارِ والد تعیین می‌شود |
| `QuantityCounter.kt` | ➖ | کامپوننت — داخلِ صفحه/والدِ ریسپانسیو رندر می‌شود؛ اندازه‌اش با شیارِ والد تعیین می‌شود |
| `StoryRing.kt` | ➖ | کامپوننت — داخلِ صفحه/والدِ ریسپانسیو رندر می‌شود؛ اندازه‌اش با شیارِ والد تعیین می‌شود |
| `Typography.kt` | ➖ | فایلِ غیرچیدمانی (تم / رنگ / فونت / توکن / منابع) — چیدمانِ بصری ندارد |
| `WindowSize.kt` | ✅ | **فایلِ زیرساخت** — کلِ سیستمِ ریسپانسیو (`WindowSizeClass` / `adaptiveGridColumns` / `responsiveMaxWidth` / `ResponsiveContainer`) اینجا تعریف شده |

## `feature/main`  — 3/6 دارای نشانِ مستقیم

| فایل | وضعیت | تکنیک / دلیل |
|---|---|---|
| `MainGraphScreen.kt` | ✅ | پوسته‌ی تطبیقی — نوارِ پایینِ موبایل ↔ نوارِ کناریِ `SideNavRail` روی صفحاتِ بزرگ |
| `MoreScreen.kt` | ✅ | کپ‌وسط روی صفحاتِ بزرگ (`responsiveMaxWidth`) |
| `BottomBar.kt` | ➖ | کامپوننت — داخلِ صفحه/والدِ ریسپانسیو رندر می‌شود؛ اندازه‌اش با شیارِ والد تعیین می‌شود |
| `DrawerItemCard.kt` | ➖ | کامپوننت — داخلِ صفحه/والدِ ریسپانسیو رندر می‌شود؛ اندازه‌اش با شیارِ والد تعیین می‌شود |
| `MainTopBar.kt` | ➖ | کامپوننت — داخلِ صفحه/والدِ ریسپانسیو رندر می‌شود؛ اندازه‌اش با شیارِ والد تعیین می‌شود |
| `SideNavRail.kt` | ✅ | نوارِ ناوبریِ کناری برای تبلت/دسکتاپ/وب |

## `feature/catalog`  — 8/19 دارای نشانِ مستقیم

| فایل | وضعیت | تکنیک / دلیل |
|---|---|---|
| `BundleDetailScreen.kt` | ✅ | کپ‌وسط روی صفحاتِ بزرگ (`responsiveMaxWidth`) |
| `BundleListScreen.kt` | ✅ | کپ‌وسط روی صفحاتِ بزرگ (`responsiveMaxWidth`) |
| `CategoriesScreen.kt` | ✅ | کپ‌وسط روی صفحاتِ بزرگ (`responsiveMaxWidth`) |
| `CategorySearchScreen.kt` | ✅ | گریدِ تطبیقی (۲ موبایل / ۳ تبلت / ۴ دسکتاپ)، کپ‌وسط روی صفحاتِ بزرگ (`responsiveMaxWidth`) |
| `ProductsOverviewScreen.kt` | ✅ | گریدِ تطبیقی (۲ موبایل / ۳ تبلت / ۴ دسکتاپ) |
| `SearchScreen.kt` | ✅ | گریدِ تطبیقی (۲ موبایل / ۳ تبلت / ۴ دسکتاپ) |
| `ShoppingAssistantScreen.kt` | ✅ | کپ‌وسط روی صفحاتِ بزرگ (`responsiveMaxWidth`) |
| `StoryDetailScreen.kt` | ➖ | تمام‌صفحه‌ی عمدی — نمایشگرِ استوری (نوارهای پیشرفت + نواحیِ لمسیِ گذر، مثلِ استوریِ اینستاگرام) |
| `CategoryCard.kt` | ➖ | کامپوننت — داخلِ صفحه/والدِ ریسپانسیو رندر می‌شود؛ اندازه‌اش با شیارِ والد تعیین می‌شود |
| `HomeSections.kt` | ✅ | گریدِ تطبیقی (۲ موبایل / ۳ تبلت / ۴ دسکتاپ) |
| `HomeVerticalSections.kt` | ➖ | کامپوننت — داخلِ صفحه/والدِ ریسپانسیو رندر می‌شود؛ اندازه‌اش با شیارِ والد تعیین می‌شود |
| `MainProductCard.kt` | ➖ | کامپوننت — داخلِ صفحه/والدِ ریسپانسیو رندر می‌شود؛ اندازه‌اش با شیارِ والد تعیین می‌شود |
| `ProductCard.kt` | ➖ | کامپوننت — داخلِ صفحه/والدِ ریسپانسیو رندر می‌شود؛ اندازه‌اش با شیارِ والد تعیین می‌شود |
| `StoryComponents.kt` | ➖ | کامپوننت — داخلِ صفحه/والدِ ریسپانسیو رندر می‌شود؛ اندازه‌اش با شیارِ والد تعیین می‌شود |
| `VideoPlayer.android.kt` | ➖ | پخش‌کننده/انتخابگرِ مدیای پلتفرمی — اندازه‌اش را از والد می‌گیرد |
| `VideoPlayer.ios.kt` | ➖ | پخش‌کننده/انتخابگرِ مدیای پلتفرمی — اندازه‌اش را از والد می‌گیرد |
| `VideoPlayer.js.kt` | ➖ | پخش‌کننده/انتخابگرِ مدیای پلتفرمی — اندازه‌اش را از والد می‌گیرد |
| `VideoPlayer.jvm.kt` | ➖ | پخش‌کننده/انتخابگرِ مدیای پلتفرمی — اندازه‌اش را از والد می‌گیرد |
| `VideoPlayer.kt` | ➖ | پخش‌کننده/انتخابگرِ مدیای پلتفرمی — اندازه‌اش را از والد می‌گیرد |

## `feature/details`  — 1/8 دارای نشانِ مستقیم

| فایل | وضعیت | تکنیک / دلیل |
|---|---|---|
| `DetailsScreen.kt` | ✅ | کپ‌وسط روی صفحاتِ بزرگ (`responsiveMaxWidth`) |
| `InteractionComponents.kt` | ➖ | کامپوننت — داخلِ صفحه/والدِ ریسپانسیو رندر می‌شود؛ اندازه‌اش با شیارِ والد تعیین می‌شود |
| `ProductReviewsSection.kt` | ➖ | کامپوننت — داخلِ صفحه/والدِ ریسپانسیو رندر می‌شود؛ اندازه‌اش با شیارِ والد تعیین می‌شود |
| `VariantChip.kt` | ➖ | کامپوننت — داخلِ صفحه/والدِ ریسپانسیو رندر می‌شود؛ اندازه‌اش با شیارِ والد تعیین می‌شود |
| `VideoPlayer.android.kt` | ➖ | پخش‌کننده/انتخابگرِ مدیای پلتفرمی — اندازه‌اش را از والد می‌گیرد |
| `VideoPlayer.ios.kt` | ➖ | پخش‌کننده/انتخابگرِ مدیای پلتفرمی — اندازه‌اش را از والد می‌گیرد |
| `VideoPlayer.jvm.kt` | ➖ | پخش‌کننده/انتخابگرِ مدیای پلتفرمی — اندازه‌اش را از والد می‌گیرد |
| `VideoPlayer.kt` | ➖ | پخش‌کننده/انتخابگرِ مدیای پلتفرمی — اندازه‌اش را از والد می‌گیرد |

## `feature/cart`  — 2/4 دارای نشانِ مستقیم

| فایل | وضعیت | تکنیک / دلیل |
|---|---|---|
| `CartScreen.kt` | ✅ | کپ‌وسط روی صفحاتِ بزرگ (`responsiveMaxWidth`) |
| `CheckoutScreen.kt` | ✅ | کپ‌وسط روی صفحاتِ بزرگ (`responsiveMaxWidth`) |
| `CartItemCard.kt` | ➖ | کامپوننت — داخلِ صفحه/والدِ ریسپانسیو رندر می‌شود؛ اندازه‌اش با شیارِ والد تعیین می‌شود |
| `PaymentCompleted.kt` | ➖ | کامپوننت — داخلِ صفحه/والدِ ریسپانسیو رندر می‌شود؛ اندازه‌اش با شیارِ والد تعیین می‌شود |

## `feature/profile`  — 6/6 دارای نشانِ مستقیم

| فایل | وضعیت | تکنیک / دلیل |
|---|---|---|
| `CustomerClubScreen.kt` | ✅ | کپ‌وسط روی صفحاتِ بزرگ (`responsiveMaxWidth`) |
| `FavoritesScreen.kt` | ✅ | گریدِ تطبیقی (۲ موبایل / ۳ تبلت / ۴ دسکتاپ) |
| `MembershipScreen.kt` | ✅ | کپ‌وسط روی صفحاتِ بزرگ (`responsiveMaxWidth`) |
| `ProfileScreen.kt` | ✅ | گریدِ تطبیقی (۲ موبایل / ۳ تبلت / ۴ دسکتاپ) |
| `ReferralScreen.kt` | ✅ | کپ‌وسط روی صفحاتِ بزرگ (`responsiveMaxWidth`) |
| `WalletScreen.kt` | ✅ | کپ‌وسط روی صفحاتِ بزرگ (`responsiveMaxWidth`) |

## `feature/orders`  — 5/5 دارای نشانِ مستقیم

| فایل | وضعیت | تکنیک / دلیل |
|---|---|---|
| `OrderDetailScreen.kt` | ✅ | کپ‌وسط روی صفحاتِ بزرگ (`responsiveMaxWidth`) |
| `OrderListScreen.kt` | ✅ | کپ‌وسط روی صفحاتِ بزرگ (`responsiveMaxWidth`) |
| `OrderTrackingScreen.kt` | ✅ | کپ‌وسط روی صفحاتِ بزرگ (`responsiveMaxWidth`) |
| `RecurringOrdersScreen.kt` | ✅ | کپ‌وسط روی صفحاتِ بزرگ (`responsiveMaxWidth`) |
| `ReturnRequestScreen.kt` | ✅ | کپ‌وسط روی صفحاتِ بزرگ (`responsiveMaxWidth`) |

## `feature/blog`  — 2/2 دارای نشانِ مستقیم

| فایل | وضعیت | تکنیک / دلیل |
|---|---|---|
| `BlogDetailScreen.kt` | ✅ | کپ‌وسط روی صفحاتِ بزرگ (`responsiveMaxWidth`) |
| `BlogListScreen.kt` | ✅ | کپ‌وسط روی صفحاتِ بزرگ (`responsiveMaxWidth`) |

## `feature/support`  — 1/1 دارای نشانِ مستقیم

| فایل | وضعیت | تکنیک / دلیل |
|---|---|---|
| `ContactUsScreen.kt` | ✅ | کپ‌وسط روی صفحاتِ بزرگ (`responsiveMaxWidth`) |

## `feature/auth`  — 4/7 دارای نشانِ مستقیم

| فایل | وضعیت | تکنیک / دلیل |
|---|---|---|
| `ForgotPasswordScreen.kt` | ✅ | کپ‌وسط روی صفحاتِ بزرگ (`responsiveMaxWidth`) |
| `LoginScreen.kt` | ✅ | کپ‌وسط روی صفحاتِ بزرگ (`responsiveMaxWidth`) |
| `RegisterScreen.kt` | ✅ | کپ‌وسط روی صفحاتِ بزرگ (`responsiveMaxWidth`) |
| `ResetPasswordScreen.kt` | ✅ | کپ‌وسط روی صفحاتِ بزرگ (`responsiveMaxWidth`) |
| `AuthBrandHeader.kt` | ➖ | کامپوننت — داخلِ صفحه/والدِ ریسپانسیو رندر می‌شود؛ اندازه‌اش با شیارِ والد تعیین می‌شود |
| `AuthButton.kt` | ➖ | کامپوننت — داخلِ صفحه/والدِ ریسپانسیو رندر می‌شود؛ اندازه‌اش با شیارِ والد تعیین می‌شود |
| `AuthTextField.kt` | ➖ | کامپوننت — داخلِ صفحه/والدِ ریسپانسیو رندر می‌شود؛ اندازه‌اش با شیارِ والد تعیین می‌شود |

## `feature/settings`  — 1/1 دارای نشانِ مستقیم

| فایل | وضعیت | تکنیک / دلیل |
|---|---|---|
| `SettingsScreen.kt` | ✅ | کپ‌وسط روی صفحاتِ بزرگ (`responsiveMaxWidth`) |

## `feature/academy`  — 10/10 دارای نشانِ مستقیم

| فایل | وضعیت | تکنیک / دلیل |
|---|---|---|
| `CertificateVerifyScreen.kt` | ✅ | کپ‌وسط روی صفحاتِ بزرگ (`responsiveMaxWidth`) |
| `CertificatesScreen.kt` | ✅ | کپ‌وسط روی صفحاتِ بزرگ (`responsiveMaxWidth`) |
| `CourseDetailScreen.kt` | ✅ | کپ‌وسط روی صفحاتِ بزرگ (`responsiveMaxWidth`) |
| `CourseLearnScreen.kt` | ✅ | کپ‌وسط روی صفحاتِ بزرگ (`responsiveMaxWidth`) |
| `CourseListScreen.kt` | ✅ | کپ‌وسط روی صفحاتِ بزرگ (`responsiveMaxWidth`) |
| `CourseQuizScreen.kt` | ✅ | کپ‌وسط روی صفحاتِ بزرگ (`responsiveMaxWidth`) |
| `LessonQuizScreen.kt` | ✅ | کپ‌وسط روی صفحاتِ بزرگ (`responsiveMaxWidth`) |
| `PeerReviewScreen.kt` | ✅ | کپ‌وسط روی صفحاتِ بزرگ (`responsiveMaxWidth`) |
| `PlacementQuizScreen.kt` | ✅ | کپ‌وسط روی صفحاتِ بزرگ (`responsiveMaxWidth`) |
| `ProjectSubmissionScreen.kt` | ✅ | کپ‌وسط روی صفحاتِ بزرگ (`responsiveMaxWidth`) |

## `feature/clinic`  — 10/10 دارای نشانِ مستقیم

| فایل | وضعیت | تکنیک / دلیل |
|---|---|---|
| `EmergencyResourcesScreen.kt` | ✅ | کپ‌وسط روی صفحاتِ بزرگ (`responsiveMaxWidth`) |
| `HomeworkScreen.kt` | ✅ | کپ‌وسط روی صفحاتِ بزرگ (`responsiveMaxWidth`) |
| `JournalScreen.kt` | ✅ | کپ‌وسط روی صفحاتِ بزرگ (`responsiveMaxWidth`) |
| `MessagingScreen.kt` | ✅ | کپ‌وسط روی صفحاتِ بزرگ (`responsiveMaxWidth`) |
| `MoodCheckInScreen.kt` | ✅ | کپ‌وسط روی صفحاتِ بزرگ (`responsiveMaxWidth`) |
| `MyAppointmentsScreen.kt` | ✅ | کپ‌وسط روی صفحاتِ بزرگ (`responsiveMaxWidth`) |
| `SessionReceiptScreen.kt` | ✅ | کپ‌وسط روی صفحاتِ بزرگ (`responsiveMaxWidth`) |
| `TherapistDetailScreen.kt` | ✅ | کپ‌وسط روی صفحاتِ بزرگ (`responsiveMaxWidth`) |
| `TherapistListScreen.kt` | ✅ | کپ‌وسط روی صفحاتِ بزرگ (`responsiveMaxWidth`) |
| `TherapistMatchScreen.kt` | ✅ | کپ‌وسط روی صفحاتِ بزرگ (`responsiveMaxWidth`) |

## `feature/psychtest`  — 2/2 دارای نشانِ مستقیم

| فایل | وضعیت | تکنیک / دلیل |
|---|---|---|
| `PsychTestListScreen.kt` | ✅ | کپ‌وسط روی صفحاتِ بزرگ (`responsiveMaxWidth`) |
| `TakeTestScreen.kt` | ✅ | کپ‌وسط روی صفحاتِ بزرگ (`responsiveMaxWidth`) |

## `feature/comparison`  — 1/1 دارای نشانِ مستقیم

| فایل | وضعیت | تکنیک / دلیل |
|---|---|---|
| `ComparisonScreen.kt` | ✅ | اسکرولِ افقی (جدولِ مقایسه — موبایل اسکرول، دسکتاپ ستون‌ها کنارِ هم) |

## `feature/admin/products`  — 10/17 دارای نشانِ مستقیم

| فایل | وضعیت | تکنیک / دلیل |
|---|---|---|
| `AdminBundlesScreen.kt` | ✅ | کپ‌وسط روی صفحاتِ بزرگ (`responsiveMaxWidth`) |
| `AdminDiscountsScreen.kt` | ✅ | کپ‌وسط روی صفحاتِ بزرگ (`responsiveMaxWidth`) |
| `AdminInteractionsScreen.kt` | ✅ | کپ‌وسط روی صفحاتِ بزرگ (`responsiveMaxWidth`) |
| `AdminPanelScreen.kt` | ✅ | کپ‌وسط روی صفحاتِ بزرگ (`responsiveMaxWidth`) |
| `AdminStoryScreen.kt` | ✅ | کپ‌وسط روی صفحاتِ بزرگ (`responsiveMaxWidth`) |
| `ManageProductScreen.kt` | ✅ | کپ‌وسط روی صفحاتِ بزرگ (`responsiveMaxWidth`) |
| `AdminProductCard.kt` | ➖ | کامپوننت — داخلِ صفحه/والدِ ریسپانسیو رندر می‌شود؛ اندازه‌اش با شیارِ والد تعیین می‌شود |
| `BulkVariantBottomSheet.kt` | ✅ | کپ‌وسط روی صفحاتِ بزرگ (`responsiveMaxWidth`) |
| `CategoriesBottomSheet.kt` | ✅ | کپ‌وسط روی صفحاتِ بزرگ (`responsiveMaxWidth`) |
| `CreateCategoryDialog.kt` | ➖ | دیالوگِ `AlertDialog`/`Dialog` — فریم‌ورک با عرضِ پیش‌فرضِ پلتفرم خودش مرکزی/محدودش می‌کند |
| `CreateDiscountDialog.kt` | ✅ | کپ‌وسط روی صفحاتِ بزرگ (`responsiveMaxWidth`) |
| `MediaPicker.android.kt` | ➖ | پخش‌کننده/انتخابگرِ مدیای پلتفرمی — اندازه‌اش را از والد می‌گیرد |
| `MediaPicker.ios.kt` | ➖ | پخش‌کننده/انتخابگرِ مدیای پلتفرمی — اندازه‌اش را از والد می‌گیرد |
| `MediaPicker.js.kt` | ➖ | پخش‌کننده/انتخابگرِ مدیای پلتفرمی — اندازه‌اش را از والد می‌گیرد |
| `MediaPicker.jvm.kt` | ➖ | پخش‌کننده/انتخابگرِ مدیای پلتفرمی — اندازه‌اش را از والد می‌گیرد |
| `MediaPicker.kt` | ➖ | پخش‌کننده/انتخابگرِ مدیای پلتفرمی — اندازه‌اش را از والد می‌گیرد |
| `VariantBottomSheet.kt` | ✅ | کپ‌وسط روی صفحاتِ بزرگ (`responsiveMaxWidth`) |

## `feature/admin/orders`  — 2/2 دارای نشانِ مستقیم

| فایل | وضعیت | تکنیک / دلیل |
|---|---|---|
| `AdminOrderScreen.kt` | ✅ | کپ‌وسط روی صفحاتِ بزرگ (`responsiveMaxWidth`) |
| `AdminReturnRequestsScreen.kt` | ✅ | کپ‌وسط روی صفحاتِ بزرگ (`responsiveMaxWidth`) |

## `feature/admin/options`  — 1/1 دارای نشانِ مستقیم

| فایل | وضعیت | تکنیک / دلیل |
|---|---|---|
| `ManageOptionsScreen.kt` | ✅ | کپ‌وسط روی صفحاتِ بزرگ (`responsiveMaxWidth`) |

## `feature/admin/wallet`  — 2/2 دارای نشانِ مستقیم

| فایل | وضعیت | تکنیک / دلیل |
|---|---|---|
| `AdminWalletScreen.kt` | ✅ | کپ‌وسط روی صفحاتِ بزرگ (`responsiveMaxWidth`) |
| `AdminWithdrawalsScreen.kt` | ✅ | کپ‌وسط روی صفحاتِ بزرگ (`responsiveMaxWidth`) |

## `feature/admin/blog`  — 2/7 دارای نشانِ مستقیم

| فایل | وضعیت | تکنیک / دلیل |
|---|---|---|
| `AdminBlogListScreen.kt` | ✅ | کپ‌وسط روی صفحاتِ بزرگ (`responsiveMaxWidth`) |
| `ManageBlogScreen.kt` | ✅ | کپ‌وسط روی صفحاتِ بزرگ (`responsiveMaxWidth`) |
| `MediaPicker.android.kt` | ➖ | پخش‌کننده/انتخابگرِ مدیای پلتفرمی — اندازه‌اش را از والد می‌گیرد |
| `MediaPicker.ios.kt` | ➖ | پخش‌کننده/انتخابگرِ مدیای پلتفرمی — اندازه‌اش را از والد می‌گیرد |
| `MediaPicker.js.kt` | ➖ | پخش‌کننده/انتخابگرِ مدیای پلتفرمی — اندازه‌اش را از والد می‌گیرد |
| `MediaPicker.jvm.kt` | ➖ | پخش‌کننده/انتخابگرِ مدیای پلتفرمی — اندازه‌اش را از والد می‌گیرد |
| `MediaPicker.kt` | ➖ | پخش‌کننده/انتخابگرِ مدیای پلتفرمی — اندازه‌اش را از والد می‌گیرد |

## `feature/admin/academy`  — 1/6 دارای نشانِ مستقیم

| فایل | وضعیت | تکنیک / دلیل |
|---|---|---|
| `AdminAcademyScreen.kt` | ✅ | کپ‌وسط روی صفحاتِ بزرگ (`responsiveMaxWidth`) |
| `MediaPicker.android.kt` | ➖ | پخش‌کننده/انتخابگرِ مدیای پلتفرمی — اندازه‌اش را از والد می‌گیرد |
| `MediaPicker.ios.kt` | ➖ | پخش‌کننده/انتخابگرِ مدیای پلتفرمی — اندازه‌اش را از والد می‌گیرد |
| `MediaPicker.js.kt` | ➖ | پخش‌کننده/انتخابگرِ مدیای پلتفرمی — اندازه‌اش را از والد می‌گیرد |
| `MediaPicker.jvm.kt` | ➖ | پخش‌کننده/انتخابگرِ مدیای پلتفرمی — اندازه‌اش را از والد می‌گیرد |
| `MediaPicker.kt` | ➖ | پخش‌کننده/انتخابگرِ مدیای پلتفرمی — اندازه‌اش را از والد می‌گیرد |

## `feature/admin/clinic`  — 1/1 دارای نشانِ مستقیم

| فایل | وضعیت | تکنیک / دلیل |
|---|---|---|
| `AdminClinicScreen.kt` | ✅ | کپ‌وسط روی صفحاتِ بزرگ (`responsiveMaxWidth`) |

## `feature/admin/psychtest`  — 1/1 دارای نشانِ مستقیم

| فایل | وضعیت | تکنیک / دلیل |
|---|---|---|
| `AdminPsychTestScreen.kt` | ✅ | کپ‌وسط روی صفحاتِ بزرگ (`responsiveMaxWidth`) |

