# Story 01. Product Overview

## Product story

As an owner of a small ecosystem, I want to keep my setups, activity history, and care tasks in one place so that I can understand the current condition of each ecosystem and avoid missing routine actions.

## Business goal

The system is designed to support the full core lifecycle of ecosystem care:

1. Register an ecosystem
2. Record observations and actions
3. View a summarized status
4. Manage maintenance tasks
5. Restrict access through login when needed

## Main roles

- Guest: can use the system when authentication is disabled by configuration
- Super admin: the first user in the system, can manage admin rights and delete admin or regular accounts
- Admin: can work with the application and delete regular user accounts
- Authenticated user: signs in, works with the application, and updates profile data
- System: analyzes recent data and automatically creates suggested follow-up tasks

## Solution boundaries

The project covers:

- ecosystem tracking
- basic temperature and humidity monitoring
- activity and observation logs
- maintenance tasks
- basic registration and login
- lightweight user governance with `SUPER_ADMIN`, `ADMIN`, and `USER` roles
- a static web UI served by the backend

The project does not cover:

- user-based ownership separation for ecosystems
- advanced analytics and charts
- email, push, or SMS notifications
- bulk operations and complex workflows

## Core business entities

- `Ecosystem`: a tracked ecosystem record
- `EcosystemLog`: an observation or activity event
- `MaintenanceTask`: a maintenance reminder
- `AppUser`: an application user for secured access mode

## Key product logic

- each ecosystem can have many logs
- each ecosystem can have many maintenance tasks
- a `WATERING` or `FEEDING` log can generate a suggested follow-up task
- ecosystem status is calculated automatically from recent logs and tasks
- authentication can be turned on or off through configuration

## Architectural meaning

The project is built as a vertical slice: backend, database, API, tests, and UI live in one solution.
That makes it easy to demonstrate the complete lifecycle of a business feature:

- business request
- API contract
- service logic
- database persistence
- browser rendering

## Summary

From a business perspective, `EcoTracker` is an operational care journal for small ecosystems.
From an architectural perspective, it is a compact modular system in which every feature moves through clear layers: controller -> service -> repository -> database -> UI.
