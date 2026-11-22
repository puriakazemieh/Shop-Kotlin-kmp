# Evidence Summary: P02-CI-OPS-016

- **Task**: P02-CI-OPS-016 — release artifact workflow از debug build جدا شود
- **Date**: 2026-11-22
- **Status**: DONE
- **Allowed Scope**: `.github/**`, `gradle/**`, `build-logic/**`, `tools/**`, `docs/**`
- **Changed Files**:
  - `.github/workflows/release-artifact.yml` (Decoupled release workflow, strict failure on missing artifacts without `|| true`, mandatory inventory validation gate, 90-day retention)
  - `tools/ci/generate_sbom.cjs` (Automated CycloneDX 1.5 & SPDX 2.3 SBOM generator, SHA-256 manifest & release metadata generator with anti-empty-payload rejection)
  - `tools/ci/verify_release_workflow.cjs` (Automated verification harness for release workflow isolation, mandatory artifact gates, negative probe for empty payload, retention, SBOM, and KMP compilation)
  - `docs/evidence/P02-CI-OPS-016/summary.md`
  - `docs/evidence/P02-CI-OPS-016/EVIDENCE.md`
  - `docs/tasks/P02-CI-OPS-016.md`
  - `docs/tasks.md`
  - `docs/MASTER_IMPLEMENTATION_CHECKLIST_FA.md`

---

## 1. Measurable Objective & Expected Outcome
- **Objective**: Completely decouple the release artifact workflow from debug builds, enforce mandatory product artifact presence (prevent metadata-only success), produce CycloneDX 1.5 & SPDX 2.3 SBOMs, SHA-256 checksums, and configure long-term retention.
- **Expected Outcome**:
  - Release artifact workflow does not trigger or assemble debug targets.
  - Silent bypasses (such as `|| true` or optional product directories) removed; workflow strictly fails if any selected release artifact is missing.
  - SBOM & checksum generator fails fast if no product artifacts exist in output directory (rejects empty payload).
  - Generates version metadata, SHA-256 checksums (`SHA256SUMS.txt`), and standards-compliant SBOM (CycloneDX 1.5 & SPDX 2.3).
  - Enforces long-term artifact retention policy (90 days vs 7 days ephemeral debug retention).

---

## 2. Automated Verification Results (`node tools/ci/verify_release_workflow.cjs`)

```text
--- Test 1: Release Workflow Decoupling from Debug Builds ---
[PASS] Release Workflow Decoupling
       release-artifact.yml is strictly isolated to release tasks; no debug tasks or debug artifact names found
--- Test 2: Mandatory Artifact & Inventory Validation Gate ---
[PASS] Mandatory Artifact & Inventory Gate
       Workflow strictly fails on missing desktop/web/android/wordpress artifacts and includes inventory validation gate without "|| true" silencing
--- Test 3: Retention & Release Policy Verification ---
[PASS] Retention Policy Verification
       Release workflow specifies 90 days retention for release artifacts (decoupled from ephemeral debug retention)
--- Test 4: Negative Probe: Empty Output Rejection ---
[PASS] Negative Probe: Empty Output Rejection
       generate_sbom.cjs correctly failed with exit code 1 and produced 0 files when called on an empty payload directory
--- Test 5: SBOM, Checksum & Release Metadata Verification ---
[PASS] SBOM & Metadata Verification
       Generated valid CycloneDX 1.5 (54 components), SPDX 2.3 (53 packages), SHA-256 sums for 3 artifacts, and 90-day retention metadata
--- Test 6: Baseline KMP Compilation Check ---
[PASS] Baseline KMP Compilation Check
       :composeApp compileKotlinJvm & compileKotlinJs completed with exit code 0

========================================
     RELEASE WORKFLOW CI/OPS CHECK      
========================================
✓ Release Workflow Decoupling: PASSED
✓ Mandatory Artifact & Inventory Gate: PASSED
✓ Retention Policy Verification: PASSED
✓ Negative Probe: Empty Output Rejection: PASSED
✓ SBOM & Metadata Verification: PASSED
✓ Baseline KMP Compilation Check: PASSED
========================================
FINAL RESULT: ALL CHECKS PASSED
```

---

## 3. Audit Findings Resolution (2026-11-15 Audit)

ممیزی ۱۵ نوامبر ۲۰۲۶ دو دغدغه اساسی را در این گردش کار شناسایی کرده بود که با تغییرات زیر به طور کامل و قطعی برطرف شدند:

1. **حذف خاموش‌سازی خطاها (`|| true`) و الزامی‌کردن خروجی‌ها در `.github/workflows/release-artifact.yml`:**
   - دستور کپی UberJar دسکتاپ دیگر فاقد `|| true` است و در صورت عدم وجود فایل JAR با کد خطای ۱ متوقف می‌شود.
   - بیلد توزیع وب وجود دایرکتوری `composeApp/build/dist/js/productionExecutable` و ایجاد موفق `carmilla-web-release.zip` را بررسی کرده و در صورت نبود متوقف می‌شود.
   - بیلد APKهای اندروید و فایل‌های ZIP وردپرس ایجاد واقعی بسته‌ها را اعتبارسنجی می‌کنند.
   - گیت جدید `Validate Release Product Artifact Inventory` اضافه شد که تعداد بسته‌های محصول (`*.apk`, `*.jar`, `*.zip`) را شمرده و در صورت صفر بودن اجازه ادامه گردش کار و انتشار متادیتای خالی را نمی‌دهد.

2. **سد رد بسته خالی در ژنراتور SBOM (`tools/ci/generate_sbom.cjs`):**
   - در صورت اجرای اسکریپت بر روی یک دایرکتوری فاقد بسته‌های محصول، پیام خطای صریح صادر شده و با کد خروجی ۱ متوقف می‌شود:
     `[ERROR] No product release artifacts found in output directory ... Refusing to generate release metadata/SBOM for empty payload.`
   - هیچ فایل متادیتا یا چک‌سام خالی تولید نمی‌شود (تست منفی با موفقیت ۱۰۰٪ اثبات شد).

---

## 4. Unified Quality Gates Check (`node tools/code-quality/verify_all_quality_gates.cjs`)
- **Theme Check Baseline Tests**: 23 scenarios passed (PASS)
- **Plugin Check Baseline Tests**: 19 scenarios passed (PASS)
- **Workflow Syntax & Actionlint (1.7.7)**: PASS (هیچ خطای گرامری یا امنیتی در گردش کار وجود ندارد)
- **WPCS Baseline & Probes**: PASS
- **Zip Builders (composer:2)**: PASS

---

## 5. Acceptance Criteria Verification
- [x] خروجی با هدف و validation این کارت منطبق است.
- [x] Scope خارج از Allowed files/directories گسترش نیافته است.
- [x] تست خودکار/بازبینی لازم واقعاً اجرا و نتیجه ثبت شده است.
- [x] تست منفی بسته خالی و اعتبارسنجی آرتیفکت‌ها با موفقیت پاس شد.
