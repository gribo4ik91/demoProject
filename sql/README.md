# SQL Module

This folder contains the local PostgreSQL runtime setup for EcoTracker.

It is an infrastructure module, not a business SQL scripts module. The actual schema changes are managed by Flyway inside the application module.

## What Is Inside

- `docker-compose.yml` - starts PostgreSQL for local development
- `Jenkinsfile` - allows Jenkins to start, stop, inspect, or remove the database container

## Default Database Setup

- Image: `postgres:15-alpine`
- Container name: `ecotracker-postgres`
- Database: `ecotracker_db`
- Username: `eco_user`
- Password: `eco_password`
- Port: `5432`

## How to Use It

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

## Startup Order

1. Start PostgreSQL from this folder
2. Start the application from `application/application`
3. Let Flyway create or update the schema
4. Open `http://localhost:8085`

## Notes

- Data is persisted in the `postgres_data` Docker volume
- This folder does not contain seed data or standalone schema scripts
- The module is meant to keep database lifecycle management separate from application code
- A working Docker environment is also required when running integration tests that use Testcontainers from the application module
