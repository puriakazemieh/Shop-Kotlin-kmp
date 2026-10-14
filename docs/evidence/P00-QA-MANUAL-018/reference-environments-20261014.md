# P00-T04 — Selected reference environment for this QA cycle

- **Date:** 2026-10-14
- **Build fingerprint:** `ea32cff8`
- **Result:** PASS — F1 selected; other presets intentionally not selected

## Preset decision

| Preset | Definition | Decision for this cycle | Reason |
| --- | --- | --- | --- |
| F0 | Minimal content/auth | Not selected | The approved test scope is shop-only. |
| F1 | Shop: content + physical commerce | **Selected** | Matches Theme + Bridge + WordPress + PWA scope. |
| F2 | Academy | Deferred | Academy is explicitly out of scope. |
| F3 | Clinic/Psych | Deferred | Clinic and Psych are explicitly out of scope. |
| F4 | All-in-one | Deferred | It includes features excluded from this release-test cycle. |

## Executable F1 environment

| Component | Verified environment |
| --- | --- |
| Browser/PWA shell | `http://localhost/carmilla-pwa-qa/` returned HTTP 200 |
| WordPress compatibility matrix | Docker project `carmilla-p09-matrix` |
| Matrix endpoints | WordPress REST endpoints on `http://localhost:8081/`, `:8082/`, and `:8083/` returned HTTP 200 |
| Matrix variants | WP 6.4/PHP 8.1, WP 6.6/PHP 8.2, current WP/PHP 8.3 |
| Test data | Synthetic QA products/users only; no production environment is selected |

## Known limitation

The preserved XAMPP WordPress source instance at
`http://localhost/carmilla-p07-qa/` had active Apache and MySQL processes but
did not return its REST response within 20 seconds at this review. It is kept
as a local migration/PWA integration witness, not as the PASS basis for this
environment selection. Investigating that responsiveness issue belongs to the
relevant WordPress/PWA test step when it is next exercised.

## Reset policy

Persistent Docker volumes and the two preserved WordPress installations must
not be deleted. Resetting a scenario means using a synthetic fixture or a
fresh isolated instance; it must not use `docker compose down -v` against the
preserved matrix.
