# Business Summary: P10-BUSINESS-BIZ-002

- Task ID: P10-BUSINESS-BIZ-002
- Date: 2026-09-06
- Title: Independent SKU Definition for Theme, Plugin, and Standalone Clients

## Independent Product SKU Catalog

| SKU ID | Product Kind | Bundle Contents | Features Included | Allowed Targets | License & Site Limit | Prerequisites |
|---|---|---|---|---|---|---|
| `SKU-THEME-BASE` | Theme | `carmilla-theme.zip` | Base UI, Blog, Settings | Web | Single Site | WordPress 6.0+ |
| `SKU-THEME-SHOP` | Theme | `carmilla-theme.zip` | Base UI + Commerce Core | Web | Single Site | WooCommerce 8.0+ |
| `SKU-THEME-BUILDER` | Theme Add-on | Build Runner Rights | App Builder Access | Android / PWA | Single Site | `SKU-THEME-SHOP` |
| `SKU-PLUGIN-BASE` | Plugin (Bridge) | `carmilla-bridge.zip` | Base UI Forms / API | Web | Single Site | Any 3rd Party Theme |
| `SKU-PLUGIN-SHOP` | Plugin (Bridge) | `carmilla-bridge.zip` | Base UI + Commerce Core | Web | Single Site | WooCommerce + 3rd Party Theme |
| `SKU-PLUGIN-BUILDER` | Plugin Add-on | Build Runner Rights | App Builder Access | Android / PWA | Single Site | `SKU-PLUGIN-SHOP` |
| `SKU-FULL-BUNDLE` | Combo Package | Theme + Plugin + Builder | All Features (`commerce`, `academy`, `clinic`, `builder`) | Android, PWA, Web | Single Site | WordPress + WooCommerce |

## Rules
1. Purchasing a Builder SKU grants build execution rights for licensed target platforms only.
2. Builder SKU does NOT automatically grant domain/feature entitlements that were not purchased.

## Status
- Final Status: AWAITING_MANUAL_QA
