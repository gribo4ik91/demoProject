# EcoTracker

EcoTracker is a compact full-stack application for tracking small ecosystems such as formicariums, florariums, indoor plant setups, and DIY incubators.

The project demonstrates a complete vertical slice:
- Kotlin + Spring Boot REST API
- PostgreSQL persistence with Flyway migrations
- Static frontend served by the application
- Validation, structured API errors, and automated tests

## Highlights

- Clear API contracts through request/response DTOs
- Service layer separated from controllers
- Flyway-managed database schema
- Consistent JSON error responses
- Controller and integration test coverage
- Lightweight UI for end-to-end interaction with the API

## Tech Stack

- Java 21
- Kotlin 2.2
- Spring Boot 4
- Spring Web MVC
- Spring Data JPA
- PostgreSQL
- Flyway
- H2 for isolated tests
- Gradle Kotlin DSL
- Bootstrap 5

## Features

- Create and browse ecosystems
- Open a dedicated dashboard for a selected ecosystem
- View a dashboard summary with current status, latest readings, and recent activity indicators
- Record activity logs with temperature, humidity, event type, and notes
- Filter and paginate activity logs by event type
- Create manual maintenance tasks with due dates
- Review maintenance tasks by status and source
- Mark suggested tasks as dismissed with a dismissal reason
- Delete an ecosystem together with its logs
- Receive structured validation and not-found responses from the API

## Architecture

Main package structure:

- `controller`: HTTP endpoints and response codes
- `service`: business flow and orchestration
- `dto`: API request/response contracts
- `mapper`: entity-to-DTO mapping
- `model`: JPA entities
- `repository`: Spring Data persistence access
- `exception`: global API error handling

Request flow:

1. The client sends an HTTP request.
2. The controller validates and forwards the request to a service.
3. The service coordinates repositories and domain logic.
4. Entities are mapped to response DTOs.
5. Errors are returned through a shared exception handler.

## Run Locally

### Start PostgreSQL

```powershell
cd C:\Endava\EndevLocal\demoProject\sql
docker compose up -d
```

### Start the application

```powershell
cd C:\Endava\EndevLocal\demoProject\application\application
.\gradlew.bat bootRun
```

The app runs at `http://localhost:8085`.

## Configuration

Datasource settings can be overridden with:

- `DB_URL`
- `DB_USERNAME`
- `DB_PASSWORD`

Authentication can be toggled with:

- `APP_AUTH_ENABLED`

Default local values are defined in [`application.yml`](C:/Endava/EndevLocal/demoProject/application/application/src/main/resources/application.yml).

### Optional Login Flow

The application stays open by default:

```yaml
app:
  auth:
    enabled: false
```

To enable the login and registration flow, set:

```powershell
$env:APP_AUTH_ENABLED="true"
.\gradlew.bat bootRun
```

When enabled:

- unauthenticated users are redirected to `/login`
- a local user can be created from the sign-up form
- the app uses a server-side session after form login
- the user store is persisted in the `app_user` table

## Database Strategy

The runtime schema is managed by Flyway in [`V1__init_schema.sql`](C:/Endava/EndevLocal/demoProject/application/application/src/main/resources/db/migration/V1__init_schema.sql).

This means:
- schema changes are explicit and versioned
- Hibernate validates the schema instead of mutating it automatically
- database state is easier to reproduce across environments

The test suite uses an isolated H2 profile for speed and determinism.

## API Summary

Base path: `/api/v1`

Interactive documentation:

- Swagger UI: `http://localhost:8085/swagger-ui.html`
- OpenAPI JSON: `http://localhost:8085/api-docs`
- OpenAPI YAML: `http://localhost:8085/api-docs.yaml`

Endpoints:

- `POST /ecosystems`
- `GET /ecosystems`
- `GET /ecosystems/{id}`
- `GET /ecosystems/{id}/summary`
- `DELETE /ecosystems/{id}`
- `POST /ecosystems/{id}/logs`
- `GET /ecosystems/{id}/logs`
- `POST /ecosystems/{ecosystemId}/tasks`
- `GET /ecosystems/{ecosystemId}/tasks`
- `PATCH /ecosystems/{ecosystemId}/tasks/{taskId}/status`

Supported query options:

- `GET /ecosystems/{id}/logs?eventType=WATERING&page=0&size=5`
- `GET /ecosystems/{ecosystemId}/tasks?filter=OPEN`
- `GET /ecosystems/{ecosystemId}/tasks?filter=DONE`
- `GET /ecosystems/{ecosystemId}/tasks?filter=OVERDUE`
- `GET /ecosystems/{ecosystemId}/tasks?filter=DISMISSED`

Example validation error:

```json
{
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed",
  "path": "/api/v1/ecosystems",
  "validationErrors": {
    "name": "must not be blank"
  }
}
```

## Testing

Run all tests:

```powershell
cd C:\Endava\EndevLocal\demoProject\application\application
.\gradlew.bat test
```

Current automated checks cover:
- application context startup
- controller-level error handling
- integration scenarios for ecosystem creation, validation, summaries, log ordering, filtering, and cascade delete
- maintenance task lifecycle scenarios including overdue, suggested, and dismissed flows

## Trade-offs

- The frontend is intentionally simple and server-hosted.
- Authentication is optional and intentionally minimal through a feature-flagged login and registration mode.
- The UI is built as Bootstrap-powered static pages rather than a separate SPA.
- Maintenance task filters are optimized for clarity and manual workflows, not for bulk operations.

## Next Steps

- Move integration tests to Testcontainers + PostgreSQL
- Add edit/update flows for ecosystems, logs, and manual maintenance tasks
- Expand task management with richer source filters and bulk actions
- Enrich the dashboard with longer-term trends and analytics
