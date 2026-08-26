# P03-MANIFEST-ADR-001 evidence

## Draft produced

`docs/architecture/adr/ADR-004-FEATURE-MANIFEST-V1.md` proposes the v1 manifest schema, F0–F4 fixtures, feature dependencies, profile trust boundary, fail-closed rules, and compatibility policy.

## Verification

- Source review: ADR-002 and audit sections 21.4–21.8 were used as requirements.
- Baseline command: `./gradlew.bat :composeApp:compileKotlinJvm` exited 1 before compilation because Gradle could not establish a loopback connection.

## Required human approval

Review the four decisions listed in the ADR's **Approval requested** section. On approval, mark this task `DONE` and make `P03-ARCH-CODE-002` ready; on requested changes, revise only this ADR and its evidence.
