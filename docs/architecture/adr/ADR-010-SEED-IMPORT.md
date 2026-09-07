# ADR-010: Seed Pack and Shared Importer Ownership

## Status
Accepted

## Context
Carmilla is split into two independent products (Theme and Plugin/Bridge) according to ADR-006. We need a way to import demo data (Seed Packs) for both products.
- **Theme** needs demo content, widget settings, and theme options.
- **Bridge** needs demo payment settings, messaging settings, or dummy entities.
- Users might install one or both. The importer must gracefully skip content for features that are not active or not licensed, and it must be idempotent (running it twice should not duplicate data unnecessarily, nor wipe out existing user configurations when features are toggled).

## Decision
1. **Shared Ownership:** The Importer engine will reside in `carmilla-core` so both the Theme and the Bridge have access to it.
2. **Schema & Manifest:** Each Seed Pack will include a `manifest.json` describing the data chunks. The importer will only process chunks that map to active and licensed SKU features.
3. **Idempotency:** Importer will use unique identifiers (e.g., specific post metas or option keys). If a record exists, it will update or skip, but not duplicate.
4. **Data Preservation:** Disabling a feature will hide its UI, but importing data for a disabled feature is skipped entirely rather than erasing existing database records.

## Consequences
- The core package must define a structured JSON schema for seed data.
- The importer logic must dynamically check feature toggles (manifest validation) before executing `wp_insert_post` or `update_option`.
- Ensures zero fatal errors if a demo pack contains WooCommerce data but WooCommerce is not installed (it simply skips that chunk).

## SKU & Feature Matrix

| Pack | Feature | Schema | SKU Requirements |
|---|---|---|---|
| Demo Content | Blog Posts | `wp_posts` | Theme (Free/Pro) |
| Demo Products| WooCommerce | `wc_products`| Theme (Free/Pro) + WooCommerce |
| Payment Setup| Gateways | `wp_options` | Bridge + Payment Module |
| SMS Templates| Messaging | `wp_options` | Bridge + Messaging Module |

If a feature is absent/unlicensed, the importer will silently skip that section of the manifest without corrupting existing data.
