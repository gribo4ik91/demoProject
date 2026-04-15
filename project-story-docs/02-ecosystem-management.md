# Story 02. Ecosystem Management

## User story

As a user, I want to create, update, and browse ecosystems so that I can manage each setup separately and keep its dashboard context accurate over time.

## Value

Without the ecosystem entity, the rest of the product has no context.
The ecosystem is the root object for logs, maintenance tasks, and the summary view.

## Main scenarios

### Scenario 1. Create an ecosystem

The user enters:

- a name
- a type
- a description if needed

The system:

- validates the required fields
- stores the new ecosystem
- returns the created record
- makes it available in the main list

### Scenario 2. View the ecosystem list

The user opens the home page.
The system shows all registered ecosystems as cards, each linking to its dedicated dashboard.

### Scenario 3. View ecosystem details

The user opens a specific ecosystem.
The system loads:

- the base ecosystem data
- the summary
- maintenance tasks
- the activity log

### Scenario 4. Delete an ecosystem

The user deletes an ecosystem.
The system:

- checks that it exists
- deletes the ecosystem itself
- deletes related logs
- deletes related maintenance tasks

### Scenario 5. Update ecosystem details

The user edits the main ecosystem information from the dashboard.
The system:

- loads the current name, type, and description
- validates the updated values
- saves the changes
- refreshes the displayed dashboard context

## Business rules

- `name` is required, up to 100 characters
- `type` is required, up to 50 characters
- UI-supported types are `FORMICARIUM`, `FLORARIUM`, `INDOOR_PLANTS`, and `DIY_INCUBATOR`
- `description` is optional, up to 500 characters
- when an ecosystem is deleted, dependent data must also be removed

## API operations

- `POST /api/v1/ecosystems`
- `PATCH /api/v1/ecosystems/{id}`
- `GET /api/v1/ecosystems`
- `GET /api/v1/ecosystems/{id}`
- `GET /api/v1/ecosystems/{id}/summary`
- `DELETE /api/v1/ecosystems/{id}`

## What the summary does

The summary acts as a quick business health panel.
It combines:

- the latest log
- the latest 5 logs for averages
- the number of logs in the last 7 days
- the number of logs in the last 30 days
- the number of distinct active logging days in the last 30 days
- the current logging streak in days
- lightweight deltas between the latest measurable window and the previous one
- the number of open tasks
- the number of overdue tasks

Based on that, the system assigns one of these statuses:

- `STABLE`
- `NEEDS_ATTENTION`
- `NO_RECENT_DATA`

## Status calculation rules

`NEEDS_ATTENTION` is returned when:

- there are overdue tasks
- humidity is below 35%
- temperature is below 18 C or above 30 C
- there are 3 or more open tasks

`NO_RECENT_DATA` is returned when:

- there is no latest log
- or there was no activity during the last 7 days

Otherwise the status is `STABLE`.

## Architectural implementation

- `EcosystemController` exposes the API
- `EcosystemService` contains the business logic
- `EcosystemRepository` works with the `ecosystems` table
- `EcosystemLogRepository` and `MaintenanceTaskRepository` are used to build the summary

## Summary

Ecosystem management is the root context of the product.
It defines the working space in which all other business processes operate.
