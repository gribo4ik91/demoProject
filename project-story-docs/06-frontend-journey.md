# Story 06. Frontend User Journey

## User story

As a user, I want to perform the main workflows through a simple web interface so that I can manage ecosystems without external API tools and without a heavy UI stack.

## Overall UI model

The frontend is backend-hosted and built from static HTML pages with JavaScript.
It is not implemented as a separate SPA.

This means:

- backend and UI are delivered together
- pages are loaded directly from the application
- data is fetched via REST endpoints using `fetch`

## Main pages

### 1. Home page `index.html`

Purpose:

- show the list of ecosystems
- provide a form to create a new ecosystem
- display the current user status when auth is enabled

User flow:

1. Load authentication status
2. Load the ecosystem list
3. Create a new ecosystem through the form
4. Open the selected ecosystem dashboard

### 2. Ecosystem page `ecosystem.html`

This is the main working screen of the product.

It combines:

- summary status
- task creation and browsing
- task editing for manual tasks
- task filtering
- log creation and browsing
- log editing
- log pagination and filtering
- ecosystem editing
- ecosystem deletion
- status changes for tasks

From a business perspective, this is where the day-to-day work happens.

### 3. `login.html`

Purpose:

- provide the sign-in form
- display login error, logout, and post-registration messages
- explain that authentication may be disabled by configuration

### 4. `register.html`

Purpose:

- create a new user account
- collect extended profile fields

### 5. `profile.html`

Purpose:

- display current user data
- allow editing of visible profile fields

## UI behavior highlights

- the interface shows toast notifications for page-level success and error feedback
- authentication status is blended into the page header via `auth/status`
- the dashboard refreshes summary data after task and log changes
- tasks can be filtered by both status and source
- logs are loaded page by page
- manual task edit flows run inline in the dashboard
- logs can also be corrected inline through the update flow
- suggested tasks remain status-driven and are not content-editable

## Architectural assessment of the frontend approach

Strengths:

- simple to run
- single deployment unit
- fast end-to-end demo setup
- low maintenance overhead

Limitations:

- no frontend component model
- page logic lives directly in HTML/JS files
- UI scalability will become harder as the product grows

## Summary

The current frontend approach fits the size and purpose of the solution well.
It is a strong choice for a demo-oriented full-stack project and fully supports the main user journey without unnecessary infrastructure complexity.
