# P12-CLIENT-HOST-ADAPTER-025 — اتصال کلاینت Kotlin به Theme/Plugin WordPress یا Spring

- Status: TODO
- Phase/Area/Type: P12 / CLIENT-CONTRACT / CODE
- Priority/Risk/Size: P0/HIGH / HIGH / M
- Owner: BOTH
- Completion authority: BOTH
- Depends on: P12-FEATURE-LIFECYCLE-024, P12-BUILDER-CODE-012
- Blocks: P12-SECURITY-SEC-017
- Requirement source: درخواست مالک محصول 2026-10-13؛ تکمیل P03 BackendProfile،P12 compatibility matrix و P15 Spring Gate.

## هدف قابل اندازه‌گیری

یک artifact Kotlin (PWA/Android و سپس iOS/Desktop در Gateهای خود) فقط با BuildSpec trusted به یکی از این provider modeها متصل شود: `WORDPRESS_THEME_ONLY`،`WORDPRESS_PLUGIN_ONLY`،`WORDPRESS_CO_INSTALL` یا `SPRING`. Theme و Plugin باید همان قرارداد canonical REST/manifest را مستقل عرضه کنند؛ Spring قرارداد هم‌معنا را پس از P15 اثبات می‌کند.

## خروجی و معیار پذیرش

- BackendProfile (`WORDPRESS`/`SPRING`) از host/provider mode جدا بماند؛ انتخاب برند یا query آزاد origin/provider را تغییر ندهد.
- Theme-only و Plugin-only داده‌های catalog/content/cart/feature-manifest را بدون نصب محصول دیگر به کلاینت بدهند؛ co-install duplicate write یا divergent state ایجاد نکند.
- client sync/mapper برای قرارداد canonical، cache/token namespace، auth/logout، feature-off و نسخه قرارداد tests منفی داشته باشد.
- matrix واقعی شامل PWA و Android است؛ iOS/Desktop به کارت‌های P16/P17 ارجاع صریح دارند. Spring فقط پس از P15-Gate به عنوان backend واقعی PASS می‌شود؛ fixture کافی نیست.

## محدوده و منع‌ها

- مجاز: `core/**`،`composeApp/**`،`androidApp/**`،`wordpress/carmilla-theme/**`،`wordpress/carmilla-bridge/**`،`wordpress/packages/carmilla-core/**`،`D:/Android/AndroidStudioProjects/ShopServer/Shop/**` فقط برای contract،`tools/**` و `docs/**`.
- ممنوع: backend سوم، sync دوطرفه بدون ADR/ownership، secret در client/artifact، یا تغییر داده مشتری/Production.

## Verification

- contract tests و integration matrix با fixture synthetic: Theme-only،Plugin-only،co-install،Spring staging؛ 401/403،origin نامعتبر،manifest mismatch و feature-off باید رد شوند.
- UAT انسانی: PWA و Android از هر WordPress mode؛ پس از P15 همان flow روی Spring. Success: داده هم‌معنا، write یکتا، logout/cache isolation و UI feature-state درست.
- Evidence: `docs/evidence/P12-CLIENT-HOST-ADAPTER-025/`; تا Manual QA، `AWAITING_MANUAL_QA`.

- Final status: TODO
