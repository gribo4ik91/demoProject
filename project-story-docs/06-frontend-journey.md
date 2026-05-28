# Story 06. Frontend User Journey

## User story

As a user, I want to perform the main workflows through a simple web interface so that I can manage ecosystems without external API tools and without a heavy UI stack.

## Overall UI model

The frontend is backend-hosted and rendered through Spring MVC + Freemarker templates.
It is not implemented as a separate SPA.

This means:

- backend and UI are delivered together
- full pages are rendered directly by the application
- smaller interactions are updated through `htmx`
- lightweight browser-only behavior is still allowed for cases such as local pinning or edit toggles

## Main pages

### 1. Home page `/`

Purpose:

- act as a workspace dashboard for the full ecosystem portfolio
- provide a form to create a new ecosystem
- surface overview counters, urgent ecosystems, pinned ecosystems, and quick actions
- show a compact inventory audit preview
- display the current user status when auth is enabled

User flow:

1. Load authentication status
2. Load workspace overview counters
3. Load the first page of enriched ecosystem cards
4. Apply search, status filter, and sorting controls
5. Review the "Needs Attention First" section
6. Pin or unpin ecosystems locally in the browser
7. Create a new ecosystem through the SSR form
8. Trigger quick log or quick task actions directly from a card
9. Move through paged ecosystem cards without leaving the page
10. Review the compact recent-changes preview
11. Open `/audit` when the full paged inventory change log is needed
12. Open the selected ecosystem dashboard
13. Open profile or users pages from the shared authenticated header

### 2. Ecosystem page `/ecosystems/{id}`

This is the main working screen of the product.

It combines:

- summary status
- prominent current-status banner
- task creation and browsing
- task editing for manual tasks
- task filtering
- log creation and browsing
- log editing
- log pagination and filtering
- ecosystem view mode plus explicit edit mode
- ecosystem deletion
- status changes for tasks
- creator labels for the ecosystem, each log entry, and each maintenance task

From a business perspective, this is where the day-to-day work happens.

### 3. `/login`

Purpose:

- provide the sign-in form
- display login error, logout, and post-registration messages
- explain that authentication may be disabled by configuration

### 4. `/register`

Purpose:

- create a new user account
- collect extended profile fields

### 5. `/profile`

Purpose:

- display current user data
- allow editing of visible profile fields
- show the current account role

### 6. `/users`

Purpose:

- list all registered users for authenticated accounts
- show role-based access information
- show `Make admin` / `Remove admin` actions only to the `SUPER_ADMIN`
- show delete actions according to role hierarchy

### 7. `/automation-rules`

Purpose:

- list configurable suggested-task rules
- filter rules by status and rule family
- create a new rule through the SSR builder
- edit existing rules
- enable or disable rules
- delete rules
- preview what kind of suggested task a rule will generate

### 8. `/audit`

Purpose:

- browse the full inventory change history
- page through older ecosystem, log, task, and automation-rule changes
- keep detailed audit history available without making the home page visually heavy

## UI behavior highlights

- the interface shows toast notifications for page-level success and error feedback
- authentication status is blended into the page header via server-rendered auth state
- the home page combines a hero section, workspace counters, filters, urgency grouping, pinned grouping, and the main card grid
- the home page renders only a compact audit preview, while `/audit` carries the full paged history
- home page cards are now enriched by backend-computed status, freshness, log counts, and task counts
- home page search and status filters are executed server-side so counters and card lists stay aligned
- home page card updates run through SSR fragments and htmx rather than bespoke page fetch logic
- quick log and quick task actions run through inline panel flows on the home page
- home and dashboard forms expose browser-side constraints that mirror backend validation where possible
- htmx form failures show field-level feedback next to the affected controls
- login failures distinguish login-field errors from password-field errors
- pinned ecosystems are still browser-local and currently use `localStorage`
- the dashboard refreshes SSR fragments or full page state after task and log changes
- tasks can be filtered by both status and source
- tasks can also be searched by title
- logs are loaded page by page
- manual task edit flows run inline in the dashboard
- logs can also be corrected inline through the update flow
- suggested tasks remain status-driven and are not content-editable
- automation rules are managed from a dedicated SSR page rather than through code changes
- the users page is visible to every signed-in account
- regular users do not see destructive actions
- admins see delete actions only for regular users
- the super admin sees admin-role actions and can delete admins or regular users
- creator labels are rendered from backend snapshots so history still makes sense after account deletion

## Architectural assessment of the frontend approach

Strengths:

- simple to run
- single deployment unit
- fast end-to-end demo setup
- low maintenance overhead

Limitations:

- no frontend component model
- page templates and a small browser enhancement layer still need manual coordination
- UI scalability will become harder as the product grows

## Summary

The current SSR + htmx frontend approach fits the size and purpose of the solution well.
It is a strong choice for a demo-oriented full-stack project and fully supports the main user journey without unnecessary infrastructure complexity.

Recent iterations made the home page materially richer: instead of being only an entry list, it now acts as a lightweight workspace dashboard and delegates more filtering and aggregation logic to the backend.
It also now acts as a lightweight change feed by showing a compact recent-changes preview for ecosystem, log, task, and automation-rule changes.
