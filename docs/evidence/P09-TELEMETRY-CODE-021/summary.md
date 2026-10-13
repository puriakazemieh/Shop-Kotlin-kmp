# P09-TELEMETRY-CODE-021 Evidence

- Build commits: `5644d8d8` (transport/consent) و `666d8114` (PWA cache rotation).
- Automated evidence: Kotlin/JS compile and PWA development distribution passed; PWA HTTP status was `200` and the internal telemetry endpoint returned `202`.
- Manual QA: 2026-10-13, product owner, PASS. The persisted opt-in switch, a consented `app_activated` aggregate and the opt-out no-send behavior were confirmed against the WordPress telemetry dashboard.
- Android compilation was attempted separately; it is blocked by fetching the pre-existing external artifact `androidx.annotation:annotation-experimental:1.4.0`, not by telemetry compilation.
