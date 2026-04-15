# Story 07. Architecture, Data, and Technical Implementation

## Architecture story

As an architect, I want every business feature to move through transparent technical layers so that the project is easy to evolve, test, and explain to the team.

## Architectural style

The project uses a classic layered architecture:

- `controller` - HTTP entry points
- `service` - business rules and orchestration
- `repository` - data access
- `model` - JPA entities
- `dto` - API input and output contracts
- `mapper` - entity/DTO conversion
- `exception` - shared error handling
- `resources/static` - embedded frontend

## Technology stack

- Java 21
- Kotlin 2.2
- Spring Boot 4
- Spring Web MVC
- Spring Data JPA
- Spring Security
- Flyway
- PostgreSQL
- H2 for tests
- Gradle Kotlin DSL
- Bootstrap 5

## Request flow

1. The user opens a page or calls the API
2. the `controller` receives and validates the request DTO
3. the `service` executes the business logic
4. the `repository` reads or writes entities
5. data is returned through DTOs
6. errors are converted into a consistent API format

## Data model

### Table `ecosystems`

Stores:

- identifier
- name
- type
- description
- creation timestamp

### Table `logs`

Stores:

- identifier
- ecosystem reference
- temperature
- humidity
- event type
- notes
- recorded timestamp

### Table `maintenance_tasks`

Stores:

- identifier
- ecosystem reference
- task title
- task type
- due date
- status
- auto-created flag
- dismissal reason
- creation timestamp

### Table `app_user`

Stores:

- identifier
- login
- password hash
- display name
- first name
- last name
- email
- location
- bio
- creation timestamp

## Database migrations

The schema evolves through Flyway migrations:

- `V1` - ecosystems and logs
- `V2` - maintenance tasks
- `V3` - `auto_created` flag
- `V4` - dismissal reason for suggested tasks
- `V5` - base user table
- `V6` - expanded user profile fields
- `V7` - optional `location` and `bio`

## Runtime infrastructure

The project is split into two main root areas:

- `application` - the Kotlin/Spring application
- `sql` - local PostgreSQL startup through Docker Compose

The database runs as a separate `postgres:15-alpine` container.
The application connects through environment variables or local defaults.

## Configuration flags

### Authentication

- `APP_AUTH_ENABLED`

Controls open mode vs secured mode.

### Artificial response delay

- `APP_RESPONSE_DELAY_ENABLED`
- `APP_RESPONSE_DELAY_MIN_MS`
- `APP_RESPONSE_DELAY_MAX_MS`

This mechanism is useful for demo scenarios and for visually testing UI behavior under more realistic latency.

## Quality and testing

The project already contains:

- application startup smoke tests
- controller tests
- integration tests
- auth-enabled and auth-disabled scenarios
- coverage for summary, logs, deletion, and task lifecycle

## Architectural strengths

- clear separation of layers
- versioned database schema
- consistent API error format
- understandable REST contracts
- complete vertical slice from UI to database

## Architectural limitations

- ecosystems are not linked to users
- the frontend is not modular and will be harder to scale
- suggested-task rules are hardcoded
- notifications and scheduled care reminders are not implemented

## Recommendations for the next phase

1. Link `ecosystem` to `app_user`
2. Add edit/update flows for ecosystems, logs, and manual tasks
3. Move suggested-task rules into configuration or a dedicated table
4. Add notifications for overdue tasks
5. Move integration testing to PostgreSQL via Testcontainers

## Final conclusion

Architecturally, the project is already mature enough for a demo and a production-like learning scenario.
It clearly demonstrates how a simple tracking entity can grow into a connected product with business logic, security, persistence, UI, and tests.
