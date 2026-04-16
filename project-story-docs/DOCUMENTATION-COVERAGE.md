# Documentation Coverage

## Purpose

This file records:

- what is already covered by the documentation
- what was reviewed explicitly
- what is intentionally not documented in detail

This helps the team understand where the documentation is considered complete and where an area is not a gap because it was intentionally excluded from deeper documentation.

## 1. What is covered by the documentation

### Product and business level

- [x] product goal
- [x] user roles
- [x] system boundaries
- [x] core business entities
- [x] product value

### Functional modules

- [x] ecosystem management
- [x] activity and observation logging
- [x] maintenance task management
- [x] suggested task logic
- [x] authentication and profile
- [x] frontend user journey
- [x] workspace home dashboard journey

### API and integration

- [x] list of main endpoints
- [x] workspace dashboard endpoints
- [x] path parameters
- [x] query parameters
- [x] request body fields
- [x] field requirements and constraints
- [x] base response structures
- [x] paged response usage for home workspace cards
- [x] page routes outside `/api/v1`
- [x] references to Swagger / OpenAPI endpoints

### Architecture and data

- [x] layered architecture
- [x] purpose of controller/service/repository/model/dto/config layers
- [x] data model
- [x] database tables
- [x] Flyway migrations
- [x] infrastructure split between `application` and `sql`

### Runtime and delivery

- [x] application Dockerfile
- [x] `docker-compose.app.yml`
- [x] `sql/docker-compose.yml`
- [x] application Jenkins pipeline
- [x] sql Jenkins pipeline
- [x] manual HTTP collections
- [x] smoke test flow

### Technical implementation guidance

- [x] mapper layer
- [x] error handling
- [x] OpenAPI configuration
- [x] response delay implementation
- [x] repository usage pattern
- [x] workspace dashboard aggregation pattern
- [x] testing strategy

### Meta-documentation

- [x] modules overview
- [x] RU documentation set
- [x] EN documentation set
- [x] SQL module documented separately
- [x] operational layer documented separately

## 2. What was reviewed explicitly

The following were additionally re-checked manually:

- root folder structure
- controllers and services
- database migrations
- security configuration
- static frontend pages
- runtime and CI files
- HTTP test collections

## 3. What is intentionally not documented in detail

The following parts are not considered documentation gaps at the current level:

- `.idea` and IDE metadata
- `build/` artifacts
- `.gradle/` caches
- temporary IDE HTTP request logs
- generated classes and runtime outputs

Reason:

these are support or derived artifacts and are not a stable part of the architectural knowledge of the project.

## 4. What could be documented later if needed

These topics can be expanded later if there is a real need:

- detailed field-by-field data dictionary for all response DTOs
- sequence diagrams for main scenarios
- decision log / ADR documents
- non-functional requirements
- deployment guide for a production-like environment
- ownership model once the project moves toward a true multi-user domain

## 5. Final status

At this stage, the documentation can be considered:

- complete for business understanding
- complete for architectural review
- sufficient for backend/frontend integration
- sufficient for local runtime and operations
- sufficient for onboarding a new team member

## Final conclusion

If new requirements appear, the documentation can still be expanded.
But at the moment, no major documentation gaps remain recorded for the project.
