# Evidence for P01-SPRING-SEC-024

## Baseline
- Date: 2026-08-25
- Server: D:\Android\AndroidStudioProjects\ShopServer

## Audit Summary
Audited all 59 Spring controllers for `@PreAuthorize` coverage.

### Findings — Admin Controllers Missing `@PreAuthorize`
| Controller | Path | Risk |
|---|---|---|
| `AdminQuestionController` | `/api/admin/questions` | Any authenticated user could list admin questions |
| `AdminReviewController` | `/api/admin/reviews` | Any authenticated user could list admin reviews |
| `AdminReturnRequestController` | `/api/admin/return-requests` | Any authenticated user could list/update return requests |

### SecurityConfig Analysis
- `@EnableMethodSecurity` is enabled ✅
- `anyRequest().authenticated()` — all non-public endpoints require JWT ✅
- BUT admin endpoints without `@PreAuthorize` only need valid JWT, not ADMIN role ❌

### Fix Applied
Added `@PreAuthorize("hasRole('ADMIN')")` at class level to all 3 controllers.

### All Other Admin Controllers
All other 22 Admin controllers already had `@PreAuthorize` — verified by automated scan.

## Verification
- `gradlew.bat compileKotlin` → exit code 0 (BUILD SUCCESSFUL in 26s)
