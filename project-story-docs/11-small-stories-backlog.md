# EcoTracker Small Stories Backlog

## Purpose

This file converts the existing product stories into smaller delivery-ready backlog items.
Each story is focused on one concrete piece of behavior so the team can estimate it and take it into implementation with limited ambiguity.

## Suggested format

For each story:

- user value is explicit
- scope is intentionally narrow
- acceptance criteria describe observable behavior
- dependencies help sequence the work

## Epic A. Ecosystem workspace foundation

### ST-01 Create ecosystem from home page

**Story**
As a user, I want to create a new ecosystem from the home page so that I can start tracking a new setup without using API tools.

**Scope**

- create ecosystem form on the home page
- validation of `name`, `type`, and optional `description`
- persist ecosystem through `POST /api/v1/ecosystems`
- show the created ecosystem in the workspace list

**Acceptance criteria**

- user can submit `name` and `type`
- system rejects blank required fields
- system rejects `name` longer than 100 characters
- system rejects `type` longer than 50 characters
- successful creation shows the new ecosystem in the workspace card list

**Dependencies**

- none

### ST-02 View workspace ecosystem cards

**Story**
As a user, I want to see all ecosystems as workspace cards so that I can quickly open the one I want to work with.

**Scope**

- load paged ecosystem cards from `GET /api/v1/ecosystems/cards`
- show name, type, status, last activity, open tasks, and overdue tasks
- open selected ecosystem dashboard

**Acceptance criteria**

- first page of cards loads on home page open
- each card links to the ecosystem dashboard
- cards show backend-computed status
- page supports incremental loading through pagination

**Dependencies**

- ST-01

### ST-03 Filter and search workspace cards

**Story**
As a user, I want to search and filter ecosystem cards so that I can find relevant ecosystems faster in a larger workspace.

**Scope**

- server-side search by text
- server-side filter by status
- server-side sorting
- synced overview counters for the current filter

**Acceptance criteria**

- search applies to name, type, and description
- status filter supports `ALL`, `NEEDS_ATTENTION`, `STABLE`, `NO_RECENT_DATA`, and `OVERDUE`
- sorting supports `PRIORITY`, `LAST_ACTIVITY`, `NAME`, and `NEWEST`
- counters reflect the same filtered result set as the cards

**Dependencies**

- ST-02

### ST-04 Edit ecosystem details from dashboard

**Story**
As a user, I want to update ecosystem metadata from its dashboard so that the workspace stays accurate when the setup changes.

**Scope**

- load current ecosystem values
- edit `name`, `type`, and `description`
- save through `PATCH /api/v1/ecosystems/{id}`
- refresh dashboard context after save

**Acceptance criteria**

- dashboard displays current ecosystem data
- user can update allowed fields
- invalid values are rejected with validation feedback
- dashboard header reflects saved changes without reopening the page

**Dependencies**

- ST-02

### ST-05 Delete ecosystem with dependent data cleanup

**Story**
As a user, I want to delete an ecosystem I no longer track so that obsolete dashboards, logs, and tasks do not remain in the workspace.

**Scope**

- delete action from ecosystem dashboard
- delete ecosystem through `DELETE /api/v1/ecosystems/{id}`
- remove related logs and tasks

## Epic D. User governance and attribution

### ST-21 Assign super-admin role to the first registered user

**Story**
As a product owner, I want the first registered account to become the super admin automatically so that the application always has one simple governance owner.

**Scope**

- assign `SUPER_ADMIN` to the first created user
- assign `USER` to every later registration
- expose role in profile and auth-status responses

**Acceptance criteria**

- first registration returns role `SUPER_ADMIN`
- second and later registrations return role `USER`
- signed-in clients can see the current role

**Dependencies**

- existing authentication flow

### ST-22 View all users in a shared directory

**Story**
As an authenticated user, I want to see all registered accounts so that I know who can access the workspace.

**Scope**

- add `/users` page
- load users through `GET /api/v1/auth/users`
- show role and basic account details

**Acceptance criteria**

- any signed-in user can open the directory
- directory shows role for each account
- regular users do not see destructive actions

**Dependencies**

- ST-21

### ST-23 Manage users through role-aware access

**Story**
As a governance user, I want account management actions to follow the role hierarchy so that access can be removed safely without breaking the single-super-admin model.

**Scope**

- add `DELETE /api/v1/auth/users/{userId}`
- add `PUT /api/v1/auth/users/{userId}/role`
- show delete action according to role hierarchy
- show admin-promotion actions only to the `SUPER_ADMIN`
- prevent self-deletion and self-role-change through the directory

**Acceptance criteria**

- `ADMIN` can delete a regular user
- `SUPER_ADMIN` can delete an admin or a regular user
- only `SUPER_ADMIN` can promote `USER` to `ADMIN`
- only `SUPER_ADMIN` can demote `ADMIN` back to `USER`
- regular users receive forbidden access for destructive governance actions
- no user can delete their own account from the directory

**Dependencies**

- ST-21
- ST-22

### ST-24 Show who created ecosystems, logs, and tasks

**Story**
As a user, I want to see who created a record so that team activity is understandable in a shared workspace.

**Scope**

- store creator user reference plus username/display-name snapshots
- expose creator fields in ecosystem, log, and task responses
- render creator labels in the frontend

**Acceptance criteria**

- new ecosystems show creator label
- new logs show creator label
- new manual and suggested tasks show creator label
- creator label remains visible after deleting the original account

**Dependencies**

- ST-21
- redirect user back to the home page

**Acceptance criteria**

- delete is available for an existing ecosystem
- after confirmation, ecosystem is removed
- related logs and tasks are also removed
- deleted ecosystem no longer appears in workspace cards

**Dependencies**

- ST-02

## Epic B. Activity logging

### ST-06 Add observation or care log

**Story**
As a user, I want to add a log entry to an ecosystem so that I can capture measurements and care events in context.

**Scope**

- add log form on ecosystem dashboard
- support `temperatureC`, `humidityPercent`, `eventType`, and `notes`
- create log through `POST /api/v1/ecosystems/{ecosystemId}/logs`

**Acceptance criteria**

- `eventType` is required
- `temperatureC`, when provided, must be between -100 and 100
- `humidityPercent`, when provided, must be between 0 and 100
- `notes` must not exceed 500 characters
- saved log appears at the top of the log list

**Dependencies**

- ST-02

### ST-07 Browse paged logs on dashboard

**Story**
As a user, I want to browse logs page by page so that I can review ecosystem history without overloading the screen.

**Scope**

- load logs from `GET /api/v1/ecosystems/{ecosystemId}/logs`
- show newest first
- support page navigation

**Acceptance criteria**

- logs are ordered from newest to oldest
- default page size is respected by the API
- user can move between available pages
- page index cannot go below 0

**Dependencies**

- ST-06

### ST-08 Filter logs by event type

**Story**
As a user, I want to filter logs by event type so that I can inspect only watering, feeding, or observation events.

**Scope**

- filter control on log section
- request filtered data from `GET /api/v1/ecosystems/{ecosystemId}/logs?eventType=...`
- preserve pagination behavior

**Acceptance criteria**

- filter supports `OBSERVATION`, `FEEDING`, and `WATERING`
- filtered results contain only selected event type
- pagination still works for filtered results
- clearing the filter returns all logs

**Dependencies**

- ST-07

### ST-09 Edit existing log entry

**Story**
As a user, I want to correct an existing log entry so that mistakes in notes or measurements do not distort the ecosystem history.

**Scope**

- inline or modal edit flow on the dashboard
- update through `PATCH /api/v1/ecosystems/{ecosystemId}/logs/{logId}`
- preserve original ownership and recorded timestamp

**Acceptance criteria**

- existing log values are prefilled in the edit form
- same validation rules apply as during log creation
- successful update refreshes the edited row in place
- log remains attached to the same ecosystem

**Dependencies**

- ST-07

## Epic C. Maintenance tasks

### ST-10 Create manual maintenance task

**Story**
As a user, I want to create a manual maintenance task so that I can remember an upcoming care action that is not system-generated.

**Scope**

- task creation form on ecosystem dashboard
- support `title`, `taskType`, and optional `dueDate`
- create via `POST /api/v1/ecosystems/{ecosystemId}/tasks`

**Acceptance criteria**

- task is saved with status `OPEN`
- `title` is required and limited to 120 characters
- `taskType` supports `WATERING`, `FEEDING`, `CLEANING`, and `INSPECTION`
- created task appears in the task list

**Dependencies**

- ST-02

### ST-11 Browse and filter tasks by status

**Story**
As a user, I want to filter tasks by status so that I can focus on open work, completed items, or overdue actions.

**Scope**

- task list on ecosystem dashboard
- filter by backend-supported values
- sorted task display

**Acceptance criteria**

- filter supports `ALL`, `OPEN`, `DONE`, `DISMISSED`, and `OVERDUE`
- tasks are shown in status priority order: `OPEN`, `DONE`, `DISMISSED`
- tasks with same status are sorted by nearest due date, then newest created date
- overdue filter returns open tasks with `dueDate < today`

**Dependencies**

- ST-10

### ST-12 Mark task as done and reopen it

**Story**
As a user, I want to change task status between open and done so that the dashboard reflects my real progress.

**Scope**

- status actions in task list
- update through `PATCH /api/v1/ecosystems/{ecosystemId}/tasks/{taskId}/status`

**Acceptance criteria**

- user can change an `OPEN` task to `DONE`
- user can change a `DONE` task back to `OPEN`
- dismissal reason is not accepted for `OPEN` or `DONE`
- updated status is visible immediately in the task list

**Dependencies**

- ST-11

### ST-13 Dismiss suggested task with reason

**Story**
As a user, I want to dismiss an irrelevant suggested task with a reason so that the system does not keep surfacing advice that is not useful right now.

**Scope**

- dismiss action for auto-created tasks only
- reason selection
- update through task status endpoint

**Acceptance criteria**

- dismiss is available only for suggested tasks
- reason is mandatory when status becomes `DISMISSED`
- allowed reasons are `TOO_SOON`, `NOT_RELEVANT`, and `ALREADY_HANDLED`
- dismissed task is still visible in filtered task history

**Dependencies**

- ST-11

### ST-14 Edit manual maintenance task

**Story**
As a user, I want to edit a manual task so that I can correct title, type, or due date when my plan changes.

**Scope**

- edit flow for manual tasks only
- update through `PATCH /api/v1/ecosystems/{ecosystemId}/tasks/{taskId}`

**Acceptance criteria**

- manual task edit form is prefilled with current values
- auto-created tasks cannot be edited through this flow
- validation rules match task creation rules
- updated task is shown with new values after save

**Dependencies**

- ST-10

### ST-15 Auto-create suggested task after watering or feeding log

**Story**
As a user, I want the system to suggest a follow-up task after watering or feeding so that I remember the next check without creating it manually.

**Scope**

- trigger suggestion logic after new `WATERING` and `FEEDING` logs
- create `INSPECTION` task with due date `today + 1 day`

**Acceptance criteria**

- after `WATERING`, system creates `Inspect moisture balance after watering`
- after `FEEDING`, system creates `Log feeding response check`
- suggested task has `autoCreated = true`
- suggested task has status `OPEN`
- no suggestion is created after `OBSERVATION`

**Dependencies**

- ST-06
- ST-10

### ST-16 Prevent duplicate or cooldown-blocked suggested tasks

**Story**
As a user, I want suggestion logic to avoid duplicates and respect recent dismissals so that the task list stays relevant and not noisy.

**Scope**

- duplicate prevention for identical open suggested tasks
- cooldown after dismissal based on reason

**Acceptance criteria**

- system does not create a duplicate if the same suggested task is already open
- `TOO_SOON` blocks recreation for 3 days
- `ALREADY_HANDLED` blocks recreation for 7 days
- `NOT_RELEVANT` blocks recreation for 30 days

**Dependencies**

- ST-13
- ST-15

## Epic D. Summary and dashboard behavior

### ST-17 Show ecosystem summary panel

**Story**
As a user, I want to see a summary panel for an ecosystem so that I can understand its current condition without manually reading all logs and tasks.

**Scope**

- load summary from `GET /api/v1/ecosystems/{id}/summary`
- show status, latest readings, averages, activity counts, streak, and task counts

**Acceptance criteria**

- summary shows latest event and latest recorded time when data exists
- summary shows open and overdue task counts
- summary shows recent logging counts for 7 and 30 days
- summary shows trend deltas when enough measurable data exists

**Dependencies**

- ST-06
- ST-10

### ST-18 Calculate ecosystem status in summary

**Story**
As a user, I want the system to assign a meaningful ecosystem status so that I can see quickly whether attention is required.

**Scope**

- backend status calculation for `STABLE`, `NEEDS_ATTENTION`, and `NO_RECENT_DATA`

**Acceptance criteria**

- status is `NEEDS_ATTENTION` when overdue tasks exist
- status is `NEEDS_ATTENTION` when humidity is below 35%
- status is `NEEDS_ATTENTION` when temperature is below 18 C or above 30 C
- status is `NEEDS_ATTENTION` when there are 3 or more open tasks
- status is `NO_RECENT_DATA` when no latest log exists or no activity occurred in the last 7 days
- status is `STABLE` otherwise

**Dependencies**

- ST-17

### ST-19 Refresh dashboard data after task or log updates

**Story**
As a user, I want the dashboard to refresh after I change logs or tasks so that status and counters always reflect the latest actions.

**Scope**

- refresh summary after creating or updating logs
- refresh summary after creating or updating tasks
- keep dashboard state aligned without full manual reload

**Acceptance criteria**

- summary refreshes after log creation
- summary refreshes after log update
- summary refreshes after task creation
- summary refreshes after task status change

**Dependencies**

- ST-17

## Epic E. Authentication and profile

### ST-20 Support open mode vs secured mode

**Story**
As a system administrator, I want authentication to be switchable by configuration so that the application can run both as an open demo and as a secured product.

**Scope**

- read `app.auth.enabled`
- allow unrestricted access when disabled
- require authentication for protected routes when enabled

**Acceptance criteria**

- when auth is disabled, main business routes are accessible without login
- when auth is enabled, only public auth routes remain open
- frontend can detect mode through `GET /api/v1/auth/status`

**Dependencies**

- none

### ST-21 Register a new user

**Story**
As a new user, I want to register an account so that I can use the application in secured mode.

**Scope**

- registration page and API integration
- validation and normalization of user fields
- password hashing

**Acceptance criteria**

- username must be unique
- email is lowercased before save
- password is accepted only in 6-72 character range
- successful registration returns created profile and allows next login step

**Dependencies**

- ST-20

### ST-22 Sign in and preserve authenticated session

**Story**
As a registered user, I want to sign in and be redirected to the home page so that I can access protected functionality.

**Scope**

- login page
- form login via `/login`
- server-side session behavior

**Acceptance criteria**

- valid credentials authenticate the user
- successful login redirects to the home page
- invalid credentials return user-visible error feedback
- authenticated state is reflected by `GET /api/v1/auth/status`

**Dependencies**

- ST-21

### ST-23 View and update own profile

**Story**
As an authenticated user, I want to view and edit my profile so that my account information stays current.

**Scope**

- load profile through `GET /api/v1/auth/profile`
- update editable fields through `PUT /api/v1/auth/profile`

**Acceptance criteria**

- profile page displays current values
- user can update display name, first name, last name, email, location, and bio
- username and password are not editable from this page
- updated profile is shown immediately after save

**Dependencies**

- ST-22

## Recommended implementation order

1. ST-01, ST-02
2. ST-06, ST-07
3. ST-10, ST-11, ST-12
4. ST-17, ST-18, ST-19
5. ST-15, ST-16, ST-13, ST-14
6. ST-03, ST-04, ST-05
7. ST-20, ST-21, ST-22, ST-23

## Notes for refinement

- These stories are intentionally small and can be estimated individually.
- Some frontend and backend parts can be split further if the team wants separate API and UI tickets.
- If needed, the next step can be to add story points, priorities, and test cases for each item.
