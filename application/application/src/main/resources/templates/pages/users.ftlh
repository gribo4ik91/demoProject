<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>EcoTracker | Users</title>
    <link rel="icon" href="/favicon.svg" type="image/svg+xml">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="/css/auth.css" rel="stylesheet">
    <link href="/css/users.css" rel="stylesheet">
    <script src="https://unpkg.com/htmx.org@2.0.4"></script>
</head>
<body class="py-5 users-page">
<div class="container users-shell">
    <div id="users-feedback" class="mb-4"></div>

    <div class="d-flex justify-content-between align-items-center flex-wrap gap-3 mb-4">
        <div>
            <p class="text-uppercase text-success fw-semibold mb-2">EcoTracker Directory</p>
            <h1 class="h3 mb-1">Users</h1>
            <p class="text-muted mb-0">
                <#if auth.displayName??>
                    Signed in as ${auth.displayName}.
                <#else>
                    Signed-in users can see the full directory.
                </#if>
            </p>
        </div>
        <div class="d-flex gap-2">
            <a href="/" class="btn btn-outline-secondary">Back to Home</a>
            <a href="/profile" class="btn btn-outline-secondary">My Profile</a>
            <form action="/logout" method="post" class="mb-0">
                <button type="submit" class="btn btn-outline-secondary">Log out</button>
            </form>
        </div>
    </div>

    <section class="users-panel p-4 mb-4">
        <div class="d-flex justify-content-between align-items-center gap-3 flex-wrap">
            <div>
                <p class="section-label mb-1">Access</p>
                <h2 class="h5 mb-0">Directory visibility</h2>
            </div>
            <div class="${ui.roleBadgeClass(auth.role)}">${auth.role!"USER"}</div>
        </div>
        <p class="text-muted small mb-0 mt-3">
            <#if auth.role == "ADMIN">
                You can review all accounts and delete users except your own admin account.
            <#else>
                You can view all registered users, but only admins can delete accounts.
            </#if>
        </p>
    </section>

    <section class="users-panel p-4">
        <div class="d-flex justify-content-between align-items-center gap-3 flex-wrap mb-3">
            <div>
                <p class="section-label mb-1">Accounts</p>
                <h2 class="h5 mb-0">Registered users</h2>
            </div>
            <div class="text-muted small">${users?size} user<#if users?size != 1>s</#if></div>
        </div>
        <div class="row g-3">
            <#if users?size == 0>
                <div class="col-12"><div class="border rounded-3 p-4 text-center text-muted">No users found.</div></div>
            <#else>
                <#list users as user>
                    <div class="col-md-6 col-xl-4">
                        <div class="user-card p-4">
                            <div class="d-flex justify-content-between align-items-start gap-3 mb-3">
                                <div>
                                    <h3 class="h5 mb-1">${user.displayName}</h3>
                                    <div class="text-muted small">@${user.username}</div>
                                </div>
                                <span class="${ui.roleBadgeClass(user.role)}">${user.role}</span>
                            </div>
                            <div class="user-meta mb-3">
                                <div>${user.firstName} ${user.lastName}</div>
                                <div>${user.email}</div>
                                <div>${user.location!"Location not set"}</div>
                                <div>Created: ${ui.formatDateTime(user.createdAt)}</div>
                            </div>
                            <div class="d-flex justify-content-between align-items-center gap-2">
                                <span class="text-muted small">
                                    <#if auth.username == user.username>This is your account<#else>Account member</#if>
                                </span>
                                <#if auth.role == "ADMIN" && auth.username != user.username>
                                    <button type="button" class="btn btn-outline-danger btn-sm"
                                            hx-delete="/ui/users/${user.id}"
                                            hx-confirm="Delete user ${user.displayName} (@${user.username})?"
                                            hx-target="#users-feedback"
                                            hx-swap="innerHTML">Delete</button>
                                </#if>
                            </div>
                        </div>
                    </div>
                </#list>
            </#if>
        </div>
    </section>
</div>
</body>
</html>
