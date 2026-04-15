# Story 09. Delivery, Operations, and Runtime Flow

## Why I added this file

After a second careful review of the project, I found one more area that had only been partially covered:

- application containerization
- application pipeline behavior
- manual HTTP API collections
- operational startup and smoke-check flows

This is not a core business feature, but it is an important part of the full project description.

## User story

As a developer, QA engineer, or DevOps engineer, I want a predictable way to build, test, run, and quickly verify the application so that the team can work with the project reliably in local and CI environments.

## What belongs to the application's operational layer

### `Dockerfile`

Purpose:

- package the application into a runtime image
- run the Spring Boot jar inside a container

How it works:

- base image `eclipse-temurin:21-jre`
- working directory `/app`
- copies `build/libs/*-SNAPSHOT.jar`
- exposes port `8085`

Conclusion:

This is the production-like packaging layer for the backend application.

### `docker-compose.app.yml`

Purpose:

- run the application container separately from the database
- connect the application to the shared `ecotracker-network`

It defines:

- container `ecotracker-app`
- build from `Dockerfile`
- port mapping `8085:8085`
- PostgreSQL connection settings
- `APP_AUTH_ENABLED`
- `APP_RESPONSE_DELAY_*` flags

Architectural meaning:

This file connects the application runtime to the SQL runtime.

### `application/application/Jenkinsfile`

Purpose:

- automate build, test, package, and runtime scenarios for the application

Supported actions:

- `full`
- `build`
- `test`
- `jar`
- `image`
- `up`
- `down`
- `restart`
- `logs`
- `ps`

Important behavior:

- the pipeline can run Gradle commands
- it can build a Docker image
- it can start the container runtime
- in `full` mode it performs a smoke test against `http://localhost:8085/api/v1/auth/status`
- JUnit results are published from `build/test-results/test/*.xml`

## Manual API collections

### `api-tests.http`

This file is used for manual testing of the main application flow in an open or simplified mode.

It covers:

- ecosystems
- summary
- logs
- tasks
- validation error scenarios

### `api-tests-auth-enabled.http`

This file is used for manual testing when authentication is enabled.

It covers:

- `auth/status`
- registration
- login/logout
- profile
- then the full functional flow for ecosystems, logs, and tasks

## Operational map of the project

The project currently supports several execution paths:

1. Local startup through Gradle
2. Local startup through Docker Compose
3. Jenkins-based execution
4. Manual verification through HTTP collections

That means the project documents not only development, but also its basic operational lifecycle.

## Important note about `Makefile`

The project also contains a `Makefile`, but based on its current content it does not appear to be the main or fully current startup path:

- it uses `APP_DIR = ./api`
- it contains placeholder comments for future API tests
- it looks like an auxiliary or legacy artifact

Because of that, it should not be treated as the primary source of operational truth.
The main operational sources are currently:

- `README.md`
- `Jenkinsfile`
- `docker-compose.app.yml`
- `sql/docker-compose.yml`

## Final conclusion

After the second review, the documentation can now be considered to cover:

- business features
- frontend flow
- security
- database structure
- SQL runtime
- container runtime
- CI/CD-like processes
- manual smoke and API verification paths

That makes the project description much closer to a complete project passport, not only a set of business stories.
