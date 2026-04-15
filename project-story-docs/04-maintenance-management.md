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

The system can automatically create follow-up tasks using simple rules:

- after `WATERING`, create `Inspect moisture balance after watering`
- after `FEEDING`, create `Log feeding response check`

Both tasks:

- use type `INSPECTION`
- are created as `autoCreated = true`
- receive a due date of `today + 1 day`

Suggested tasks appear only in these cases:

- after a new `WATERING` log is saved
- after a new `FEEDING` log is saved

Suggested tasks do not appear:

- after `OBSERVATION`
- if an identical open suggested task already exists
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
- `MaintenanceTaskService` implements filters, status rules, suggestion logic, and cooldown logic
- `MaintenanceTaskRepository` works with the `maintenance_tasks` table

## Summary

This module turns EcoTracker from a passive journal into an operational care tool.
It gives the user practical value by showing what needs attention now, what is overdue, and which checks the system suggests automatically.
