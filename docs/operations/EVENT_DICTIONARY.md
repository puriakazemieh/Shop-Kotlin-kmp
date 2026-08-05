# Event Dictionary (Version 0)

> Generated: 2026-08-05
> Task ID: P00-OBSERVABILITY-ADR-014

This document defines the standard taxonomy for product events across all Carmilla platforms.

## 1. Onboarding & Identity
| Event Name | Trigger | Parameters | Type |
|---|---|---|---|
| `app_opened` | App launch | `platform`, `version`, `tenant_id` | Operational |
| `sign_up_start` | User clicks register | `method` (email/mobile) | Analytics |
| `otp_sent` | OTP successfully issued | `provider_id`, `purpose` | Operational |
| `login_success` | User logs in successfully | `method`, `role` | Audit |
| `login_failure` | Login attempt failed | `error_code`, `reason` | Security |

## 2. Commerce (Shop)
| Event Name | Trigger | Parameters | Type |
|---|---|---|---|
| `product_viewed` | Product detail opened | `product_id`, `category_id`, `stock_status` | Analytics |
| `add_to_cart` | User adds item | `product_id`, `variant_id`, `quantity`, `price` | Analytics |
| `checkout_start` | User opens checkout | `cart_total`, `item_count` | Analytics |
| `payment_initiated`| User redirected to gateway | `intent_id`, `provider`, `amount` | Operational |
| `payment_verified` | Success callback verified | `order_id`, `amount`, `ref_id` | Audit |
| `order_placed` | Order record created | `order_id`, `total_amount` | Audit |

## 3. Verticals (LMS / Clinic)
| Event Name | Trigger | Parameters | Type |
|---|---|---|---|
| `course_enrolled` | User joins a course | `course_id`, `type` (free/paid) | Analytics |
| `lesson_completed` | User finishes a lesson | `course_id`, `lesson_id`, `duration` | Analytics |
| `appointment_booked`| Slot successfully reserved | `therapist_id`, `slot_time` | Audit |
| `test_submitted` | Psych test completed | `test_id`, `score_bracket` | Audit |

## 4. System & Operations (PWA/Build)
| Event Name | Trigger | Parameters | Type |
|---|---|---|---|
| `pwa_installed` | Browser install prompt accepted| `browser`, `os` | Analytics |
| `build_job_started` | Build service queue entry | `job_id`, `template_version` | Operational |
| `build_job_failed` | Build failure | `job_id`, `error_category` | Operational |
| `import_started` | Seed/Demo data import | `pack_id`, `site_id` | Audit |
| `import_finished` | Import completed | `pack_id`, `duration`, `entity_count` | Operational |

---

## 5. Parameter Constraints
- **Boolean**: Always `true` / `false`.
- **Numeric**: Use `Double` for currency and `Int` for counts.
- **Strings**: Use `snake_case` for both parameter keys and values.
- **PII Guard**: Never include `email`, `phone_number`, `user_full_name`, or `detailed_address` in any event.
