# Story 05. Authentication and User Profile

## User story

As a user, I want to sign in when needed and update my profile so that I can use the application in a secured mode and see a personalized account experience.

## Purpose of the feature

Authentication is optional in this project.
That means the product can operate in two modes:

- open demo mode
- secured login mode

This makes the solution suitable for learning, demos, and lightweight local use cases.

## Key business scenarios

### Scenario 1. Check security mode

At startup, the application reads:

- `app.auth.enabled`

If it is turned off:

- all requests are allowed without login
- login/logout flows are disabled in the security configuration

If it is turned on:

- only login/register/status and public login pages remain open
- all other routes require authentication

### Scenario 2. Register a user

The user fills in:

- display name
- login
- first name
- last name
- email
- location
- bio
- password

The system:

- normalizes text fields
- lowercases the email
- checks `username` uniqueness
- hashes the password with BCrypt
- creates the user record

### Scenario 3. Sign in

The user goes through form login.
After successful authentication, the system redirects to the home page and keeps a server-side session.

### Scenario 4. View and edit profile

The authenticated user opens the profile page.
The system allows reading and updating:

- display name
- first name
- last name
- email
- location
- bio

The username and password are not changed on this page.

## Business rules

- `username` must be unique
- password length must be between 6 and 72 characters
- `displayName` must be between 3 and 60 characters
- `firstName` and `lastName` must be between 2 and 60 characters
- `email` must be valid and at most 120 characters
- `location` can be up to 80 characters
- `bio` can be up to 500 characters

## API operations

- `POST /api/v1/auth/register`
- `GET /api/v1/auth/profile`
- `PUT /api/v1/auth/profile`
- `GET /api/v1/auth/status`

## What auth status returns

The authentication status endpoint helps the frontend determine:

- whether security is enabled at all
- whether a user is currently signed in
- which user information should be shown in the header

The response includes:

- `enabled`
- `authenticated`
- `username`
- `displayName`

## Architectural implementation

- `SecurityConfig` switches between open mode and protected mode
- `AuthController` exposes registration and profile endpoints
- `AuthService` implements normalization, lookup, update, and registration logic
- `AppUserRepository` and the `app_user` table provide persistence

## Architectural note

The current authentication model protects access to the application, but it does not isolate business data per user.
Ecosystems are not linked to a specific owner, so the feature currently secures entry into the product rather than implementing true multi-user ownership.

## Summary

The authentication module makes the project more product-like while intentionally staying lightweight.
It is a practical balance between security, simplicity, and demo friendliness.
