# Story 03. Activity and Observation Logging

## User story

As a user, I want to record observations and actions for an ecosystem so that I can see how the environment changes over time and keep a history of care events.

## Purpose of the feature

The activity log is the main source of factual data.
It is used for:

- recording temperature
- recording humidity
- recording event types
- storing notes
- building the summary
- triggering suggested tasks

## Main scenarios

### Scenario 1. Add a log entry

The user selects an ecosystem and enters:

- temperature
- humidity
- event type
- a note

The system:

- verifies that the ecosystem exists
- normalizes the event type to uppercase
- creates the log entry
- triggers the suggested task mechanism when applicable

### Scenario 2. Browse logs

The user opens the log section.
The system returns entries:

- with pagination
- ordered from newest to oldest
- optionally filtered by event type

### Scenario 3. Filter logs

The user wants to see only a specific event type, such as `WATERING`.
The system returns only matching records while preserving page navigation.

### Scenario 4. Edit a log entry

The user updates an existing log when a note or measurement needs correction.
The system:

- verifies that both the ecosystem and log exist
- validates the updated values
- preserves the log ownership and timestamp
- returns the updated record

## Business rules

- `temperatureC` may be empty, but if present must be between -100 and 100
- `humidityPercent` may be empty, but if present must be between 0 and 100
- `eventType` is required
- UI-supported values are `OBSERVATION`, `FEEDING`, and `WATERING`
- `notes` is optional, maximum 500 characters
- log page size is constrained to a range from 1 to 50
- page index cannot be less than 0

## Business meaning of event types

- `OBSERVATION`: a regular status note without mandatory follow-up
- `FEEDING`: a care action that may require a later response check
- `WATERING`: a watering or humidity action that may require a later moisture balance check

## Relationship to other features

The log is tightly connected to two other parts of the product:

1. Summary
   Recent logs affect the ecosystem status and average readings.

2. Suggested maintenance
   `WATERING` and `FEEDING` logs may create automatic follow-up tasks.

## API operations

- `POST /api/v1/ecosystems/{ecosystemId}/logs`
- `PATCH /api/v1/ecosystems/{ecosystemId}/logs/{logId}`
- `GET /api/v1/ecosystems/{ecosystemId}/logs`

## Architectural implementation

- `EcosystemLogController` manages the HTTP endpoints
- `EcosystemLogService` creates and reads log data
- `EcosystemLogRepository` supports filtering and pagination
- `MaintenanceTaskService` is called after saving a log to create suggested tasks

## Role in the product

If the ecosystem is the tracked object, the log is the factual history of that object.
It turns a static record into a living operational journal.
