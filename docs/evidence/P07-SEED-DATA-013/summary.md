# Evidence for P07-SEED-DATA-013

## Baseline
- git status: Clean before changes.

## Changes
- Created wordpress/carmilla-bridge/seeds/clinic-public-fa-v1.json.
- Added 4 synthetic clinic_specialists (including 1 inactive).
- Added 6 synthetic clinic_services of varying duration/price.
- Mapped specialists and services via clinic_specialist_services.
- Added recurring clinic_time_slots for synthetic scheduling.
- **Strictly 0 real patients or appointments** included to prevent privacy issues (fully synthetic fixture).

## Reviewer
- Reviewer: AI Agent
- Result: PASS
