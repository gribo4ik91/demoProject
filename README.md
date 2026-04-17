# EcoTracker Demo Project

EcoTracker is a demo full-stack project for tracking small ecosystems such as formicariums, florariums, indoor plant setups, and DIY incubators.

The repository is split into three main areas:

- `application/application` - the Kotlin + Spring Boot web application
- `sql` - local PostgreSQL setup notes plus optional Docker helper files
- `project-story-docs` - product, architecture, and delivery documentation

## Main Capabilities

- Create and browse ecosystems
- Edit ecosystem metadata directly from the dashboard
- Open a dedicated ecosystem dashboard
- Record activity logs and observations
- Edit existing logs inline from the dashboard
- Manage maintenance tasks
- Edit manual maintenance tasks inline from the dashboard
- View richer summary analytics including recent readings, 30-day activity, logging streaks, and lightweight trend indicators
- Use optional user registration, login, profile, and lightweight role-based access
- Automatically assign the first account in the system to `SUPER_ADMIN`
- Let every signed-in account view the shared users directory, allow `SUPER_ADMIN` to manage admins, and restrict `ADMIN` deletion rights to regular users
- Show who created each ecosystem, log, and task entry
- Persist data in PostgreSQL with Flyway migrations

## Tech Stack

- Java 21
- Kotlin 2.2
- Spring Boot 4
- Spring Web MVC
- Spring Data JPA
- PostgreSQL 15
- Flyway
- Testcontainers + PostgreSQL for integration tests
- Gradle Kotlin DSL
- Server-side rendered Freemarker + htmx frontend

## Quick Start

If PostgreSQL is already installed and running locally:

```powershell
run-postgres.bat
```

If you want the fastest startup without PostgreSQL and without Docker:

```powershell
run-local.bat
```

After startup, open:

- App: `http://localhost:8085`
- Swagger UI: `http://localhost:8085/swagger-ui.html`

## How to Run the Project

### Prerequisites

- Java 21

There are two supported local startup modes.

### Variant 1: PostgreSQL Mode

Use this when you want the real database behavior with PostgreSQL and Flyway migrations.

What you need:

- Java 21
- PostgreSQL on `localhost:5432`

If PostgreSQL is installed locally, create the database and user once:

```powershell
psql -U postgres -c "CREATE USER eco_user WITH PASSWORD 'eco_password';"
psql -U postgres -c "CREATE DATABASE ecotracker_db OWNER eco_user;"
```

Then start the app:

```powershell
run-postgres.bat
```

What happens in this mode:

- the app connects to PostgreSQL
- Flyway applies migrations from `application/application/src/main/resources/db/migration`
- this is the closest mode to the intended runtime database behavior
- a default user is created automatically if the user table is still empty

### Variant 2: Fast Local Mode

Use this when you want the simplest startup in 1-2 clicks without PostgreSQL and without Docker.

What you need:

- Java 21

Start the app:

```powershell
run-local.bat
```

What happens in this mode:

- the app starts with an in-memory `H2` database
- Flyway is disabled for this profile
- Hibernate creates the schema automatically
- data is reset after each restart
- a default user is created automatically

Useful local details:

- App: `http://localhost:8085`
- Swagger UI: `http://localhost:8085/swagger-ui.html`
- H2 console: `http://localhost:8085/h2-console`
- Default login: `demo_user_auth`
- Default password: `secret123`

### PostgreSQL Startup Sequence

1. Start PostgreSQL in the way you prefer:

- locally installed PostgreSQL

2. Start the Spring Boot application:

```powershell
run-postgres.bat
```

3. Open the application in the browser:

- App: `http://localhost:8085`
- Swagger UI: `http://localhost:8085/swagger-ui.html`

### What Happens During PostgreSQL Startup

1. The selected database starts
2. The Spring Boot application starts on port `8085`
3. Flyway applies database migrations from `application/application/src/main/resources/db/migration`
4. Security rules are loaded
5. Freemarker pages and htmx fragments are served by the backend

## Default Local Configuration

- Database URL: `jdbc:postgresql://localhost:5432/ecotracker_db`
- Database user: `eco_user`
- Database password: `eco_password`
- Application port: `8085`
- Authentication: enabled by default
- Default user bootstrap: enabled in both the main profile and the `local` profile

If authentication is enabled, the first screen for anonymous users is the login page. A local user can be created through the registration flow, and the first account in the system becomes `SUPER_ADMIN`.

Default user bootstrap can be configured with:

- `APP_AUTH_DEFAULT_USER_ENABLED`
- `APP_AUTH_DEFAULT_USER_USERNAME`
- `APP_AUTH_DEFAULT_USER_PASSWORD`

To disable automatic bootstrap user creation, change:

- [`app.auth.default-user.enabled`](application/application/src/main/resources/application.yml)
- [`app.auth.default-user.enabled`](application/application/src/main/resources/application-local.yml)

Set the value to `false` in the profile you want to run without a pre-created user.

## Demo Scenarios

The application can be demonstrated in three main modes depending on authentication flags.

### Scenario 1: Authentication enabled with bootstrap user

Use this when:

- `APP_AUTH_ENABLED=true`
- `APP_AUTH_DEFAULT_USER_ENABLED=true`

What the user does:

1. Open `http://localhost:8085`.
2. Sign in with the bootstrap account such as `demo_user_auth / secret123`.
3. Open the home page and create a new ecosystem.
4. Add a log entry like `WATERING` or `OBSERVATION`.
5. Add a manual maintenance task with a due date.
6. Open the ecosystem page to review summary, activity history, and tasks.
7. Open `/users` to see the shared directory.
8. As the bootstrap `SUPER_ADMIN`, promote another account to `ADMIN` or remove that right again.


### Scenario 2: Authentication enabled without bootstrap user

Use this when:

- `APP_AUTH_ENABLED=true`
- `APP_AUTH_DEFAULT_USER_ENABLED=false`

What the user does:

1. Open `http://localhost:8085` and go to `/register`.
2. Register the very first account in the environment.
3. Sign in and confirm that this account is now `SUPER_ADMIN`.
4. Register one more account from the registration page.
5. Sign back in as the first user and open `/users`.
6. Promote the second account to `ADMIN`.
7. Verify that `ADMIN` can delete only regular `USER` accounts, while `SUPER_ADMIN` can also remove admins.


### Scenario 3: Authentication disabled

Use this when:

- `APP_AUTH_ENABLED=false`

What the user does:

1. Open `http://localhost:8085`.
2. Enter the workspace directly without login.
3. Create several ecosystems from the home page.
4. Open one ecosystem and add logs and tasks.
5. Edit ecosystem details, logs, and manual tasks inline.


### Suggested short live demo

If you want one compact walkthrough for a presentation:

1. Start with authentication enabled and log in as the bootstrap `SUPER_ADMIN`.
2. Create one ecosystem and explain the workspace card, status, and quick actions.
3. Open the ecosystem and add one log plus one task.
4. Show how creator information is attached to the records.
5. Open `/users`, promote another account to `ADMIN`, and explain the difference between `SUPER_ADMIN`, `ADMIN`, and `USER`.
6. Mention that the same project can also run with `APP_AUTH_ENABLED=false` for a faster no-login demo.

## Useful Commands

Start the app without PostgreSQL or Docker:

```powershell
run-local.bat
```

Start the app against PostgreSQL:

```powershell
run-postgres.bat
```

Run tests:

```powershell
cd path\to\demoProject\application\application
.\gradlew.bat test
```

Note:

- integration tests still rely on Testcontainers and therefore require a working Docker environment
- the application runtime itself no longer needs Docker if PostgreSQL is installed locally
- if Docker is not available locally, compilation and non-containerized tests can still be run

Compile the project and test sources:

```powershell
cd path\to\demoProject\application\application
.\gradlew.bat compileKotlin compileTestKotlin
```

## Where to Find More Details

- Application details: [application/application/README.md](application/application/README.md)
- SQL environment details: [sql/README.md](sql/README.md)
- Story-style project documentation: [project-story-docs/README.md](project-story-docs/README.md)
