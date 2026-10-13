<div dir="rtl" align="right">

# P04-WORDPRESS-CODE-051 — قرارداد canonical WordPress برای کلاینت Kotlin

- Status: TODO
- Phase/Area/Type: P04 / WORDPRESS / CODE
- Priority/Risk/Size: P0 / HIGH / M
- Owner: BOTH
- Completion authority: BOTH؛ شبکه نیازمند Manual QA
- Depends on: P04-FEATURE-CODE-050
- Blocks: P04-CI-CODE-043
- Requirement source: درخواست مالک محصول 2026-10-13؛ قرارداد پایه P03-ARCH-ADR-026.

## هدف قابل اندازه‌گیری

Theme-only،Plugin-only و co-install همان REST/manifest canonical را مستقل به کلاینت Kotlin عرضه کنند. adapter PWA/Android داده catalog/content/cart و feature state را sync کند، write یکتا باشد و origin/token/cache به tenant صحیح ایزوله بمانند.

## Acceptance

- سه mode پاسخ هم‌معنا و versioned دارند؛ co-install state divergent یا write/hook تکراری ندارد.
- client با BuildSpec trusted provider صحیح را انتخاب می‌کند؛ query/branding/manifest origin یا mode را تغییر نمی‌دهد.
- negative coverage برای 401/403،origin نامعتبر،tenant mismatch،manifest mismatch و feature-off وجود دارد.
- Spring در این کارت پیاده‌سازی نمی‌شود؛ پذیرش واقعی آن بعد از Gate P15 است.

## Scope و Verification

- مجاز: `core/**`،`composeApp/**`،`androidApp/**`،`wordpress/carmilla-theme/**`،`wordpress/carmilla-bridge/**`،`wordpress/packages/carmilla-core/**`،تست‌ها و Evidence.
- ممنوع: backend سوم،sync دوطرفه بدون ADR،secret یا داده Production.
- Manual QA: PWA و Android را جداگانه به Theme-only،Plugin-only و co-install با fixture یکسان وصل کنید. موفقیت: داده هم‌معنا،یک write،logout/cache isolation و state feature درست باشد. تا تأیید انسان `AWAITING_MANUAL_QA`.

## Completion record

- Started at:
- Completed at:
- Evidence paths:
- Final status: TODO

</div>
