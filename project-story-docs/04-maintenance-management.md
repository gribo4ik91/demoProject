# Story 04. Maintenance Task Management

## User story

As a user, I want to manage maintenance tasks for an ecosystem so that I do not forget required actions and can quickly identify overdue or system-suggested follow-ups.

## What this feature solves

This module turns observations into actions.
The user gets not only a history, but also an operational worklist.

## Task types

There are two sources of tasks in the system:

- manual tasks created by the user
- automatically suggested tasks created by the system

## Main scenarios

### Scenario 1. Create a manual task

The user creates a task and defines:

- title
- task type
- due date

The system saves the task with status `OPEN`.

### Scenario 2. View tasks

The user opens the task section.
The system shows a sorted list with filters.

Supported backend filters:

- `ALL`
- `OPEN`
- `DONE`
- `OVERDUE`
- `DISMISSED`

The frontend also provides a source filter:

- all
- manual
- suggested

### Scenario 3. Mark a task as done

The user marks a task as completed.
The system changes the status to `DONE`.

### Scenario 4. Reopen a task

The user can return a task to active work.
The system changes the status back to `OPEN`.

### Scenario 5. Dismiss a suggested task

If a task was created automatically and is currently not relevant, the user can dismiss it with one of these reasons:

- `TOO_SOON`
- `NOT_RELEVANT`
- `ALREADY_HANDLED`

The system moves the task to `DISMISSED` and stores the reason.

### Scenario 6. Edit a manual task

The user updates the title, type, or due date of a manually created reminder.
The system:

- verifies that the task belongs to the selected ecosystem
- validates the updated fields
- allows the change only for manual tasks
- rejects manual editing for auto-created suggestions

## Suggested task engine

The system can automatically create follow-up tasks using configurable automation rules.

Current rule families in the MVP:

- `AFTER_EVENT`
- `AFTER_INACTIVITY`

Current runtime execution behavior:

- enabled `AFTER_EVENT` rules are evaluated when a new log is saved
- rules can target all ecosystems or one ecosystem type
- each rule defines the event type, optional delay, generated task title, generated task type, and duplicate-prevention behavior

Suggested tasks created through rules:

- use `autoCreated = true`
- are created with status `OPEN`
- receive a due date derived from the matching rule for event-based scenarios

Suggested tasks do not appear:

- if no enabled rule matches the ecosystem and event
- if an identical open suggested task already exists and the rule blocks duplicates
- if the latest dismissed matching suggestion is still inside its cooldown window

## Duplicate prevention

If an open suggested task with the same title and type already exists, a new one is not created.

## Cooldown after dismiss

If a suggested task was dismissed, the system will not recreate the same one immediately.

The cooldown depends on the dismissal reason:

- `TOO_SOON` -> 3 days
- `ALREADY_HANDLED` -> 7 days
- `NOT_RELEVANT` -> 30 days

## Status business rules

- allowed statuses are `OPEN`, `DONE`, and `DISMISSED`
- only auto-created tasks can be dismissed
- suggested tasks cannot be edited directly; only their status can change
- a dismissal reason is mandatory for `DISMISSED`
- a dismissal reason cannot be sent for `OPEN` or `DONE`
- a task is overdue when its status is `OPEN` and `dueDate < today`

## Task sorting

The system shows tasks in this priority order:

1. `OPEN`
2. `DONE`
3. `DISMISSED`

Within the same status, sorting uses:

- nearest `dueDate`
- then newest `createdAt`

## API operations

- `POST /api/v1/ecosystems/{ecosystemId}/tasks`
- `PATCH /api/v1/ecosystems/{ecosystemId}/tasks/{taskId}`
- `GET /api/v1/ecosystems/{ecosystemId}/tasks`
- `PATCH /api/v1/ecosystems/{ecosystemId}/tasks/{taskId}/status`

## Architectural implementation

- `MaintenanceTaskController` exposes the API
- `MaintenanceTaskService` implements filters, status rules, suggestion creation, and cooldown logic
- `AutomationRuleController` exposes rule-management API operations
- `AutomationRuleService` owns rule validation, CRUD behavior, and rule lookup for task generation
- `MaintenanceTaskRepository` works with the `maintenance_tasks` table
- `AutomationRuleRepository` works with the `automation_rules` table

## Summary

This module turns EcoTracker from a passive journal into an operational care tool.
It gives the user practical value by showing what needs attention now, what is overdue, and which checks the system suggests automatically.
