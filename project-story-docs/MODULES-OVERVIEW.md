# Project Modules Overview

## Purpose

This file provides a quick structural overview of the project so it is easy to understand:

- which modules the solution contains
- what each module is responsible for
- which files are the main entry points

## Module map

| Module | Purpose | Key files |
|---|---|---|
| `application/application/src/main/kotlin/com/example/api/controller` | HTTP API and page routing | `EcosystemController.kt`, `EcosystemLogController.kt`, `MaintenanceTaskController.kt`, `AuthController.kt`, `LoginPageController.kt` |
| `application/application/src/main/kotlin/com/example/api/service` | Business logic and orchestration | `EcosystemService.kt`, `EcosystemLogService.kt`, `MaintenanceTaskService.kt`, `AuthService.kt` |
| `application/application/src/main/kotlin/com/example/api/model` | JPA domain entities | `Ecosystem.kt`, `EcosystemLog.kt`, `MaintenanceTask.kt`, `AppUser.kt` |
| `application/application/src/main/kotlin/com/example/api/repository` | Data access via Spring Data JPA | `EcosystemRepository.kt`, `EcosystemLogRepository.kt`, `MaintenanceTaskRepository.kt`, `AppUserRepository.kt` |
| `application/application/src/main/kotlin/com/example/api/dto` | API request and response contracts | `CreateEcosystemRequest.kt`, `LogRequest.kt`, `CreateMaintenanceTaskRequest.kt`, `RegisterUserRequest.kt` |
| `application/application/src/main/kotlin/com/example/api/config` | Security, OpenAPI, and response delay configuration | `SecurityConfig.kt`, `OpenApiConfig.kt`, `ResponseDelayFilter.kt`, `ResponseDelayProperties.kt` |
| `application/application/src/main/kotlin/com/example/api/exception` | Shared API error handling | `GlobalExceptionHandler.kt`, `ApiErrorResponse.kt` |
| `application/application/src/main/kotlin/com/example/api/controller` | HTTP API and SSR page routing | `EcosystemController.kt`, `UiController.kt`, `LoginPageController.kt`, `UiSupport.kt` |
| `application/application/src/main/resources/templates` | Freemarker pages and fragments | `pages/home.ftlh`, `pages/ecosystem.ftlh`, `fragments/workspace-panel.ftlh`, `fragments/task-list.ftlh`, `fragments/log-list.ftlh` |
| `application/application/src/main/resources/static` | Embedded static assets for the SSR UI | `css/home.css`, `css/ecosystem.css`, `js/ssr-ui.js`, `favicon.svg` |
| `application/application/src/main/resources/db/migration` | Versioned database schema through Flyway | `V1__init_schema.sql` ... `V7__add_optional_profile_fields.sql` |
| `application/application/src/test` | Automated controller, integration, and configuration tests | `ApplicationTests.kt`, `ApiIntegrationTests.kt`, `AuthEnabledIntegrationTests.kt`, `EcosystemControllerTest.kt` |
| `application/application` | Application operational layer | `README.md`, `Dockerfile`, `docker-compose.app.yml`, `Jenkinsfile`, `api-tests.http`, `api-tests-auth-enabled.http` |
| `sql` | PostgreSQL operational layer | `docker-compose.yml`, `Jenkinsfile` |
| `project-story-docs` | Russian analytical and architectural documentation | `README.md`, `MODULES-OVERVIEW.md`, `01-09 *.md` |
| `project-story-docs/en` | English analytical and architectural documentation | `README.md`, `MODULES-OVERVIEW.md`, `01-09 *.md` |

## How to read the project by perspective

### If you need the business view

Start with:

- `project-story-docs/01-product-overview.md`
- then continue with `02-06`

### If you need the architecture view

Read:

- `project-story-docs/07-architecture-and-data.md`
- `project-story-docs/08-sql-module.md`
- `project-story-docs/09-delivery-and-operations.md`

### If you need the code entry path

Go top-down:

1. `controller`
2. `service`
3. `repository`
4. `model`
5. `db/migration`

### If you need the runtime path

Check:

- `application/application/README.md`
- `sql/docker-compose.yml`
- `application/application/docker-compose.app.yml`
- `application/application/Jenkinsfile`

## Short conclusion

The project is organized quite cleanly:

- business features are separated from infrastructure
- database schema is separated from PostgreSQL runtime
- the frontend is embedded in the backend but still separated into SSR templates, UI helpers, and static assets
- the documentation now covers business, architecture, and operational concerns
