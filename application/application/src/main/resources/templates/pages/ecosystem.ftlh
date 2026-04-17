<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>EcoTracker | ${ecosystem.name}</title>
    <link rel="icon" href="/favicon.svg" type="image/svg+xml">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="/css/ecosystem.css" rel="stylesheet">
    <script src="https://unpkg.com/htmx.org@2.0.4"></script>
</head>
<body class="bg-light">
<div class="container mt-5">
    <nav aria-label="breadcrumb">
        <ol class="breadcrumb">
            <li class="breadcrumb-item"><a href="/" class="text-success">Home</a></li>
            <li class="breadcrumb-item active" aria-current="page">${ecosystem.name}</li>
        </ol>
    </nav>

    <#if auth.authenticated>
        <div class="d-flex justify-content-end align-items-center gap-3 mb-3 flex-wrap">
            <span class="text-muted small">${auth.displayName!"Signed in"}<#if auth.role??> | ${auth.role}</#if></span>
            <a href="/automation-rules" class="btn btn-outline-success btn-sm">Automation Rules</a>
            <a href="/users" class="btn btn-outline-success btn-sm">Users</a>
            <a href="/profile" class="btn btn-outline-success btn-sm">My Profile</a>
            <form action="/logout" method="post" class="mb-0">
                <button type="submit" class="btn btn-outline-secondary btn-sm">Log out</button>
            </form>
        </div>
    </#if>

    <div class="d-flex justify-content-between align-items-center mb-4 gap-3 flex-wrap">
        <div>
            <h2 class="text-success mb-1">${ecosystem.name}</h2>
            <p class="text-muted small mb-1">${ui.ecosystemTypeLabel(ecosystem.type)} | Created by ${ui.createdByLabel(ecosystem.createdByDisplayName, ecosystem.createdByUsername)}</p>
            <p class="text-muted small mb-0">${ecosystem.description!"No description added yet."}</p>
        </div>
        <button class="btn btn-danger"
                hx-delete="/ui/ecosystems/${ecosystem.id}"
                hx-confirm="Delete this ecosystem together with its logs and tasks?"
                hx-target="#ecosystem-action-feedback"
                hx-swap="innerHTML">Delete Ecosystem</button>
    </div>

    <div id="ecosystem-action-feedback" class="mb-3"></div>

    <div class="card shadow-sm border-0 ecosystem-overview-card mb-4">
        <div class="card-body p-3 p-lg-4">
            <div class="row g-3 align-items-start">
                <div class="col-lg-7">
                    <div class="summary-shell h-100">
                        <div class="summary-status-banner summary-status-${summary.status?lower_case} mb-3">
                            <div class="summary-status-label">Current Status</div>
                            <div class="summary-status-value">${ui.statusLabel(summary.status)}</div>
                            <div class="summary-status-note">
                                <#if summary.status == "NEEDS_ATTENTION">
                                    This ecosystem currently needs attention based on recent readings or overdue work.
                                <#elseif summary.status == "STABLE">
                                    Recent readings and maintenance activity look stable.
                                <#else>
                                    There is not enough recent activity to assess the ecosystem confidently yet.
                                </#if>
                            </div>
                        </div>
                        <h5 class="mb-3">Dashboard Summary</h5>
                        <div class="row g-3">
                            <div class="col-md-6"><div class="border rounded-3 p-3 h-100"><div class="small text-muted">Last activity</div><div class="fw-semibold">${ui.formatDateTime(summary.lastRecordedAt)}</div></div></div>
                            <div class="col-md-6"><div class="border rounded-3 p-3 h-100"><div class="small text-muted">Latest event</div><div class="fw-semibold">${ui.eventTypeLabel(summary.latestEventType)}</div></div></div>
                            <div class="col-md-6"><div class="border rounded-3 p-3 h-100"><div class="small text-muted">Logs last 7 days</div><div class="fw-semibold">${summary.logsLast7Days}</div></div></div>
                            <div class="col-md-6"><div class="border rounded-3 p-3 h-100"><div class="small text-muted">Open tasks</div><div class="fw-semibold">${summary.openTasks}</div></div></div>
                            <div class="col-md-6"><div class="border rounded-3 p-3 h-100"><div class="small text-muted">Current temperature</div><div class="fw-semibold">${summary.currentTemperatureC!"-"} C</div></div></div>
                            <div class="col-md-6"><div class="border rounded-3 p-3 h-100"><div class="small text-muted">Current humidity</div><div class="fw-semibold">${summary.currentHumidityPercent!"-"}%</div></div></div>
                        </div>
                    </div>
                </div>
                <div class="col-lg-5">
                    <div class="edit-ecosystem-shell h-100">
                        <p class="section-label mb-1">Manage</p>
                        <h5 class="mb-1">Edit Ecosystem</h5>
                        <p class="text-muted small mb-3">Update the main ecosystem details without affecting logs or tasks.</p>
                        <div id="edit-ecosystem-feedback" class="mb-3"></div>
                        <div class="ecosystem-view-mode" data-ecosystem-view>
                            <div class="ecosystem-preview-grid">
                                <div class="ecosystem-preview-item">
                                    <div class="small text-muted">Name</div>
                                    <div class="fw-semibold">${ecosystem.name}</div>
                                </div>
                                <div class="ecosystem-preview-item">
                                    <div class="small text-muted">Type</div>
                                    <div class="fw-semibold">${ui.ecosystemTypeLabel(ecosystem.type)}</div>
                                </div>
                                <div class="ecosystem-preview-item ecosystem-preview-item-wide">
                                    <div class="small text-muted">Description</div>
                                    <div class="fw-semibold">${ecosystem.description!"No description added yet."}</div>
                                </div>
                            </div>
                            <div class="d-flex justify-content-end mt-3">
                                <button type="button" class="btn btn-outline-success" data-open-ecosystem-edit>Edit Ecosystem</button>
                            </div>
                        </div>
                        <form class="row g-3 d-none"
                              data-ecosystem-edit-form
                              hx-patch="/ui/ecosystems/${ecosystem.id}"
                              hx-target="#edit-ecosystem-feedback"
                              hx-swap="innerHTML">
                            <div class="col-12">
                                <label class="form-label small text-muted mb-1">Name</label>
                                <input type="text" class="form-control" name="name" maxlength="100" value="${ecosystem.name}" required>
                            </div>
                            <div class="col-md-6">
                                <label class="form-label small text-muted mb-1">Type</label>
                                <select name="type" class="form-select" required>
                                    <#list ui.ecosystemTypes() as option>
                                        <option value="${option}" <#if option == ecosystem.type>selected</#if>>${ui.ecosystemTypeLabel(option)}</option>
                                    </#list>
                                </select>
                            </div>
                            <div class="col-md-6">
                                <label class="form-label small text-muted mb-1">Description</label>
                                <textarea class="form-control ecosystem-description-input" name="description" maxlength="500" rows="4" placeholder="Description">${ecosystem.description!""}</textarea>
                            </div>
                            <div class="col-12 d-flex justify-content-end gap-2">
                                <button type="button" class="btn btn-outline-secondary" data-cancel-ecosystem-edit>Cancel Changes</button>
                                <button type="submit" class="btn btn-outline-success">Save Changes</button>
                            </div>
                        </form>
                    </div>
                </div>
            </div>
        </div>
    </div>

    <div class="row">
        <div class="col-lg-5 mb-4">
            <div class="card shadow-sm border-0 ecosystem-section-card h-100">
                <div class="card-header bg-white section-card-header">
                    <h5 class="mb-0">Maintenance Tasks</h5>
                </div>
                <div class="card-body p-3 p-lg-4">
                    <div id="task-form-feedback" class="mb-3"></div>
                    <div class="section-block mb-4">
                        <p class="section-label mb-1">Create</p>
                        <h6 class="mb-1">Add a maintenance task</h6>
                        <p class="text-muted small mb-0">Use these fields to create a manual reminder for this ecosystem.</p>
                    </div>
                    <form class="row g-2 mb-4" hx-post="/ui/ecosystems/${ecosystem.id}/tasks" hx-target="#task-form-feedback" hx-swap="innerHTML">
                        <div class="col-12"><input type="text" class="form-control" name="title" maxlength="120" placeholder="Task title" required></div>
                        <div class="col-md-6">
                            <select class="form-select" name="taskType" required>
                                <#list ui.taskTypes() as option>
                                    <option value="${option}">${ui.taskTypeLabel(option)}</option>
                                </#list>
                            </select>
                        </div>
                        <div class="col-md-6"><input type="date" class="form-control" name="dueDate"></div>
                        <div class="col-12 d-flex justify-content-end gap-2">
                            <button type="reset" class="btn btn-outline-secondary">Cancel</button>
                            <button type="submit" class="btn btn-success">Add Task</button>
                        </div>
                    </form>

                    <div class="section-block section-divider mb-3 pt-3">
                        <p class="section-label mb-1">Filter</p>
                        <h6 class="mb-1">Find the right task</h6>
                        <p class="text-muted small mb-0">Search by task title or narrow the list by status and source.</p>
                    </div>
                    <form id="task-filter-form" class="row g-2 mb-3">
                        <div class="col-12">
                            <label class="form-label small text-muted mb-1">Search by task title</label>
                            <input type="search"
                                   class="form-control"
                                   name="taskSearch"
                                   value="${taskSearch!""}"
                                   placeholder="Search by task title"
                                   hx-get="/ui/ecosystems/${ecosystem.id}/tasks"
                                   hx-trigger="input changed delay:300ms, search"
                                   hx-include="#task-filter-form"
                                   hx-target="#tasks-panel"
                                   hx-swap="innerHTML">
                        </div>
                        <div class="col-md-6">
                            <label class="form-label small text-muted mb-1">Task status</label>
                            <select class="form-select" name="taskFilter" hx-get="/ui/ecosystems/${ecosystem.id}/tasks" hx-include="#task-filter-form" hx-target="#tasks-panel" hx-swap="innerHTML">
                                <#list ui.taskFilters() as option>
                                    <option value="${option}" <#if option == taskFilter>selected</#if>>${option?replace("_", " ")?cap_first}</option>
                                </#list>
                            </select>
                        </div>
                        <div class="col-md-6">
                            <label class="form-label small text-muted mb-1">Task source</label>
                            <select class="form-select" name="taskSource" hx-get="/ui/ecosystems/${ecosystem.id}/tasks" hx-include="#task-filter-form" hx-target="#tasks-panel" hx-swap="innerHTML">
                                <#list ui.taskSources() as option>
                                    <option value="${option}" <#if option == taskSource>selected</#if>>${option?cap_first}</option>
                                </#list>
                            </select>
                        </div>
                        <div class="col-12 d-flex justify-content-end">
                            <button type="button"
                                    class="btn btn-outline-secondary btn-sm"
                                    data-reset-task-filters
                                    data-task-fragment-url="/ui/ecosystems/${ecosystem.id}/tasks">Reset filters</button>
                        </div>
                    </form>

                    <div id="tasks-panel">
                        <#assign ecosystemId = ecosystem.id>
                        <#include "../fragments/task-list.ftlh">
                    </div>
                </div>
            </div>
        </div>

        <div class="col-lg-7 mb-4">
            <div class="card shadow-sm border-0 ecosystem-section-card">
                <div class="card-header bg-white section-card-header">
                    <h5 class="mb-0">Activity Logs</h5>
                </div>
                <div class="card-body p-3 p-lg-4">
                    <div id="log-form-feedback" class="mb-3"></div>
                    <div class="section-block mb-4">
                        <p class="section-label mb-1">Create</p>
                        <h6 class="mb-1">Add an activity log</h6>
                        <p class="text-muted small mb-0">Capture a new observation, feeding, or watering event for this ecosystem.</p>
                    </div>
                    <form class="row g-2 mb-4" hx-post="/ui/ecosystems/${ecosystem.id}/logs" hx-target="#log-form-feedback" hx-swap="innerHTML">
                        <div class="col-md-2"><input type="number" step="0.1" class="form-control" name="temperatureC" placeholder="Temp C"></div>
                        <div class="col-md-2"><input type="number" class="form-control" name="humidityPercent" placeholder="Humidity %"></div>
                        <div class="col-md-3">
                            <select class="form-select" name="eventType" required>
                                <#list ui.logEventTypes() as option>
                                    <option value="${option}">${ui.eventTypeLabel(option)}</option>
                                </#list>
                            </select>
                        </div>
                        <div class="col-md-5"><input type="text" class="form-control" name="notes" maxlength="500" placeholder="Notes"></div>
                        <div class="col-12 d-flex justify-content-end gap-2">
                            <button type="reset" class="btn btn-outline-secondary">Cancel</button>
                            <button type="submit" class="btn btn-primary">Add Log</button>
                        </div>
                    </form>

                    <div class="section-block section-divider mb-3 pt-3">
                        <p class="section-label mb-1">Filter</p>
                        <h6 class="mb-1">Browse recorded activity</h6>
                        <p class="text-muted small mb-0">Narrow the log list by event type and page through older entries.</p>
                    </div>
                    <form id="log-filter-form" class="row g-2 align-items-end mb-3">
                        <div class="col-md-4">
                            <label for="log-filter-event" class="form-label small text-muted mb-1">Filter by event</label>
                            <select id="log-filter-event" class="form-select" name="eventType" hx-get="/ui/ecosystems/${ecosystem.id}/logs" hx-include="#log-filter-form" hx-target="#logs-panel" hx-swap="innerHTML">
                                <option value="" <#if logEventType == "">selected</#if>>All events</option>
                                <#list ui.logEventTypes() as option>
                                    <option value="${option}" <#if option == logEventType>selected</#if>>${ui.eventTypeLabel(option)}</option>
                                </#list>
                            </select>
                        </div>
                    </form>

                    <div id="logs-panel">
                        <#assign ecosystemId = ecosystem.id>
                        <#include "../fragments/log-list.ftlh">
                    </div>
                </div>
            </div>
        </div>
    </div>
</div>
<script src="/js/ssr-ui.js"></script>
</body>
</html>
