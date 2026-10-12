# P06 — Carmilla admin-menu consolidation

Date: 2026-10-12

## Fixed

- Shared Core is the sole owner of the top-level `Carmilla` menu (`admin.php?page=carmilla-settings`).
- Bridge registers Integrations only as its submenu (`admin.php?page=carmilla-integrations`).
- The Theme dashboard is also a Core submenu, preventing a second top-level Carmilla item when both products are installed.

## Verification

- PHP lint passed for the changed Bridge and Theme files.
- In the preserved local WordPress installation, administrator menu registration produced exactly one top-level Carmilla entry and the Integrations submenu:

```text
top-level-carmilla=1 integrations-submenu=1
```
