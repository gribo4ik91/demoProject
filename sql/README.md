# SQL Module

This folder contains the local PostgreSQL setup notes for EcoTracker.

It is an infrastructure module, not a business SQL scripts module. The actual schema changes are managed by Flyway inside the application module.

This module is relevant only for the PostgreSQL startup variant. If you use `run-local.bat`, this folder is not required for startup.

## What Is Inside

- `docker-compose.yml` - optional helper to run PostgreSQL in Docker
- `Jenkinsfile` - allows Jenkins to start, stop, inspect, or remove the database container

## Default Database Setup

- Database: `ecotracker_db`
- Username: `eco_user`
- Password: `eco_password`
- Port: `5432`

When Docker is used, the helper image is `postgres:15-alpine` and the container name is `ecotracker-postgres`.

## PostgreSQL Variant Requirements

To run the application in PostgreSQL mode, you need:

- Java 21
- a locally installed PostgreSQL server

## Preferred Local PostgreSQL Setup

Install PostgreSQL locally, ensure the server is running, then create the application user and database once:

```powershell
psql -U postgres -c "CREATE USER eco_user WITH PASSWORD 'eco_password';"
psql -U postgres -c "CREATE DATABASE ecotracker_db OWNER eco_user;"
```

After that, start the application from the repository root with:

```powershell
run-postgres.bat
```

Flyway will create or upgrade the schema automatically.

## Optional Docker PostgreSQL Setup

This folder still contains the Docker helper files for PostgreSQL, but they are not part of the normal Windows flow documented in the main README files.
Use them only if you intentionally run Docker through WSL or another supported environment.

Start the database:

```powershell
cd path\to\demoProject\sql
docker compose up -d
```

Check running containers:

```powershell
cd path\to\demoProject\sql
docker compose ps
```

Show logs:

```powershell
cd path\to\demoProject\sql
docker compose logs
```

Stop and remove the container:

```powershell
cd path\to\demoProject\sql
docker compose down
```

## Relationship to the Application

The backend connects to this database with the following defaults:

- `DB_URL=jdbc:postgresql://localhost:5432/ecotracker_db`
- `DB_USERNAME=eco_user`
- `DB_PASSWORD=eco_password`

When the application starts, Flyway applies migrations from:

- `application/application/src/main/resources/db/migration`

The main application profile also enables automatic creation of the default bootstrap user when the user table is empty.
If you want PostgreSQL mode without that user, change [`application/application/src/main/resources/application.yml`](C:\Endava\EndevLocal\demoProject\application\application\src\main\resources\application.yml) and set `app.auth.default-user.enabled: false`.

If you want the fastest local startup without PostgreSQL at all, use `run-local.bat` from the repository root. That mode uses `H2`, disables Flyway, and does not affect this Docker setup.

## Startup Order

1. Start PostgreSQL locally or through the optional Docker helper
2. Start the application from `application/application`
3. Let Flyway create or update the schema
4. Open `http://localhost:8085`

## Notes

- When Docker is used, data is persisted in the `postgres_data` volume
- This folder does not contain seed data or standalone schema scripts
- The module is meant to keep database lifecycle management separate from application code
- A working Docker environment is also required when running integration tests that use Testcontainers from the application module
