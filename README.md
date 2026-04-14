# EcoTracker Demo Project

EcoTracker is a demo full-stack project for tracking small ecosystems such as formicariums, florariums, indoor plant setups, and DIY incubators.

The repository is split into three main areas:

- `application/application` - the Kotlin + Spring Boot web application
- `sql` - Docker Compose files for the local PostgreSQL database
- `project-story-docs` - product, architecture, and delivery documentation

## Main Capabilities

- Create and browse ecosystems
- Open a dedicated ecosystem dashboard
- Record activity logs and observations
- Manage maintenance tasks
- Use optional user registration, login, and profile features
- Persist data in PostgreSQL with Flyway migrations

## Tech Stack

- Java 21
- Kotlin 2.2
- Spring Boot 4
- Spring Web MVC
- Spring Data JPA
- PostgreSQL 15
- Flyway
- H2 for tests
- Gradle Kotlin DSL
- Static HTML + Bootstrap frontend

## How to Run the Project

### Prerequisites

- Java 21
- Docker Desktop or another Docker runtime with Compose support

### Startup Sequence

1. Start PostgreSQL from the `sql` folder:

```powershell
cd path\to\demoProject\sql
docker compose up -d
```

2. Start the Spring Boot application:

```powershell
cd path\to\demoProject\application\application
.\gradlew.bat bootRun
```

3. Open the application in the browser:

- App: `http://localhost:8085`
- Swagger UI: `http://localhost:8085/swagger-ui.html`

### What Happens During Startup

1. Docker starts PostgreSQL on port `5432`
2. The Spring Boot application starts on port `8085`
3. Flyway applies database migrations from `application/application/src/main/resources/db/migration`
4. Security rules are loaded
5. Static frontend pages are served by the backend

## Default Local Configuration

- Database URL: `jdbc:postgresql://localhost:5432/ecotracker_db`
- Database user: `eco_user`
- Database password: `eco_password`
- Application port: `8085`
- Authentication: enabled by default

If authentication is enabled, the first screen for anonymous users is the login page. A local user can be created through the registration flow.

## Useful Commands

Run tests:

```powershell
cd path\to\demoProject\application\application
.\gradlew.bat test
```

Stop the database:

```powershell
cd path\to\demoProject\sql
docker compose down
```

## Where to Find More Details

- Application details: [application/application/README.md](application/application/README.md)
- SQL environment details: [sql/README.md](sql/README.md)
- Story-style project documentation: [project-story-docs/README.md](project-story-docs/README.md)
