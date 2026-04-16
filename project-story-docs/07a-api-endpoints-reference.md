# API Endpoints Reference

## Why this file matters

Yes, this was a significant omission.
Project documentation without an explicit endpoint reference is incomplete for:

- backend development
- frontend integration
- manual testing
- onboarding new team members

This file closes exactly that gap.

## General information

- Base path: `/api/v1`
- Request/response format: JSON, except form login on `/login`
- Authentication:
  when `APP_AUTH_ENABLED=true`, almost all endpoints require authentication except the public auth routes

## 1. Authentication API

### `POST /api/v1/auth/register`

Purpose:
register a new user.

#### Body

| Field | Type | Required | Constraints / allowed values |
|---|---|---|---|
| `displayName` | string | yes | 3-60 characters |
| `username` | string | yes | 3-40 characters, unique |
| `firstName` | string | yes | 2-60 characters |
| `lastName` | string | yes | 2-60 characters |
| `email` | string | yes | valid email, up to 120 characters |
| `location` | string \| null | no | up to 80 characters |
| `bio` | string \| null | no | up to 500 characters |
| `password` | string | yes | 6-72 characters |

#### Response

- `201 Created`
- returns the created user profile

### `GET /api/v1/auth/profile`

Purpose:
get the current user's profile.

#### Parameters

- path parameters: none
- query parameters: none
- body: none

#### Response

- `200 OK`
- returns `AuthUserResponse`

### `PUT /api/v1/auth/profile`

Purpose:
update editable fields of the current user's profile.

#### Body

| Field | Type | Required | Constraints |
|---|---|---|---|
| `displayName` | string | yes | 3-60 characters |
| `firstName` | string | yes | 2-60 characters |
| `lastName` | string | yes | 2-60 characters |
| `email` | string | yes | valid email, up to 120 characters |
| `location` | string \| null | no | up to 80 characters |
| `bio` | string \| null | no | up to 500 characters |

#### Response

- `200 OK`
- returns the updated `AuthUserResponse`

### `GET /api/v1/auth/status`

Purpose:
check whether authentication is enabled and whether a session exists.

#### Parameters

- path parameters: none
- query parameters: none
- body: none

#### Response

Response fields:

| Field | Type | Description |
|---|---|---|
| `enabled` | boolean | whether auth mode is enabled |
| `authenticated` | boolean | whether a user is currently authenticated |
| `username` | string \| null | current username |
| `displayName` | string \| null | current display name |
| `role` | string \| null | current role, typically `ADMIN` or `USER` |

### `GET /api/v1/auth/users`

Purpose:
return all registered users for the signed-in account.

#### Response

- `200 OK`
- returns an array of user directory entries

Response fields:

| Field | Type | Description |
|---|---|---|
| `id` | UUID | user identifier |
| `displayName` | string | display name |
| `username` | string | login |
| `role` | string | `ADMIN` or `USER` |
| `firstName` | string | first name |
| `lastName` | string | last name |
| `email` | string | contact email |
| `location` | string \| null | optional location |
| `createdAt` | datetime | account creation timestamp |

### `DELETE /api/v1/auth/users/{userId}`

Purpose:
delete a user account through admin access.

#### Important behavior

- only admins can call it
- admins cannot delete their own account
- successful deletion returns `204 No Content`

## 2. Ecosystem API

### `POST /api/v1/ecosystems`

Purpose:
create a new ecosystem.

#### Body

| Field | Type | Required | Constraints / allowed values |
|---|---|---|---|
| `name` | string | yes | not blank, up to 100 characters |
| `type` | string | yes | not blank, up to 50 characters; UI values: `FORMICARIUM`, `FLORARIUM`, `INDOOR_PLANTS`, `DIY_INCUBATOR` |
| `description` | string \| null | no | up to 500 characters |

#### Response

- `201 Created`
- returns `EcosystemResponse`

Important response fields also include:

- `createdByUsername`
- `createdByDisplayName`

### `GET /api/v1/ecosystems`

Purpose:
return all ecosystems.

#### Parameters

- path parameters: none
- query parameters: none
- body: none

#### Response

- `200 OK`
- returns an array of `EcosystemResponse`

### `GET /api/v1/ecosystems/cards`

Purpose:
return enriched workspace cards for the home page dashboard using server-side filtering, sorting, and pagination.

#### Parameters

- path parameters: none
- body: none

#### Query parameters

| Parameter | Type | Required | Default | Description |
|---|---|---|---|---|
| `search` | string | no | - | case-insensitive match across ecosystem name, type, and description |
| `status` | string | no | `ALL` | `NEEDS_ATTENTION`, `STABLE`, `NO_RECENT_DATA`, or `OVERDUE` |
| `sort` | string | no | `PRIORITY` | `PRIORITY`, `LAST_ACTIVITY`, `NAME`, or `NEWEST` |
| `page` | integer | no | `0` | page index, minimum 0 |
| `size` | integer | no | `9` | page size, effectively constrained to 1-24 |

#### Response

Returns `PagedResponse<EcosystemWorkspaceCardResponse>`.

Paged wrapper fields:

| Field | Type |
|---|---|
| `page` | integer |
| `size` | integer |
| `totalElements` | integer |
| `totalPages` | integer |
| `hasNext` | boolean |
| `hasPrevious` | boolean |
| `items` | array |

Fields of one `EcosystemWorkspaceCardResponse`:

| Field | Type |
|---|---|
| `id` | UUID |
| `name` | string |
| `type` | string |
| `description` | string \| null |
| `status` | string |
| `lastRecordedAt` | datetime \| null |
| `logsLast7Days` | integer |
| `openTasks` | integer |
| `overdueTasks` | integer |
| `createdAt` | datetime |

#### Important behavior

- filtering is applied before pagination
- sorting is applied before pagination
- `OVERDUE` is a derived filter that matches ecosystems with overdue open tasks
- the endpoint is intended to support incremental loading on `index.html`

### `GET /api/v1/ecosystems/overview`

Purpose:
return aggregated workspace counters for the home page dashboard.

#### Parameters

- path parameters: none
- body: none

#### Query parameters

| Parameter | Type | Required | Default | Description |
|---|---|---|---|---|
| `search` | string | no | - | same workspace search semantics as `/ecosystems/cards` |
| `status` | string | no | `ALL` | same status filter semantics as `/ecosystems/cards` |

#### Response

Returns `EcosystemWorkspaceOverviewResponse`.

| Field | Type | Description |
|---|---|---|
| `totalEcosystems` | integer | number of ecosystems in the filtered result set |
| `needsAttention` | integer | number of filtered ecosystems with `NEEDS_ATTENTION` |
| `stable` | integer | number of filtered ecosystems with `STABLE` |
| `noRecentData` | integer | number of filtered ecosystems with `NO_RECENT_DATA` |
| `openTasks` | integer | sum of open tasks across filtered ecosystems |
| `overdueTasks` | integer | sum of overdue open tasks across filtered ecosystems |

#### Important behavior

- overview uses the same search and status filtering semantics as `/ecosystems/cards`
- overview is calculated from the full filtered set, not from a paged slice

### `GET /api/v1/ecosystems/{id}`

Purpose:
get a single ecosystem by identifier.

#### Path parameters

| Parameter | Type | Description |
|---|---|---|
| `id` | UUID | ecosystem identifier |

#### Response

- `200 OK`
- `404 Not Found` if the ecosystem does not exist

### `GET /api/v1/ecosystems/{id}/summary`

Purpose:
get the ecosystem summary.

#### Path parameters

| Parameter | Type | Description |
|---|---|---|
| `id` | UUID | ecosystem identifier |

#### Response

Response fields:

| Field | Type |
|---|---|
| `ecosystemId` | UUID |
| `status` | string |
| `lastRecordedAt` | datetime \| null |
| `latestEventType` | string \| null |
| `currentTemperatureC` | number \| null |
| `currentHumidityPercent` | number \| null |
| `averageTemperatureC` | number \| null |
| `averageHumidityPercent` | number \| null |
| `logsLast7Days` | number |
| `logsLast30Days` | number |
| `activeDaysLast30Days` | number |
| `loggingStreakDays` | number |
| `temperatureTrendDeltaC` | number \| null |
| `humidityTrendDeltaPercent` | number \| null |
| `openTasks` | number |
| `overdueTasks` | number |

### `PATCH /api/v1/ecosystems/{id}`

Purpose:
update the main ecosystem metadata.

#### Path parameters

| Parameter | Type | Description |
|---|---|---|
| `id` | UUID | ecosystem identifier |

#### Body

| Field | Type | Required | Constraints / allowed values |
|---|---|---|---|
| `name` | string | yes | not blank, up to 100 characters |
| `type` | string | yes | not blank, up to 50 characters; UI values: `FORMICARIUM`, `FLORARIUM`, `INDOOR_PLANTS`, `DIY_INCUBATOR` |
| `description` | string \| null | no | up to 500 characters |

#### Response

- `200 OK`
- returns the updated `EcosystemResponse`

### `DELETE /api/v1/ecosystems/{id}`

Purpose:
delete the ecosystem and its related data.

#### Path parameters

| Parameter | Type | Description |
|---|---|---|
| `id` | UUID | ecosystem identifier |

#### Response

- `204 No Content`
- `404 Not Found` if the ecosystem does not exist

## 3. Ecosystem Logs API

### `POST /api/v1/ecosystems/{ecosystemId}/logs`

Purpose:
add a log entry to an ecosystem.

#### Path parameters

| Parameter | Type | Description |
|---|---|---|
| `ecosystemId` | UUID | ecosystem identifier |

#### Body

| Field | Type | Required | Constraints / allowed values |
|---|---|---|---|
| `temperatureC` | number \| null | no | from -100 to 100 |
| `humidityPercent` | integer \| null | no | from 0 to 100 |
| `eventType` | string | yes | `OBSERVATION`, `FEEDING`, `WATERING`; up to 50 characters |
| `notes` | string \| null | no | up to 500 characters |

#### Response

- `201 Created`
- returns `EcosystemLogResponse`

### `GET /api/v1/ecosystems/{ecosystemId}/logs`

Purpose:
return ecosystem logs with pagination and filtering.

#### Path parameters

| Parameter | Type | Description |
|---|---|---|
| `ecosystemId` | UUID | ecosystem identifier |

#### Query parameters

| Parameter | Type | Required | Default | Description |
|---|---|---|---|---|
| `eventType` | string | no | - | filter by event type |
| `page` | integer | no | `0` | page index, minimum 0 |
| `size` | integer | no | `5` | page size, effectively constrained to 1-50 |

#### Response

Returns `PagedResponse<EcosystemLogResponse>` with these fields:

| Field | Type |
|---|---|
| `page` | integer |
| `size` | integer |
| `totalElements` | integer |
| `totalPages` | integer |
| `hasNext` | boolean |
| `hasPrevious` | boolean |
| `items` | array |

Fields of one `EcosystemLogResponse`:

| Field | Type |
|---|---|
| `id` | UUID |
| `ecosystemId` | UUID |
| `temperatureC` | number \| null |
| `humidityPercent` | integer \| null |
| `eventType` | string |
| `notes` | string \| null |
| `createdByUsername` | string \| null |
| `createdByDisplayName` | string \| null |
| `recordedAt` | datetime |

### `PATCH /api/v1/ecosystems/{ecosystemId}/logs/{logId}`

Purpose:
update an existing log entry for an ecosystem.

#### Path parameters

| Parameter | Type | Description |
|---|---|---|
| `ecosystemId` | UUID | ecosystem identifier |
| `logId` | UUID | log identifier |

#### Body

| Field | Type | Required | Constraints / allowed values |
|---|---|---|---|
| `temperatureC` | number \| null | no | from -100 to 100 |
| `humidityPercent` | integer \| null | no | from 0 to 100 |
| `eventType` | string | yes | `OBSERVATION`, `FEEDING`, `WATERING`; up to 50 characters |
| `notes` | string \| null | no | up to 500 characters |

#### Response

- `200 OK`
- returns the updated `EcosystemLogResponse`

## 4. Maintenance Tasks API

### `POST /api/v1/ecosystems/{ecosystemId}/tasks`

Purpose:
create a manual maintenance task.

#### Path parameters

| Parameter | Type | Description |
|---|---|---|
| `ecosystemId` | UUID | ecosystem identifier |

#### Body

| Field | Type | Required | Constraints / allowed values |
|---|---|---|---|
| `title` | string | yes | not blank, up to 120 characters |
| `taskType` | string | yes | `WATERING`, `FEEDING`, `CLEANING`, `INSPECTION`; up to 50 characters |
| `dueDate` | date \| null | no | format `YYYY-MM-DD` |

#### Response

- `201 Created`
- returns `MaintenanceTaskResponse`

### `GET /api/v1/ecosystems/{ecosystemId}/tasks`

Purpose:
return ecosystem tasks with status filtering.

#### Path parameters

| Parameter | Type | Description |
|---|---|---|
| `ecosystemId` | UUID | ecosystem identifier |

#### Query parameters

| Parameter | Type | Required | Default | Allowed values |
|---|---|---|---|---|
| `filter` | string | no | `ALL` | `ALL`, `OPEN`, `DONE`, `DISMISSED`, `OVERDUE` |

#### Response

Returns an array of `MaintenanceTaskResponse`.

Fields of `MaintenanceTaskResponse`:

| Field | Type |
|---|---|
| `id` | UUID |
| `ecosystemId` | UUID |
| `title` | string |
| `taskType` | string |
| `dueDate` | date \| null |
| `status` | string |
| `autoCreated` | boolean |
| `dismissalReason` | string \| null |
| `createdByUsername` | string \| null |
| `createdByDisplayName` | string \| null |
| `createdAt` | datetime |

### `PATCH /api/v1/ecosystems/{ecosystemId}/tasks/{taskId}`

Purpose:
update a manually created maintenance task.

#### Path parameters

| Parameter | Type | Description |
|---|---|---|
| `ecosystemId` | UUID | ecosystem identifier |
| `taskId` | UUID | task identifier |

#### Body

| Field | Type | Required | Constraints / allowed values |
|---|---|---|---|
| `title` | string | yes | not blank, up to 120 characters |
| `taskType` | string | yes | `WATERING`, `FEEDING`, `CLEANING`, `INSPECTION`; up to 50 characters |
| `dueDate` | date \| null | no | format `YYYY-MM-DD` |

#### Important rules

- only manual tasks can be edited this way
- auto-created suggested tasks cannot be edited through this endpoint

#### Response

- `200 OK`
- returns the updated `MaintenanceTaskResponse`

### `PATCH /api/v1/ecosystems/{ecosystemId}/tasks/{taskId}/status`

Purpose:
change the status of an existing task.

#### Path parameters

| Parameter | Type | Description |
|---|---|---|
| `ecosystemId` | UUID | ecosystem identifier |
| `taskId` | UUID | task identifier |

#### Body

| Field | Type | Required | Allowed values / rules |
|---|---|---|---|
| `status` | string | yes | `OPEN`, `DONE`, `DISMISSED` |
| `dismissalReason` | string \| null | no | `TOO_SOON`, `NOT_RELEVANT`, `ALREADY_HANDLED`; required only for `DISMISSED` |

#### Important rules

- `DISMISSED` is allowed only for auto-created tasks
- suggested tasks appear automatically only after new `WATERING` or `FEEDING` logs
- an identical open suggested task is not duplicated
- a dismissed suggested task is not recreated during its cooldown window
- suggested tasks cannot be edited directly; only their status can be changed
- `dismissalReason` is mandatory for `DISMISSED`
- `dismissalReason` must not be sent for `OPEN` or `DONE`

#### Response

- `200 OK`
- returns the updated `MaintenanceTaskResponse`

## 5. Page routes outside `/api/v1`

These routes are not part of the REST API, but they matter for a complete application view.

### `GET /login`

- returns the login page

### `GET /register`

- returns the registration page

### `GET /profile`

- returns the profile page

### `POST /login`

- Spring Security form login
- accepts `application/x-www-form-urlencoded`

Form fields:

| Field | Type | Required |
|---|---|---|
| `username` | string | yes |
| `password` | string | yes |

### `POST /logout`

- ends the current user session

## 6. Where to find the machine-readable spec

The application already exposes the runtime specification through:

- Swagger UI: `http://localhost:8085/swagger-ui.html`
- OpenAPI JSON: `http://localhost:8085/api-docs`
- OpenAPI YAML: `http://localhost:8085/api-docs.yaml`

## Summary

This omission is now closed:

- all main endpoints are listed
- path/query/body parameters are documented
- base response structures are listed
- both REST API and page routes are included
