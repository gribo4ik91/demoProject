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
- Testcontainers + PostgreSQL for integration tests
- Gradle Kotlin DSL
- Bootstrap 5

## Features

- Create and browse ecosystems
- Use the home page as a workspace dashboard with overview counters, priority sections, pinned ecosystems, richer cards, and quick actions
- Edit ecosystem metadata directly from the dashboard
- Open a dedicated dashboard for a selected ecosystem
- View a dashboard summary with current status, recent readings, activity streaks, 30-day activity, and lightweight trend analytics
- Record activity logs with temperature, humidity, event type, and notes
- Edit existing activity logs without leaving the dashboard
- Filter and paginate activity logs by event type
- Filter, sort, search, and incrementally load workspace cards from the backend
- Create manual maintenance tasks with due dates
- Edit manual maintenance tasks inline from the dashboard
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

### Prerequisites

- Java 21
- Docker with Compose support

### Start PostgreSQL

```powershell
cd path\to\demoProject\sql
docker compose up -d
```

### Start the application

```powershell
cd path\to\demoProject\application\application
.\gradlew.bat bootRun
```

The app runs at `http://localhost:8085`.

### Startup Sequence

1. Start PostgreSQL from the `sql` module
2. Start the Spring Boot application with Gradle
3. Spring Boot connects to PostgreSQL using the configured datasource
4. Flyway runs the migrations from `src/main/resources/db/migration`
5. Security configuration is applied
6. The static frontend becomes available through the backend

## Configuration

Datasource settings can be overridden with:

- `DB_URL`
- `DB_USERNAME`
- `DB_PASSWORD`

Authentication can be toggled with:

- `APP_AUTH_ENABLED`

Response delay can be toggled with:

- `APP_RESPONSE_DELAY_ENABLED`
- `APP_RESPONSE_DELAY_MIN_MS`
- `APP_RESPONSE_DELAY_MAX_MS`

Default local values are defined in [`application.yml`](src/main/resources/application.yml).

### Authentication Mode

Authentication is enabled by default:

```yaml
app:
  auth:
    enabled: ${APP_AUTH_ENABLED:true}
```

If you want to run the application without login, set:

```powershell
$env:APP_AUTH_ENABLED="false"
.\gradlew.bat bootRun
```

When authentication is enabled:

- unauthenticated users are redirected to `/login`
- a local user can be created from the sign-up form
- the app uses a server-side session after form login
- the user store is persisted in the `app_user` table

When authentication is disabled:

- all requests are allowed without login
- the home page opens directly
- the registration and profile flow is not required for local use

## Database Strategy

The runtime schema is managed by Flyway in [`V1__init_schema.sql`](src/main/resources/db/migration/V1__init_schema.sql).

This means:
- schema changes are explicit and versioned
- Hibernate validates the schema instead of mutating it automatically
- database state is easier to reproduce across environments

The integration test suite is configured to run against PostgreSQL through Testcontainers so the test database matches runtime behavior more closely.

## API Summary

Base path: `/api/v1`

Interactive documentation:

- Swagger UI: `http://localhost:8085/swagger-ui.html`
- OpenAPI JSON: `http://localhost:8085/api-docs`
- OpenAPI YAML: `http://localhost:8085/api-docs.yaml`

Endpoints:

- `POST /ecosystems`
- `PATCH /ecosystems/{id}`
- `GET /ecosystems`
- `GET /ecosystems/cards`
- `GET /ecosystems/overview`
- `GET /ecosystems/{id}`
- `GET /ecosystems/{id}/summary`
- `DELETE /ecosystems/{id}`
- `POST /ecosystems/{id}/logs`
- `PATCH /ecosystems/{id}/logs/{logId}`
- `GET /ecosystems/{id}/logs`
- `POST /ecosystems/{ecosystemId}/tasks`
- `PATCH /ecosystems/{ecosystemId}/tasks/{taskId}`
- `GET /ecosystems/{ecosystemId}/tasks`
- `PATCH /ecosystems/{ecosystemId}/tasks/{taskId}/status`

Supported query options:

- `GET /ecosystems/cards?search=fern&status=NEEDS_ATTENTION&sort=PRIORITY&page=0&size=9`
- `GET /ecosystems/overview?search=fern&status=STABLE`
- `GET /ecosystems/{id}/logs?eventType=WATERING&page=0&size=5`
- `GET /ecosystems/{ecosystemId}/tasks?filter=OPEN`
- `GET /ecosystems/{ecosystemId}/tasks?filter=DONE`
- `GET /ecosystems/{ecosystemId}/tasks?filter=OVERDUE`
- `GET /ecosystems/{ecosystemId}/tasks?filter=DISMISSED`

### Workspace home endpoints

The home page now relies on two backend endpoints:

- `GET /api/v1/ecosystems/cards`
  returns `PagedResponse<EcosystemWorkspaceCardResponse>` for the dashboard card grid
- `GET /api/v1/ecosystems/overview`
  returns `EcosystemWorkspaceOverviewResponse` for the top-level counters

`/ecosystems/cards` supports:

- `search` for name, type, and description matching
- `status` with `NEEDS_ATTENTION`, `STABLE`, `NO_RECENT_DATA`, and `OVERDUE`
- `sort` with `PRIORITY`, `LAST_ACTIVITY`, `NAME`, and `NEWEST`
- `page` and `size` for incremental loading on the home page

Important behavior:

- both cards and overview honor the same `search` and `status` filters
- overview counters are calculated from the full filtered result set, not only the current page
- the home page uses paged card loading while pinned ecosystems remain stored in browser `localStorage`

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
cd path\to\demoProject\application\application
.\gradlew.bat test
```

Current automated checks cover:
- application context startup
- controller-level error handling
- integration scenarios for ecosystem creation, updates, validation, summaries, log ordering, filtering, and cascade delete
- maintenance task lifecycle scenarios including overdue, suggested, dismissed, and manual edit flows

## Trade-offs

- The frontend is intentionally simple and server-hosted.
- Authentication is optional and intentionally minimal through a feature-flagged login and registration mode.
- The UI is built as Bootstrap-powered static pages rather than a separate SPA.
- Maintenance task filters are optimized for clarity and manual workflows, not for bulk operations.

## Next Steps

- Build out a Smart Monitoring Hub with richer trend visualizations, health scoring, alert states, anomaly indicators, and weekly ecosystem summaries
- Evolve maintenance workflows into a Care Automation Engine with recurring tasks, ecosystem-specific care templates, smarter suggested follow-up actions, snooze and reschedule flows, and bulk task updates
- Expand the home page experience in `index.html` with quick insight cards, overdue and attention-needed counters, recent activity snapshots, filter and sort controls, search, pinned ecosystems, and one-click actions for creating logs or tasks directly from the ecosystem list
- Add a dashboard-level overview for the full workspace so the home page can highlight stale ecosystems, recently updated setups, and the most urgent maintenance items before the user opens a specific ecosystem
