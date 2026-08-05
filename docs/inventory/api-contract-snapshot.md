# API Contract Snapshot and Mismatch Report

> Generated: 2026-08-05T12:45:00.000000000+03:30
> Task ID: P00-CORE-DISC-006

This document records the current state of API contracts across KMP, WordPress, and Spring, highlighting identified mismatches.

## 1. Base Endpoint Patterns
| Platform | Base Path / Namespace | Example |
|---|---|---|
| **Spring Boot** | `/api/` | `POST /api/auth/login` |
| **WordPress** | `/wp-json/carmilla/v1/api/` | `POST .../api/auth/login` |
| **KMP (Ktor)** | Relative to `PlatformConfig.baseUrl` | `client.post("api/auth/login")` |

## 2. Core Contracts (Authentication)
Identified parity in endpoint naming for most auth operations:
- `register`, `login`, `send-login-otp`, `login-with-otp`, `refresh`, `forgot-password`, `reset-password`.

### ⚠️ Mismatches:
- **Logout**: Spring has `/api/auth/logout` and `/api/auth/logout-all`. WordPress implementation for logout is missing in `CB_Auth_Controller`.

## 3. Catalog and Product Contracts
### ⚠️ Mismatches:
- **Sort Parameter Values**:
    - **WordPress**: `price,asc`, `price,desc`, `newest`, `createdAt,desc`.
    - **Spring**: `newest`, `price_asc`, `price_desc`.
    - **KMP**: Passes string directly; logic depends on active backend.
- **Pagination Strategy**:
    - Both use `PageResponse` with `content` and `page` metadata.
    - **WP**: pages are 1-based internally (mapped to 0-based for app).
    - **Spring**: 0-based.
- **Product Detail Identifiers**:
    - **Spring**: Supports `slug` and sometimes `id`.
    - **WP**: Public uses `slug`, Admin uses `id`.

## 4. Error Envelope (ApiError)
High parity maintained. Both backends return:
```json
{
  "message": "Error description",
  "status": 400,
  "code": 400,
  "errorCode": "VALIDATION_FAILED",
  "path": "/api/...",
  "timestamp": "ISO-8601"
}
```

## 5. Deep Link Mismatches
Critical mismatch in payment return schemes:
- **Android Manifest**: `myapp://payment-result`
- **WordPress Helper**: `carmilla://payment/result` (default option)
- **iOS Config**: Not explicitly registered in `Info.plist` (only schema placeholder seen in `MainViewController.kt`).

## 6. Data Type Consistency
- **Prices**: Spring uses `BigDecimal` (accurate for currency). WordPress uses `float`/`Double` in many helpers, which is prone to precision issues.
- **Dates**: WordPress uses `cb_iso` helper (standard ISO-8601). Spring uses `OffsetDateTime`.

---

## 7. Action Items for P01/P02
1. **Unify Deep Link**: Pick a canonical scheme (e.g., `carmilla://payment-result`) and apply to all.
2. **Sort Enum**: Define a unified Sort enum in `core:domain` and map it in backend adapters.
3. **Price Precision**: Audit WordPress helpers to ensure `BigDecimal` (or string-based representation) is used for all financial calculations.
