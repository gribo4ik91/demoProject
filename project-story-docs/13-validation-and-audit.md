# Story 13. Validation, Duplicate Protection, and Audit Trail

## User story

As a user working in a shared ecosystem inventory, I want invalid or duplicate data to be rejected and every important inventory change to be visible so that the workspace stays clean, trustworthy, and explainable.

## Purpose

This story documents the cross-cutting rules that now protect the main workflows:

- field-level validation for supported input values
- business duplicate checks before persistence
- audit logging for changes to ecosystems, activity logs, maintenance tasks, and automation rules

The goal is to keep the application from accepting data that the UI cannot represent safely or that would make the workspace ambiguous.

## Validated inputs

### Users

- `username` is required, 3-40 characters, and may contain lowercase letters, numbers, dots, underscores, and hyphens
- `username` is checked case-insensitively for duplicates
- `email` is required, valid, up to 120 characters, normalized to lowercase, and checked case-insensitively for duplicates
- `password` is required and must be 8-72 characters
- display and person names are constrained to letters, spaces, and basic name punctuation
- `location` is optional and limited to simple location punctuation

### Ecosystems

- `name` is required, up to 100 characters, and checked case-insensitively for duplicates
- `type` is required and must be one of `FORMICARIUM`, `FLORARIUM`, `INDOOR_PLANTS`, or `DIY_INCUBATOR`
- `description` is required and limited to 500 characters

### Activity logs

- `eventType` is required and must be one of `OBSERVATION`, `FEEDING`, or `WATERING`
- `temperatureC`, when present, must be from -100 to 100
- `humidityPercent`, when present, must be from 0 to 100
- `notes` is optional and limited to 500 characters
- a log must include at least one meaningful detail: temperature, humidity, or notes

### Maintenance tasks

- `title` is required, up to 120 characters, and limited to common task punctuation
- `taskType` must be one of `WATERING`, `FEEDING`, `CLEANING`, or `INSPECTION`
- `status` must be one of `OPEN`, `DONE`, or `DISMISSED`
- `dismissalReason`, when used, must be one of `TOO_SOON`, `NOT_RELEVANT`, or `ALREADY_HANDLED`
- duplicate open manual tasks are rejected when ecosystem, title, task type, and due date match an existing open manual task

## Duplicate protection

Duplicate checks are implemented in services because they express business rules rather than only field shape:

- `AuthService` blocks duplicate usernames and emails
- `EcosystemService` blocks duplicate ecosystem names
- `MaintenanceTaskService` blocks duplicate open manual task signatures

The API returns controlled conflict responses for duplicate business data.

## Audit trail scope

The audit trail records inventory-visible changes:

- ecosystem created
- ecosystem fields updated
- ecosystem deleted
- activity log created
- activity log fields updated
- manual maintenance task created
- manual maintenance task fields updated
- maintenance task status or dismissal reason changed
- suggested task created by automation rules
- automation rule created
- automation rule fields updated
- automation rule enabled state changed
- automation rule deleted

For field updates, each changed field is stored separately with old and new values.

## Audit data model

Audit entries are stored in `audit_logs`.

The table stores:

- `entity_type`
- `entity_id`
- `entity_name`
- `action`
- `field_name`
- `old_value`
- `new_value`
- `created_by_username`
- `created_by_display_name`
- `created_at`

The table is created by Flyway migration `V11__add_audit_logs.sql`.

## UI behavior

The home page shows a compact recent-changes preview in the workspace panel.
The full paged audit history is available on `/audit`.

Each entry displays:

- action badge
- entity type
- entity name
- changed field with old and new values when applicable
- actor label
- timestamp

This keeps the home page lightweight while still making older entries reachable in the dedicated audit view.

## Architectural implementation

- request DTOs contain Bean Validation annotations and shared regex patterns from `ValidationPatterns`
- services perform duplicate checks and business-only validation
- `AuditLogService` writes audit entries and provides both compact preview and paged read models
- `AuditLogRepository` provides the Spring Data paging access
- `UiController` adds a compact audit preview to the home page model and paged audit entries to `/audit`
- `UiSupport` formats audit labels and badge classes for Freemarker templates

## Summary

These changes make EcoTracker stricter and more accountable.
The system now rejects invalid values at the boundary, blocks ambiguous duplicates in the service layer, and records a visible audit trail for the inventory changes that matter to users.
