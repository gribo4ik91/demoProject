<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>EcoTracker | Home</title>
    <link rel="icon" href="/favicon.svg" type="image/svg+xml">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="/css/home.css" rel="stylesheet">
    <script src="https://unpkg.com/htmx.org@2.0.4"></script>
</head>
<body>
<div class="container shell py-5">
    <div class="d-flex justify-content-between align-items-center mb-4 gap-3 flex-wrap">
        <div>
            <p class="text-uppercase text-success fw-semibold mb-1">EcoTracker</p>
            <h1 class="h3 mb-0">Ecosystem workspace</h1>
        </div>
        <#if auth.authenticated>
            <div class="d-flex align-items-center gap-3 flex-wrap">
                <span class="text-muted small">${auth.displayName!"Signed in"}<#if auth.role??> | ${auth.role}</#if></span>
                <a href="/users" class="btn btn-outline-success btn-sm">Users</a>
                <a href="/profile" class="btn btn-outline-success btn-sm">My Profile</a>
                <form action="/logout" method="post" class="mb-0">
                    <button type="submit" class="btn btn-outline-secondary btn-sm">Log out</button>
                </form>
            </div>
        </#if>
    </div>

    <section class="panel p-4 mb-4">
        <div class="d-flex justify-content-between align-items-center mb-3 gap-3 flex-wrap">
            <div>
                <p class="section-label mb-1">Create</p>
                <h3 class="h5 mb-0">Add New Ecosystem</h3>
            </div>
            <div class="text-muted small">Start simple and grow each setup over time.</div>
        </div>
        <div id="create-ecosystem-feedback" class="mb-3"></div>
        <form hx-post="/ui/ecosystems" hx-target="#create-ecosystem-feedback" hx-swap="innerHTML">
            <div class="row">
                <div class="col-md-4 mb-2"><input type="text" class="form-control" name="name" maxlength="100" placeholder="Name" required></div>
                <div class="col-md-4 mb-2">
                    <select class="form-select" name="type" required>
                        <#list ui.ecosystemTypes() as option>
                            <option value="${option}">${ui.ecosystemTypeLabel(option)}</option>
                        </#list>
                    </select>
                </div>
                <div class="col-md-3 mb-2"><input type="text" class="form-control" name="description" maxlength="500" placeholder="Description"></div>
                <div class="col-md-1 mb-2"><button type="submit" class="btn btn-success w-100">Add</button></div>
            </div>
        </form>
    </section>

    <section class="panel p-4 mb-4">
        <div class="d-flex justify-content-between align-items-center mb-3 gap-3 flex-wrap">
            <div>
                <p class="section-label mb-1">Explore</p>
                <h3 class="h5 mb-0">Workspace Controls</h3>
            </div>
            <a href="/" class="btn btn-outline-secondary btn-sm">Clear Filters</a>
        </div>
        <form id="workspace-filter-form" class="toolbar-grid">
            <div>
                <label class="form-label small text-muted" for="search-input">Search ecosystems</label>
                <input id="search-input" name="search" type="search" class="form-control" value="${search}" placeholder="Search by name, type, or description"
                       hx-get="/ui/workspace" hx-trigger="input changed delay:350ms, search" hx-include="#workspace-filter-form" hx-target="#workspace-panel" hx-swap="innerHTML">
            </div>
            <div>
                <label class="form-label small text-muted" for="status-filter">Status filter</label>
                <select id="status-filter" name="status" class="form-select"
                        hx-get="/ui/workspace" hx-trigger="change" hx-include="#workspace-filter-form" hx-target="#workspace-panel" hx-swap="innerHTML">
                    <option value="ALL" <#if status == "ALL">selected</#if>>All ecosystems</option>
                    <option value="NEEDS_ATTENTION" <#if status == "NEEDS_ATTENTION">selected</#if>>Needs Attention</option>
                    <option value="STABLE" <#if status == "STABLE">selected</#if>>Stable</option>
                    <option value="NO_RECENT_DATA" <#if status == "NO_RECENT_DATA">selected</#if>>No Recent Data</option>
                    <option value="OVERDUE" <#if status == "OVERDUE">selected</#if>>Has Overdue Tasks</option>
                </select>
            </div>
            <div>
                <label class="form-label small text-muted" for="sort-select">Sort by</label>
                <select id="sort-select" name="sort" class="form-select"
                        hx-get="/ui/workspace" hx-trigger="change" hx-include="#workspace-filter-form" hx-target="#workspace-panel" hx-swap="innerHTML">
                    <option value="PRIORITY" <#if sort == "PRIORITY">selected</#if>>Priority</option>
                    <option value="LAST_ACTIVITY" <#if sort == "LAST_ACTIVITY">selected</#if>>Last Activity</option>
                    <option value="NAME" <#if sort == "NAME">selected</#if>>Name</option>
                    <option value="NEWEST" <#if sort == "NEWEST">selected</#if>>Newest</option>
                </select>
            </div>
        </form>
    </section>

    <div id="workspace-panel">
        <#include "../fragments/workspace-panel.ftlh">
    </div>
</div>
<script src="/js/ssr-ui.js"></script>
</body>
</html>
