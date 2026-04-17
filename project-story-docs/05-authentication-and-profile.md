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
- assigns role `SUPER_ADMIN` to the first created account
- assigns role `USER` to every later account

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

### Scenario 5. View the user directory

Any authenticated user can open the shared users page and review all registered accounts.

The directory shows:

- display name
- login
- role
- basic contact and profile fields used by the page

### Scenario 6. Delete a user

Admins and the super admin can delete user accounts, but with different limits.

The system also prevents chaotic cleanup:

- regular users cannot delete anyone
- admins can delete only regular users
- the super admin can delete admins and regular users
- no user can delete their own account from the directory
- creator labels on old ecosystems, logs, and tasks remain visible through stored creator snapshots

### Scenario 7. Grant or remove admin rights

Only the `SUPER_ADMIN` can change another user's role between `USER` and `ADMIN`.

The system protects the hierarchy:

- the first user remains the only `SUPER_ADMIN`
- the super admin cannot change their own role
- regular users cannot assign or remove admin rights
- admin role can be granted to a regular user and later removed back to `USER`

## Business rules

- `username` must be unique
- the first created account becomes `SUPER_ADMIN`
- all later accounts become `USER`
- every authenticated account can read the directory of users
- only the `SUPER_ADMIN` can assign or remove `ADMIN` rights
- `ADMIN` can delete only `USER` accounts
- `SUPER_ADMIN` can delete `ADMIN` and `USER` accounts
- no user can delete their own account from the directory
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
- `GET /api/v1/auth/users`
- `DELETE /api/v1/auth/users/{userId}`
- `PUT /api/v1/auth/users/{userId}/role`

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
- `role`

## Architectural implementation

- `SecurityConfig` switches between open mode and protected mode
- `AuthController` exposes registration, profile, directory, deletion, and role-update endpoints
- `AuthService` implements normalization, lookup, update, registration, role assignment, directory loading, super-admin role governance, and role-aware deletion logic
- `AppUserRepository` and the `app_user` table provide persistence

## Architectural note

The current authentication model still protects entry into the product rather than implementing true multi-user ownership.
At the same time, it now adds lightweight governance through:

- one automatic super-admin account
- a shared user directory
- controlled promotion and demotion of admins
- creator attribution on ecosystems, logs, and tasks

## Summary

The authentication module makes the project more product-like while intentionally staying lightweight.
It is a practical balance between security, simplicity, and demo friendliness.
