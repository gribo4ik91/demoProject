# EcoTracker Implementation-Ready Stories

## Purpose

This file is a stricter and more implementation-oriented version of the backlog.
Unlike the earlier compact backlog, these stories explicitly include:

- concrete functional scope
- endpoint contracts
- request parameters and body fields
- response expectations
- SQL and persistence impact
- implementation notes where the spec already implies technical behavior

## Story format

Each story contains:

- `Goal` - why the feature exists
- `API contract` - endpoints, params, body, and response
- `SQL / data impact` - affected tables and schema expectations
- `Acceptance criteria` - observable completion rules

## Epic A. Ecosystem management

### ST-01 Create ecosystem

**Goal**
As a user, I want to create a new ecosystem so that I can start tracking a concrete setup.

**API contract**

- Endpoint: `POST /api/v1/ecosystems`
- Body:
  - `name: string` required, not blank, max 100
  - `type: string` required, not blank, max 50
  - `description: string | null` optional, max 500
- Allowed UI values for `type`:
  - `FORMICARIUM`
  - `FLORARIUM`
  - `INDOOR_PLANTS`
  - `DIY_INCUBATOR`
- Success response:
  - `201 Created`
  - returns `EcosystemResponse`
- Error expectations:
  - `400 Bad Request` for invalid payload

**SQL / data impact**

- Writes to table `ecosystems`
- Required stored fields:
  - `id`
  - `name`
  - `type`
  - `description`
  - `created_at`
- Schema source:
  - Flyway migrations in `application/application/src/main/resources/db/migration`

**Acceptance criteria**

- valid payload creates a new ecosystem row
- blank `name` is rejected
- blank `type` is rejected
- too-long `name`, `type`, or `description` is rejected
- response contains the created ecosystem data

### ST-02 Get ecosystem list

**Goal**
As a user, I want to load all ecosystems so that I can navigate to a specific one.

**API contract**

- Endpoint: `GET /api/v1/ecosystems`
- Query params: none
- Body: none
- Success response:
  - `200 OK`
  - returns `EcosystemResponse[]`

**SQL / data impact**

- Reads from `ecosystems`
- No writes

**Acceptance criteria**

- response returns all saved ecosystems
- each item contains base ecosystem metadata
- order may follow repository default unless explicitly changed in implementation

### ST-03 Get single ecosystem details

**Goal**
As a user, I want to open one ecosystem so that I can work in its dedicated dashboard context.

**API contract**

- Endpoint: `GET /api/v1/ecosystems/{id}`
- Path params:
  - `id: UUID` required
- Success response:
  - `200 OK`
  - returns `EcosystemResponse`
- Error expectations:
  - `404 Not Found` if ecosystem does not exist

**SQL / data impact**

- Reads from `ecosystems` by primary key

**Acceptance criteria**

- existing UUID returns ecosystem data
- missing UUID returns `404`

### ST-04 Update ecosystem metadata

**Goal**
As a user, I want to edit ecosystem metadata so that the dashboard remains accurate over time.

**API contract**

- Endpoint: `PATCH /api/v1/ecosystems/{id}`
- Path params:
  - `id: UUID` required
- Body:
  - `name: string` required, not blank, max 100
  - `type: string` required, not blank, max 50
  - `description: string | null` optional, max 500
- Success response:
  - `200 OK`
  - returns updated `EcosystemResponse`
- Error expectations:
  - `400 Bad Request` for invalid payload
  - `404 Not Found` if ecosystem does not exist

**SQL / data impact**

- Updates row in `ecosystems`
- Does not create a new row

**Acceptance criteria**

- existing ecosystem can be updated
- invalid values are rejected
- returned object contains saved values

### ST-05 Delete ecosystem and dependent records

**Goal**
As a user, I want to delete an ecosystem so that obsolete data is fully removed.

**API contract**

- Endpoint: `DELETE /api/v1/ecosystems/{id}`
- Path params:
  - `id: UUID` required

## Epic D. User governance and attribution

### ST-21 Automatic admin assignment

**Goal**
As a product owner, I want the first registered account to receive admin rights automatically so that the environment always has one governance user without manual setup.

**API contract**

- Endpoint: `POST /api/v1/auth/register`
- Behavior:
  - first successful registration persists role `ADMIN`
  - all later registrations persist role `USER`
- Success response:
  - `201 Created`
  - `AuthUserResponse` includes `role`
- Related reads:
  - `GET /api/v1/auth/profile`
  - `GET /api/v1/auth/status`
  - both expose `role`

**SQL / data impact**

- table `app_user`
- add column `role`
- migrate existing data so one earliest user becomes `ADMIN` if no admin exists

**Acceptance criteria**

- first registered user is stored as `ADMIN`
- second and later users are stored as `USER`
- role is visible in profile and auth-status payloads

### ST-22 Shared user directory

**Goal**
As an authenticated user, I want a shared page listing all accounts so that I can understand who has access to the workspace.

**API contract**

- Endpoint: `GET /api/v1/auth/users`
- Auth:
  - requires authenticated session
- Success response:
  - `200 OK`
  - returns `UserListItemResponse[]`
- Response fields:
  - `id`
  - `displayName`
  - `username`
  - `role`
  - `firstName`
  - `lastName`
  - `email`
  - `location`
  - `createdAt`

**SQL / data impact**

- reads from `app_user`
- no writes

**Acceptance criteria**

- any signed-in user can call the endpoint
- response contains all registered users
- frontend page `/users` renders the directory

### ST-23 Admin-only user deletion

**Goal**
As an admin, I want to remove obsolete user accounts so that access stays clean without changing the rest of the business logic.

**API contract**

- Endpoint: `DELETE /api/v1/auth/users/{userId}`
- Auth:
  - requires authenticated admin session
- Path params:
  - `userId: UUID` required
- Success response:
  - `204 No Content`
- Error expectations:
  - `403 Forbidden` when caller is not admin
  - `404 Not Found` when target user does not exist
  - `400 Bad Request` when admin tries to delete their own account

**SQL / data impact**

- deletes rows from `app_user`
- creator foreign keys use `ON DELETE SET NULL`
- creator snapshots remain stored on business records

**Acceptance criteria**

- admin can delete another user
- regular users cannot delete accounts
- admin self-deletion is rejected

### ST-24 Creator attribution on business records

**Goal**
As a user, I want to see who created an ecosystem, log, or task so that the shared workspace remains understandable when several people use it.

**API contract**

- `POST /api/v1/ecosystems`
  - persists creator metadata from current user when available
- `POST /api/v1/ecosystems/{ecosystemId}/logs`
  - persists creator metadata from current user when available
- `POST /api/v1/ecosystems/{ecosystemId}/tasks`
  - persists creator metadata from current user when available
- Response additions:
  - `EcosystemResponse` includes `createdByUsername`, `createdByDisplayName`
  - `EcosystemLogResponse` includes `createdByUsername`, `createdByDisplayName`
  - `MaintenanceTaskResponse` includes `createdByUsername`, `createdByDisplayName`
- Suggested tasks:
  - use a system actor snapshot such as `System`

**SQL / data impact**

- table `ecosystems`
  - add `created_by_user_id`
  - add `created_by_username`
  - add `created_by_display_name`
- table `logs`
  - add `created_by_user_id`
  - add `created_by_username`
  - add `created_by_display_name`
- table `maintenance_tasks`
  - add `created_by_user_id`
  - add `created_by_username`
  - add `created_by_display_name`

**Acceptance criteria**

- ecosystem payload returns creator data
- log payload returns creator data
- task payload returns creator data
- creator labels remain visible after deleting the original account
- Success response:
  - `204 No Content`
- Error expectations:
  - `404 Not Found` if ecosystem does not exist

**SQL / data impact**

- Deletes from `ecosystems`
- Must also delete dependent rows from:
  - `logs`
  - `maintenance_tasks`
- This can be implemented by cascade behavior or service-level cleanup, but result must match spec

**Acceptance criteria**

- deleting existing ecosystem removes root record
- related logs are removed
- related maintenance tasks are removed
- deleted ecosystem cannot be loaded afterward

### ST-06 Get ecosystem summary

**Goal**
As a user, I want to see a synthesized health panel for one ecosystem so that I can assess its state quickly.

**API contract**

- Endpoint: `GET /api/v1/ecosystems/{id}/summary`
- Path params:
  - `id: UUID` required
- Success response:
  - `200 OK`
  - returns fields:
    - `ecosystemId: UUID`
    - `status: string`
    - `lastRecordedAt: datetime | null`
    - `latestEventType: string | null`
    - `currentTemperatureC: number | null`
    - `currentHumidityPercent: number | null`
    - `averageTemperatureC: number | null`
    - `averageHumidityPercent: number | null`
    - `logsLast7Days: number`
    - `logsLast30Days: number`
    - `activeDaysLast30Days: number`
    - `loggingStreakDays: number`
    - `temperatureTrendDeltaC: number | null`
    - `humidityTrendDeltaPercent: number | null`
    - `openTasks: number`
    - `overdueTasks: number`

**SQL / data impact**

- Reads from `ecosystems`
- Aggregates from `logs`
- Aggregates from `maintenance_tasks`
- Uses recent windows:
  - latest log
  - latest 5 logs for averages
  - last 7 days
  - last 30 days

**Acceptance criteria**

- summary returns calculated metrics for an existing ecosystem
- `status` is computed from logs and tasks, not stored directly
- no direct writes happen during summary request

### ST-07 Get workspace cards with search, status, sort, and pagination

**Goal**
As a user, I want a paged enriched workspace view so that I can manage multiple ecosystems efficiently from the home page.

**API contract**

- Endpoint: `GET /api/v1/ecosystems/cards`
- Query params:
  - `search: string` optional
  - `status: string` optional, default `ALL`
  - `sort: string` optional, default `PRIORITY`
  - `page: integer` optional, default `0`, minimum `0`
  - `size: integer` optional, default `9`, effective range `1..24`
- Allowed `status` values:
  - `ALL`
  - `NEEDS_ATTENTION`
  - `STABLE`
  - `NO_RECENT_DATA`
  - `OVERDUE`
- Allowed `sort` values:
  - `PRIORITY`
  - `LAST_ACTIVITY`
  - `NAME`
  - `NEWEST`
- Success response:
  - `200 OK`
  - returns `PagedResponse<EcosystemWorkspaceCardResponse>`
- Paged wrapper fields:
  - `page`
  - `size`
  - `totalElements`
  - `totalPages`
  - `hasNext`
  - `hasPrevious`
  - `items`

**SQL / data impact**

- Reads from `ecosystems`
- Aggregates latest log snapshots from `logs`
- Aggregates counts from `maintenance_tasks`
- Filtering and sorting are coordinated in service logic using grouped reads

**Acceptance criteria**

- filtering is applied before pagination
- sorting is applied before pagination
- `OVERDUE` filter matches ecosystems with overdue open tasks
- response shape matches paged contract used by frontend

### ST-08 Get workspace overview counters

**Goal**
As a user, I want counters aligned with the current workspace filter so that the dashboard summary matches the visible result set.

**API contract**

- Endpoint: `GET /api/v1/ecosystems/overview`
- Query params:
  - `search: string` optional
  - `status: string` optional, default `ALL`
- Success response:
  - `200 OK`
  - returns:
    - `totalEcosystems: integer`
    - `needsAttention: integer`
    - `stable: integer`
    - `noRecentData: integer`
    - `openTasks: integer`
    - `overdueTasks: integer`

**SQL / data impact**

- Reads same filtered workspace snapshot as cards logic
- Aggregates over filtered set, not only visible page

**Acceptance criteria**

- overview uses same search semantics as `/ecosystems/cards`
- overview uses same status filter semantics as `/ecosystems/cards`
- counters reflect entire filtered dataset

## Epic B. Activity logging

### ST-09 Create ecosystem log entry

**Goal**
As a user, I want to save an observation or care event so that ecosystem history remains factual and complete.

**API contract**

- Endpoint: `POST /api/v1/ecosystems/{ecosystemId}/logs`
- Path params:
  - `ecosystemId: UUID` required
- Body:
  - `temperatureC: number | null` optional, range `-100..100`
  - `humidityPercent: integer | null` optional, range `0..100`
  - `eventType: string` required, max 50
  - `notes: string | null` optional, max 500
- Allowed `eventType` values:
  - `OBSERVATION`
  - `FEEDING`
  - `WATERING`
- Success response:
  - `201 Created`
  - returns `EcosystemLogResponse`

**Implementation notes**

- event type should be normalized to uppercase
- ecosystem existence must be checked before save
- after save, maintenance suggestion logic may be triggered

**SQL / data impact**

- Reads `ecosystems` to validate parent existence
- Inserts into `logs`
- May also insert into `maintenance_tasks` if suggestion rules apply

**Acceptance criteria**

- valid log creates a row in `logs`
- invalid measurements are rejected
- missing `eventType` is rejected
- `WATERING` and `FEEDING` trigger suggestion evaluation

### ST-10 Get paged ecosystem logs with optional event filter

**Goal**
As a user, I want to browse history with filtering and pagination so that the log remains usable over time.

**API contract**

- Endpoint: `GET /api/v1/ecosystems/{ecosystemId}/logs`
- Path params:
  - `ecosystemId: UUID` required
- Query params:
  - `eventType: string` optional
  - `page: integer` optional, default `0`, minimum `0`
  - `size: integer` optional, default `5`, effective range `1..50`
- Success response:
  - `200 OK`
  - returns `PagedResponse<EcosystemLogResponse>`

**SQL / data impact**

- Reads from `logs` by `ecosystem_id`
- Supports filtering by `event_type`
- Sort order: newest to oldest by `recorded_at`

**Acceptance criteria**

- logs are returned newest first
- filtering by event type returns only matching rows
- page size and page index constraints are enforced

### ST-11 Update ecosystem log entry

**Goal**
As a user, I want to correct an existing log so that wrong notes or values do not distort later analysis.

**API contract**

- Endpoint: `PATCH /api/v1/ecosystems/{ecosystemId}/logs/{logId}`
- Path params:
  - `ecosystemId: UUID` required
  - `logId: UUID` required
- Body:
  - `temperatureC: number | null` optional, range `-100..100`
  - `humidityPercent: integer | null` optional, range `0..100`
  - `eventType: string` required
  - `notes: string | null` optional, max 500
- Success response:
  - `200 OK`
  - returns updated `EcosystemLogResponse`
- Error expectations:
  - `404 Not Found` if ecosystem or log is missing

**Implementation notes**

- original log ownership and timestamp must be preserved

**SQL / data impact**

- Reads `ecosystems`
- Reads and updates row in `logs`
- Does not create a new log row

**Acceptance criteria**

- existing log can be updated
- recorded timestamp remains original
- log remains attached to same ecosystem

## Epic C. Maintenance tasks

### ST-12 Create manual maintenance task

**Goal**
As a user, I want to add my own maintenance reminder so that I can track work that is not auto-suggested by the system.

**API contract**

- Endpoint: `POST /api/v1/ecosystems/{ecosystemId}/tasks`
- Path params:
  - `ecosystemId: UUID` required
- Body:
  - `title: string` required, not blank, max 120
  - `taskType: string` required, max 50
  - `dueDate: date | null` optional, format `YYYY-MM-DD`
- Allowed `taskType` values:
  - `WATERING`
  - `FEEDING`
  - `CLEANING`
  - `INSPECTION`
- Success response:
  - `201 Created`
  - returns `MaintenanceTaskResponse`

**Implementation notes**

- manual tasks are saved with:
  - `status = OPEN`
  - `autoCreated = false`

**SQL / data impact**

- Reads `ecosystems`
- Inserts into `maintenance_tasks`

**Acceptance criteria**

- manual task is saved as open
- optional due date is accepted in correct date format
- invalid title or type is rejected

### ST-13 Get tasks by status filter

**Goal**
As a user, I want to load tasks by status so that I can focus on the right worklist.

**API contract**

- Endpoint: `GET /api/v1/ecosystems/{ecosystemId}/tasks`
- Path params:
  - `ecosystemId: UUID` required
- Query params:
  - `filter: string` optional, default `ALL`
- Allowed values:
  - `ALL`
  - `OPEN`
  - `DONE`
  - `DISMISSED`
  - `OVERDUE`
- Success response:
  - `200 OK`
  - returns `MaintenanceTaskResponse[]`

**Implementation notes**

- status priority order in response:
  1. `OPEN`
  2. `DONE`
  3. `DISMISSED`
- within same status:
  - nearest `dueDate`
  - then newest `createdAt`

**SQL / data impact**

- Reads from `maintenance_tasks` by `ecosystem_id`
- `OVERDUE` means `status = OPEN` and `due_date < today`

**Acceptance criteria**

- requested filter is applied correctly
- overdue tasks appear only when open and past due
- sorting follows business rules

### ST-14 Update manual maintenance task

**Goal**
As a user, I want to edit a manual task so that I can adjust the plan when needed.

**API contract**

- Endpoint: `PATCH /api/v1/ecosystems/{ecosystemId}/tasks/{taskId}`
- Path params:
  - `ecosystemId: UUID` required
  - `taskId: UUID` required
- Body:
  - `title: string` required, not blank, max 120
  - `taskType: string` required, max 50
  - `dueDate: date | null` optional
- Success response:
  - `200 OK`
  - returns updated `MaintenanceTaskResponse`
- Error expectations:
  - `404 Not Found` if ecosystem or task is missing
  - business rejection if task is auto-created

**Implementation notes**

- only manual tasks are editable through this endpoint

**SQL / data impact**

- Reads `maintenance_tasks`
- Updates existing manual task row
- Must not allow content edits for auto-created suggestions

**Acceptance criteria**

- manual task can be updated
- auto-created task cannot be updated through this endpoint
- response contains updated task values

### ST-15 Change task status

**Goal**
As a user, I want to change task status so that the worklist reflects what is active, completed, or dismissed.

**API contract**

- Endpoint: `PATCH /api/v1/ecosystems/{ecosystemId}/tasks/{taskId}/status`
- Path params:
  - `ecosystemId: UUID` required
  - `taskId: UUID` required
- Body:
  - `status: string` required
  - `dismissalReason: string | null` optional
- Allowed `status` values:
  - `OPEN`
  - `DONE`
  - `DISMISSED`
- Allowed `dismissalReason` values:
  - `TOO_SOON`
  - `NOT_RELEVANT`
  - `ALREADY_HANDLED`

**Business rules**

- `dismissalReason` is required for `DISMISSED`
- `dismissalReason` must not be sent for `OPEN` or `DONE`
- `DISMISSED` is allowed only for auto-created tasks

**SQL / data impact**

- Updates `status` and optionally `dismissal_reason` in `maintenance_tasks`

**Acceptance criteria**

- open task can be marked done
- done task can be reopened
- suggested task can be dismissed with valid reason
- invalid status/reason combinations are rejected

### ST-16 Auto-create suggested task after watering or feeding

**Goal**
As a user, I want the system to create follow-up tasks after care actions so that routine checks are not forgotten.

**API contract**

- Trigger source:
  - `POST /api/v1/ecosystems/{ecosystemId}/logs`
- Triggering event types:
  - `WATERING`
  - `FEEDING`
- Auto-created task shape:
  - title after `WATERING`: `Inspect moisture balance after watering`
  - title after `FEEDING`: `Log feeding response check`
  - `taskType = INSPECTION`
  - `autoCreated = true`
  - `status = OPEN`
  - `dueDate = today + 1 day`

**SQL / data impact**

- Reads recent `maintenance_tasks` for duplicate and cooldown checks
- Inserts into `maintenance_tasks` when rule passes

**Acceptance criteria**

- suggestion appears after new watering log
- suggestion appears after new feeding log
- suggestion does not appear after observation log

### ST-17 Prevent duplicate or cooldown-blocked suggestions

**Goal**
As a user, I want suggestions to stay relevant and non-repetitive so that the task list does not become noisy.

**API contract**

- No standalone endpoint
- Behavior is part of suggestion evaluation triggered by log creation

**Business rules**

- do not create a suggestion if identical open suggested task already exists
- cooldown after dismissal:
  - `TOO_SOON` -> 3 days
  - `ALREADY_HANDLED` -> 7 days
  - `NOT_RELEVANT` -> 30 days

**SQL / data impact**

- Reads open suggested tasks from `maintenance_tasks`
- Reads latest dismissed matching suggestion from `maintenance_tasks`
- Compares dismissal timestamp/date against cooldown window

**Acceptance criteria**

- duplicate open suggestion is not recreated
- dismissed suggestion inside cooldown is not recreated
- same suggestion may be recreated again after cooldown expires

## Epic D. Authentication and profile

### ST-18 Register user

**Goal**
As a new user, I want to register so that I can access the secured application mode.

**API contract**

- Endpoint: `POST /api/v1/auth/register`
- Body:
  - `displayName: string` required, `3..60`
  - `username: string` required, `3..40`, unique
  - `firstName: string` required, `2..60`
  - `lastName: string` required, `2..60`
  - `email: string` required, valid, max 120
  - `location: string | null` optional, max 80
  - `bio: string | null` optional, max 500
  - `password: string` required, `6..72`
- Success response:
  - `201 Created`
  - returns created user profile

**Implementation notes**

- normalize text fields
- lowercase email
- hash password with BCrypt

**SQL / data impact**

- Inserts into `app_user`
- Requires unique constraint or equivalent uniqueness check for `username`
- Stores password hash, not raw password

**Acceptance criteria**

- unique username is enforced
- password is stored hashed
- normalized profile is returned after creation

### ST-19 Get auth status

**Goal**
As a frontend, I want to know whether auth is enabled and whether the current user is logged in so that pages can render the correct header and access flow.

**API contract**

- Endpoint: `GET /api/v1/auth/status`
- Response:
  - `enabled: boolean`
  - `authenticated: boolean`
  - `username: string | null`
  - `displayName: string | null`

**SQL / data impact**

- May read authenticated user context
- No mandatory DB write

**Acceptance criteria**

- response shows whether auth mode is enabled
- response shows whether current request has authenticated session
- user identity fields are present only when authenticated

### ST-20 Get current user profile

**Goal**
As an authenticated user, I want to load my profile so that I can review current account data.

**API contract**

- Endpoint: `GET /api/v1/auth/profile`
- Body: none
- Success response:
  - `200 OK`
  - returns `AuthUserResponse`

**SQL / data impact**

- Reads from `app_user` for currently authenticated principal

**Acceptance criteria**

- authenticated user receives own profile
- unauthenticated access is blocked when auth mode is enabled

### ST-21 Update current user profile

**Goal**
As an authenticated user, I want to update editable profile fields so that my account remains current.

**API contract**

- Endpoint: `PUT /api/v1/auth/profile`
- Body:
  - `displayName: string` required, `3..60`
  - `firstName: string` required, `2..60`
  - `lastName: string` required, `2..60`
  - `email: string` required, valid, max 120
  - `location: string | null` optional, max 80
  - `bio: string | null` optional, max 500
- Success response:
  - `200 OK`
  - returns updated `AuthUserResponse`

**Implementation notes**

- username and password are not editable on this endpoint

**SQL / data impact**

- Updates `app_user`
- No password change in this flow

**Acceptance criteria**

- editable fields are updated
- username remains unchanged
- password remains unchanged

### ST-22 Support open mode and secured mode

**Goal**
As a system owner, I want authentication to be configurable so that the product can run in both demo and secured modes.

**API contract**

- Configuration flag:
  - `APP_AUTH_ENABLED`
- Public routes when auth enabled:
  - `/login`
  - `/register`
  - `/api/v1/auth/register`
  - `/api/v1/auth/status`
- Protected routes:
  - all business APIs and profile endpoints

**SQL / data impact**

- No schema change required
- Uses `app_user` only when auth is enabled and user flows are active

**Acceptance criteria**

- all requests are allowed without login when auth disabled
- business routes require authentication when auth enabled
- login/logout flows are disabled in open mode

## Epic E. SQL and persistence

### ST-23 Run PostgreSQL locally from the sql module

**Goal**
As a developer, I want to start the database independently from the app so that I can run backend features and migrations locally.

**Operational contract**

- Module: `sql`
- Main file: `sql/docker-compose.yml`
- Expected runtime settings:
  - image `postgres:15-alpine`
  - container `ecotracker-postgres`
  - database `ecotracker_db`
  - user `eco_user`
  - password `eco_password`
  - port `5432:5432`
  - persistent volume `postgres_data`
  - network `ecotracker-network`

**SQL / data impact**

- Provides PostgreSQL runtime only
- Does not contain business schema scripts
- Schema itself lives in Flyway migrations under the application module

**Acceptance criteria**

- database can be started without starting the application
- application default JDBC URL can connect to local DB:
  - `jdbc:postgresql://localhost:5432/ecotracker_db`
- data persists between restarts through Docker volume

### ST-24 Control SQL runtime through Jenkins pipeline

**Goal**
As an environment engineer, I want Jenkins to control the SQL container lifecycle so that the local or shared DB runtime can be managed consistently.

**Operational contract**

- File: `sql/Jenkinsfile`
- Supported actions:
  - `up`
  - `down`
  - `stop`
  - `logs`
  - `ps`

**SQL / data impact**

- Controls database container lifecycle
- No business table change by itself

**Acceptance criteria**

- pipeline can start DB container
- pipeline can stop or remove DB container
- pipeline can show logs and process status

### ST-25 Apply Flyway migrations for schema evolution

**Goal**
As a developer, I want the schema to evolve through versioned migrations so that database structure stays reproducible and auditable.

**Implementation contract**

- Migration location:
  - `application/application/src/main/resources/db/migration`
- Known schema evolution:
  - `V1` ecosystems and logs
  - `V2` maintenance tasks
  - `V3` auto-created flag
  - `V4` dismissal reason
  - `V5` base user table
  - `V6` expanded user profile
  - `V7` optional `location` and `bio`

**SQL / data impact**

- Defines and evolves tables:
  - `ecosystems`
  - `logs`
  - `maintenance_tasks`
  - `app_user`

**Acceptance criteria**

- schema can be created from migrations only
- fresh environment gets latest structure without manual SQL steps
- migrations stay aligned with API fields used by implemented features

## Notes

- The previous file `11-small-stories-backlog.md` is still useful as a compact backlog view.
- This file is the better source when the team needs implementation-ready stories with endpoint and SQL context.
