# ADR-011: Separation of Seed Import and Customer Migration Workflows

## Context
The Carmilla project requires tools to populate data in two distinct scenarios:
1. **Seed/Demo Import**: Populating fresh installations with synthetic, template, and placeholder data for demonstration or initial setup (e.g., ase-fa-v1, shop-fa-v1).
2. **Customer Migration**: Moving real, live, sensitive customer data from a legacy system into Carmilla.

## Decision
We will strictly separate these two processes into two entirely distinct workflows and codebases, rather than trying to build a single generic "Importer".

### 1. Seed Import Workflow (Demo Data)
- **Scope**: Handles static JSON manifests (like ll-fa-v1.json) containing synthetic data.
- **Codebase**: Handled by Carmilla_Importer inside carmilla-core/inc/Import.
- **Consent**: Does not require explicit privacy consent because it contains zero PII (Personally Identifiable Information) or real patient/customer data. It uses synthetic dummy data.
- **Retention**: Seed manifests are tracked in version control (Git) indefinitely. Data inserted is tracked in carmilla_seed_objects for idempotency and safe rollback.
- **Security**: Strict allowlists for media sideloading to prevent SSRF.

### 2. Customer Migration Workflow (Live Data)
- **Scope**: Handles moving real WooCommerce orders, patient appointments, and user accounts from legacy databases/exports.
- **Codebase**: Will be handled by a completely isolated module (e.g., Carmilla_Migrator), physically separate from the Seed Importer.
- **Consent (Crucial)**: 
  - Must include explicit admin consent (UI checkbox or CLI confirmation flag) confirming that PII is being processed.
  - Must require a database backup to be taken before execution.
- **Retention (Crucial)**: 
  - Migration temporary files, exported CSVs, and staging databases must have strict TTLs (Time-To-Live). They must be automatically purged after a successful migration or after 7 days.
  - Migration logs must automatically redact PII (passwords, emails, phone numbers, health data).
- **Security**: Must execute in a secure, isolated environment. Cannot load data from untrusted public URLs.

## Consequences
- **Positive**: Strict isolation reduces the risk of accidentally overwriting or mixing real patient data with dummy data. It ensures privacy laws and GDPR compliance regarding real customer data.
- **Negative**: Requires maintaining two separate data insertion logics, slightly increasing development overhead.
