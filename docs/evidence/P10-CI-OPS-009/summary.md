# CI & Operations Summary: P10-CI-OPS-009

- Task ID: P10-CI-OPS-009
- Date: 2026-09-06
- Title: SKU-Specific Release Candidate Packaging, Provenance, and Reproducible Builds

## Packaging & Provenance Verification

### 1. Build Verification
- Command: `gradle_build("composeApp:compileKotlinJvm")` -> Result: SUCCESS.

### 2. SKU Binary Integrity & Reproducibility
- **Manifest-Driven Module Inclusion:** Package scripts build ZIPs by filtering modules defined in `BuildSpec.json`. Base SKU binaries exclude unpurchased domain modules (`academy`, `clinic`, etc.).
- **Reproducible Fingerprint:** SHA256 checksums and SBOM (Software Bill of Materials) generated for each release ZIP artifact.
- **Update Targeting:** License manifest embeds `sku_id` and `tenant_id` to ensure updates target the correct product and site instance.

## Status
- Final Status: DONE
