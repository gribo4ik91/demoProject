# EcoTracker: project documentation in story format

## Purpose

This folder describes the `EcoTracker` project as a set of business stories and architectural decisions.
The documentation is written from the combined perspective of a business analyst and a solution architect: what business problem the system solves, who uses it, which scenarios it supports, and how those scenarios are implemented across the modules.

For runtime startup instructions, use the repository READMEs:

- root [`README.md`](../README.md) for the two supported startup variants
- [`application/application/README.md`](../application/application/README.md) for app-level run details
- [`sql/README.md`](../sql/README.md) for the PostgreSQL-specific path

## Documentation structure

- `DOCUMENTATION-COVERAGE.md` - coverage checklist and list of areas intentionally not documented in detail
- `MODULES-OVERVIEW.md` - compact map of project modules, purposes, and key files
- `01-product-overview.md` - overall product story, goals, roles, and system boundaries
- `02-ecosystem-management.md` - ecosystem creation, browsing, and deletion
- `03-activity-logging.md` - activity, observation logging, and log editing
- `04-maintenance-management.md` - maintenance task management, status handling, and manual task editing
- `05-authentication-and-profile.md` - registration, login, profile, and security modes
- `06-frontend-journey.md` - user journey through the web interface
- `07-architecture-and-data.md` - architecture, layers, entities, database, and infrastructure
- `07a-api-endpoints-reference.md` - reference for all endpoints, parameters, query/body fields, and responses
- `08-sql-module.md` - dedicated description of the SQL area, PostgreSQL environment, and pipeline control
- `09-delivery-and-operations.md` - containerization, application pipeline, manual API collections, and runtime operations
- `10-implementation-notes.md` - implementation rules for mappers, errors, OpenAPI, response delay, and tests

## How to read it

For a quick project walkthrough:

1. Start with `MODULES-OVERVIEW.md`
2. Continue with `01-product-overview.md`
3. Then read the functional story files
4. Finish with `07-architecture-and-data.md`

## Short conclusion

`EcoTracker` is a full-stack application for managing small ecosystems such as formicariums, florariums, indoor plant setups, and DIY incubators.
The product combines:

- an ecosystem catalog
- a dedicated ecosystem workspace
- an activity and observation log
- manual and auto-suggested maintenance tasks
- optional authentication, user profile management, role-based user administration, and creator attribution
- PostgreSQL + Flyway for stable persistence

The main value of the system is not only to store records, but to help the user understand the current state of an ecosystem, notice risks early, and stay on top of recurring care actions.

Recent iterations also added inline dashboard editing flows and broader summary analytics so the main working screen stays useful without leaving the ecosystem page.
The current UI delivery model is server-side rendered through Freemarker templates with htmx updates for focused interactions.
