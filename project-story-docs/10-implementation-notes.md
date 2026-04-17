# Story 10. Technical Implementation Notes

## Why this file exists

After another review, it became clear that the project was already well documented from a functional and architectural perspective, but some technical mechanisms were described only indirectly rather than as implementation guidance.

This file captures how the supporting but important layers are implemented.

## 1. Mapper layer

### Purpose

The mapper layer is responsible for converting:

- request DTO -> entity
- entity -> response DTO

### Where it is implemented

- `application/application/src/main/kotlin/com/example/api/mapper/ApiMappings.kt`
- `application/application/src/main/kotlin/com/example/api/mapper/MaintenanceTaskMappings.kt`

### Current implementation rules

- `Ecosystem` creation uses the extension function `CreateEcosystemRequest.toEntity()`
- update DTOs apply changes through dedicated mapper extension functions where that keeps normalization centralized
- conversion of `Ecosystem`, `EcosystemLog`, and `MaintenanceTask` to API models uses `toResponse()`
- the mapper layer does not contain business logic
- the mapper layer may perform only safe normalization such as `trim()`

### Practical rule

If a new endpoint or DTO is added, the transformation should be added to the mapper layer instead of mixing it into controllers or services.

## 2. Error handling

### Purpose

The project uses a consistent REST error format so that the frontend and tests can rely on a predictable response structure.

### Where it is implemented

- `application/application/src/main/kotlin/com/example/api/exception/GlobalExceptionHandler.kt`
- `application/application/src/main/kotlin/com/example/api/exception/ApiErrorResponse.kt`

### Current implementation rules

Three main cases are handled:

- `MethodArgumentNotValidException` -> `400 Bad Request`
- `ResponseStatusException` -> status taken from the exception
- any other `Exception` -> `500 Internal Server Error`

### Error payload structure

The shared error payload includes:

- `timestamp`
- `status`
- `error`
- `message`
- `path`
- `validationErrors`

### Practical rule

If new business logic must return a controlled error, it should use `ResponseStatusException` or a compatible custom approach aligned with the current `GlobalExceptionHandler`.

## 3. OpenAPI and Swagger

### Purpose

The OpenAPI configuration provides machine-readable and human-readable API documentation.

### Where it is implemented

- `application/application/src/main/kotlin/com/example/api/config/OpenApiConfig.kt`

### What is fixed in the implementation

- title: `EcoTracker API`
- version: `v1`
- local server URL: `http://localhost:8085`
- available through Swagger UI and OpenAPI docs endpoints

### Practical rule

When adding new endpoints, the project should:

- keep Swagger annotations on controller methods
- keep request/response DTO descriptions updated
- avoid drift between runtime API and story documentation

Because the new update endpoints are already annotated in the controllers, they should be exposed automatically in Swagger UI once the application is running.

## 4. Response delay mechanism

### Purpose

The response delay mechanism exists for demo scenarios and manual UI verification under imperfect network-like behavior.

### Where it is implemented

- `application/application/src/main/kotlin/com/example/api/config/ResponseDelayFilter.kt`
- `application/application/src/main/kotlin/com/example/api/config/ResponseDelayProperties.kt`

### How it works

- the filter applies only to routes starting with `/api/`
- if `app.response-delay.enabled=false`, no delay is applied
- when enabled, the system chooses a random delay in the `minMs..maxMs` range
- the range is validated by the rule `minMs <= maxMs`

### Practical rule

If the project introduces new API-prefixed routes outside `/api/`, it should be decided explicitly whether the delay mechanism should apply to them.

## 5. Repository usage pattern

### Purpose

The repository layer should remain a thin data access layer.

### Current pattern

- repositories are used only from the service layer
- controllers should not work with repositories directly
- selection, sorting, and filtering behavior are coordinated through repository methods plus service orchestration

### Practical rule

New read/write behavior should first be modeled as a business use case in the service layer and only then supported by repository methods.

## 6. Workspace dashboard aggregation pattern

### Purpose

The home page workspace now needs richer cards and overview counters without rebuilding a full ecosystem summary for every visible item.

### Current implementation direction

- workspace cards are assembled in the service layer from aggregated repository reads
- latest log snapshots are loaded in grouped form
- recent log counts are loaded in grouped form
- task counters are loaded in grouped form
- card-level search, status filtering, sorting, and pagination are coordinated after the shared snapshot is assembled
- overview reuses the same filtered workspace snapshot so counters match the visible filter context

### Practical rule

If the workspace dashboard grows further, new home page metrics should prefer shared aggregation inputs over per-ecosystem summary rebuilding.
This keeps the home page cheaper than the detailed dashboard and reduces drift between cards and overview counters.

## 7. Testing strategy

### Where it is implemented

- `application/application/src/test/kotlin/com/example/api/controller`
- `application/application/src/test/kotlin/com/example/api/integration`
- `application/application/src/test/kotlin/com/example/api/config`

### What is already covered

- startup smoke
- controller behavior
- integration flow for ecosystems, logs, and tasks
- auth-enabled and auth-disabled scenarios
- response delay related checks

### Current direction

- integration tests are being moved toward Testcontainers + PostgreSQL so the test database matches runtime behavior
- focused integration scenarios now cover manual edit flows and richer dashboard summary analytics
- controller coverage now also protects the workspace cards contract, including filter forwarding and paged response shape
- auth-enabled integration scenarios now also protect the `SUPER_ADMIN` / `ADMIN` / `USER` hierarchy and role-aware user management rules

### Practical rule

If a new business feature is added, it should ideally be checked at three levels:

- validation / controller contract
- service or integration flow
- negative error scenarios

## 8. What is intentionally not documented in detail

At this stage, a dedicated implementation document is not needed for:

- `.idea`
- `build/`
- Gradle internal caches
- generated artifacts

These files do not provide long-term architectural value for understanding the project.

## Summary

With this file added, the documentation now covers not only business behavior and API contracts, but also implementation rules for the supporting technical layers.
That means the docs now describe not only what exists in the project, but also how those parts are expected to be implemented.
