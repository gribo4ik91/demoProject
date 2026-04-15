# Story 08. SQL Area and PostgreSQL Environment

## You were right

There was no dedicated full description for the `sql` folder before.
The SQL area was only mentioned inside the broader architecture notes, so it was easy to miss it as a separate module.

## User story

As a developer or environment engineer, I want to start and stop the project database independently from the application so that I can run the backend locally, test migrations, and manage PostgreSQL without extra manual setup.

## Purpose of the `sql` folder

The `sql` folder in this project does not store business SQL scripts, stored procedures, or the main schema files.
Its role is different: it manages the infrastructure side of the database runtime.

In practice, it is an operational module for the local PostgreSQL environment.

## What is inside

### `docker-compose.yml`

This file starts the local PostgreSQL database for the project.

It defines:

- a `postgres:15-alpine` container
- container name `ecotracker-postgres`
- database `ecotracker_db`
- user `eco_user`
- password `eco_password`
- port mapping `5432:5432`
- a `postgres_data` volume for persistence
- a dedicated `ecotracker-network`

### `Jenkinsfile`

This pipeline is used to control the SQL environment through Jenkins.

It supports these actions:

- `up` - start the database container
- `down` - stop and remove the container
- `stop` - stop the container without removing it
- `logs` - show container logs
- `ps` - show current container status

## Business meaning of the SQL area

Even though the folder is called `sql`, it is not really a business database logic module.
It is an environment management module for the database runtime.

Its purpose is to:

- provide PostgreSQL for the application
- make local startup reproducible
- separate the database lifecycle from the application lifecycle

## Relationship to the application

The application in the `application` folder connects to the database through:

- `DB_URL`
- `DB_USERNAME`
- `DB_PASSWORD`

By default, it expects PostgreSQL at:

- `jdbc:postgresql://localhost:5432/ecotracker_db`

That directly matches the parameters defined in `sql/docker-compose.yml`.

## Where the actual schema lives

Important: the database structure itself is not stored in the `sql` folder.
It lives in the application:

- `application/application/src/main/resources/db/migration`

Those Flyway migrations create and evolve the schema:

- `ecosystems`
- `logs`
- `maintenance_tasks`
- `app_user`

So the responsibility split is:

- `sql` - PostgreSQL runtime environment
- `application/.../db/migration` - versioned database schema

## Architectural value of this split

This separation is useful because:

- database infrastructure is kept separate from business logic code
- schema evolution is versioned alongside the application
- local startup and CI database control remain simple

## Limitations of the current approach

- the `sql` folder has no seed data
- there are no dedicated backup/restore scripts
- there is no healthcheck or readiness orchestration
- database control is limited to basic Docker Compose commands

## Summary

The `sql` folder is valid and useful in this project, but it serves an infrastructure role rather than a domain SQL role.
You did not miss anything: the dedicated description was indeed missing, and it has now been added.
