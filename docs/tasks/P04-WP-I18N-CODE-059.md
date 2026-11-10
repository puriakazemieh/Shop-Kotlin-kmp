# P04-WP-I18N-CODE-059 — Localize independent Theme and Bridge

- Status: TODO
- Phase/Area/Type: P04 / WordPress / CODE
- Priority/Risk/Size: P2 / MEDIUM / L (decompose before coding)
- Owner: AI
- Depends on: P04-WP-ADMIN-CODE-054
- Blocks: P04-CI-CODE-019

## Scope and acceptance

Resolve P02 C3B3B i18n findings for the independently installable Theme and Bridge. Shared core currently uses Theme domain `carmilla` while Bridge ZIP declares `carmilla-bridge`; Bridge-only installs can lack the required catalog. Design an explicit dual-host translation strategy without breaking Theme-only or combined installation, and split into S/M implementation and QA cards before editing. Cover the privacy-policy text, placeholders and translator comments, and Bridge-local missing domain. Test language loading and translated output in representative Theme-only, Bridge-only and combined WordPress installs with synthetic catalogs; inspect the resulting package scan. Evidence: `docs/evidence/P04-WP-I18N-CODE-059/`.

## Completion record

- Final status: TODO; ownership transferred from P02 i18n triage.
