# P03-MANIFEST-CODE-011B — client امن remote manifest

- Status: TODO
- Owner: AI
- Depends on: P03-MANIFEST-CODE-011A
- Blocks: P03-MANIFEST-CODE-011C
- Size: M

## هدف

client دریافت manifest tenant با timeout، ETag و decode/validation fail-closed افزوده شود؛ remote manifest فقط featureها را کاهش می‌دهد و origin یا ceiling را تغییر نمی‌دهد.

## محدودهٔ مجاز

`core/**`، `composeApp/**` و `docs/**`.

## پذیرش

- پاسخ invalid، schema ناشناخته، backend یا tenant ناهماهنگ به local fallback منتهی شود.
- ETag و timeout در test پوشش داده شوند.
- Evidence در `docs/evidence/P03-MANIFEST-CODE-011B/` ثبت شود.

## تکمیل

- Commands and exit codes:
- Evidence paths:
- Final status: TODO | DONE | BLOCKED
