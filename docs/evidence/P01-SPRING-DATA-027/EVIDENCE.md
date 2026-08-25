# Evidence for P01-SPRING-DATA-027

## Actions
- Added Flyway dependencies (core & postgresql) to Shop/build.gradle.kts.
- Extracted current JPA schema to V1__init.sql using Hibernate Schema Generator.
- Changed spring.jpa.hibernate.ddl-auto to validate for safe production deployments.
- Configured spring.flyway.baseline-on-migrate=true to allow smooth migration of existing environments.

## Verification
- Spring Boot compiled successfully.
- Schema file created at Shop/src/main/resources/db/migration/V1__init.sql.
