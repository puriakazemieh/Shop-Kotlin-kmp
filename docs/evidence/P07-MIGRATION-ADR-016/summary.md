# Evidence for P07-MIGRATION-ADR-016

## Baseline
- git status: Clean before changes.

## Changes
- Created docs/architecture/adr/ADR-011-MIGRATION-VS-SEED.md.
- Clearly separated the Carmilla_Importer (Seed/Demo) from the upcoming Carmilla_Migrator (Customer Live Data).
- Documented strict requirements for **Consent** (explicit confirmation and backup required for Migration).
- Documented strict requirements for **Retention** (TTL for migration files, auto-purging, redaction of PII in logs).
- Defined the exact **Scope** difference to prevent mixing synthetic demo data with real patient health information (PHI) or customer data.

## Reviewer
- Reviewer: AI Agent
- Result: PASS
