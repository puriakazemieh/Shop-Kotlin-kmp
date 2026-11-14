# Evidence Summary: P02-CI-OPS-016

- **Task**: P02-CI-OPS-016 — release artifact workflow از debug build جدا شود
- **Date**: 2026-11-14
- **Status**: DONE
- **Allowed Scope**: `.github/**`, `gradle/**`, `build-logic/**`, `tools/**`, `docs/**`
- **Changed Files**:
  - `.github/workflows/release-artifact.yml` (Decoupled release workflow, release build tasks, SBOM, SHA-256 sums, release metadata, 90-day retention)
  - `tools/ci/generate_sbom.cjs` (Automated CycloneDX 1.5 & SPDX 2.3 SBOM generator, SHA-256 manifest & release metadata generator)
  - `tools/ci/verify_release_workflow.cjs` (Automated verification harness for release workflow isolation, retention, SBOM, and KMP compilation)
  - `docs/evidence/P02-CI-OPS-016/summary.md`
  - `docs/evidence/P02-CI-OPS-016/EVIDENCE.md`
  - `docs/tasks/P02-CI-OPS-016.md`
  - `docs/tasks.md`
  - `docs/MASTER_IMPLEMENTATION_CHECKLIST_FA.md`

---

## 1. Measurable Objective & Expected Outcome
- **Objective**: Completely decouple the release artifact workflow from debug builds.
- **Expected Outcome**:
  - Release artifact workflow does not trigger or assemble debug targets.
  - Generates version metadata, SHA-256 checksums (`SHA256SUMS.txt`), and standards-compliant SBOM (CycloneDX 1.5 & SPDX 2.3).
  - Enforces long-term artifact retention policy (90 days vs 7 days ephemeral debug retention).

---

## 2. Automated Verification Results (`node tools/ci/verify_release_workflow.cjs`)

```text
--- Test 1: Release Workflow Decoupling from Debug Builds ---
[PASS] Release Workflow Decoupling
       release-artifact.yml is strictly isolated to release tasks; no debug tasks or debug artifact names found
--- Test 2: Retention & Release Policy Verification ---
[PASS] Retention Policy Verification
       Release workflow specifies 90 days retention for release artifacts (decoupled from ephemeral debug retention)
--- Test 3: SBOM, Checksum & Release Metadata Verification ---
[PASS] SBOM & Metadata Verification
       Generated valid CycloneDX 1.5 (54 components), SPDX 2.3 (53 packages), SHA-256 sums, and release metadata
--- Test 4: Baseline KMP Compilation Check ---
[PASS] Baseline KMP Compilation Check
       :composeApp compileKotlinJvm & compileKotlinJs completed with exit code 0

========================================
     RELEASE WORKFLOW CI/OPS CHECK      
========================================
✓ Release Workflow Decoupling: PASSED
✓ Retention Policy Verification: PASSED
✓ SBOM & Metadata Verification: PASSED
✓ Baseline KMP Compilation Check: PASSED
========================================
FINAL RESULT: ALL CHECKS PASSED
```

---

## 3. Workflow & Tooling Architecture Details
1. **Separation from Debug Builds**:
   - `.github/workflows/release-artifact.yml` previously invoked `./gradlew :androidApp:assembleDebug` and uploaded `android-debug-apks` with `retention-days: 7`.
   - Now, it exclusively targets release assemblies:
     - Android: `:androidApp:assembleCarmilaRelease`, `:androidApp:assembleAtrisRelease`, etc.
     - Desktop: `:composeApp:packageUberJarForCurrentOS`
     - Web: `:composeApp:jsBrowserDistribution` (production executable bundle)
     - WordPress: `build-theme-zip.sh` and `build-bridge-zip.sh`
   - Debug workflows remain isolated in `build-all.yml` and `pr-gate.yml`.

2. **Standards-Compliant SBOM Generation (`tools/ci/generate_sbom.cjs`)**:
   - Parses `gradle/libs.versions.toml` to extract all 52 external dependencies with group, name, version, and standard Package URL (`purl: pkg:maven/...`).
   - Generates `dist/sbom.cyclonedx.json` adhering to CycloneDX v1.5 specification.
   - Generates `dist/sbom.spdx.json` adhering to SPDX v2.3 specification.

3. **Cryptographic Checksums & Release Metadata**:
   - Computes SHA-256 digests for all generated build artifacts and outputs `dist/SHA256SUMS.txt`.
   - Produces `dist/release-metadata.json` documenting application version, commit SHA, build timestamp, retention period, and per-file SHA256/size descriptors.

4. **Retention Policy**:
   - Configures `retention-days: 90` for release artifacts in GitHub Actions, ensuring historical availability while separating ephemeral 7-day debug runs.

---

## 4. Acceptance Criteria Verification
- [x] خروجی با هدف و validation این کارت منطبق است.
- [x] Scope خارج از Allowed files/directories گسترش نیافته است.
- [x] تست خودکار/بازبینی لازم واقعاً اجرا و نتیجه ثبت شده است.
- [x] اگر تست دستی لازم است،Evidence انسانی ثبت شده یا Status برابر AWAITING_MANUAL_QA است (QA کاملاً خودکار و فنی است).
