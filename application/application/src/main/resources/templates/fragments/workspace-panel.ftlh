<section class="panel hero p-4 mb-4">
    <div class="row g-4 align-items-center">
        <div class="col-lg-7">
            <p class="section-label mb-2">Home Base</p>
            <h2 class="h1 fs-2 mb-3">Track small ecosystems with a clearer workspace overview.</h2>
            <p class="text-muted mb-0">See what needs attention, review recent activity, and open the right dashboard faster.</p>
        </div>
        <div class="col-lg-5">
            <p class="section-label mb-3">Workspace Overview</p>
            <div class="row g-3">
                <div class="col-6"><div class="metric p-3"><div class="text-muted small">Total Ecosystems</div><div class="metric-value fw-semibold">${workspaceOverview.totalEcosystems}</div></div></div>
                <div class="col-6"><div class="metric p-3"><div class="text-muted small">Needs Attention</div><div class="metric-value fw-semibold">${workspaceOverview.needsAttention}</div></div></div>
                <div class="col-6"><div class="metric p-3"><div class="text-muted small">Overdue Tasks</div><div class="metric-value fw-semibold">${workspaceOverview.overdueTasks}</div></div></div>
                <div class="col-6"><div class="metric p-3"><div class="text-muted small">No Recent Data</div><div class="metric-value fw-semibold">${workspaceOverview.noRecentData}</div></div></div>
            </div>
        </div>
    </div>
</section>

<section class="panel p-4 mb-4">
    <div class="d-flex justify-content-between align-items-center mb-3 gap-3 flex-wrap">
        <div>
            <p class="section-label mb-1">Priority</p>
            <h3 class="h5 mb-0">Needs Attention First</h3>
        </div>
        <div class="text-muted small">The most urgent ecosystems surface here automatically.</div>
    </div>
    <div class="row g-3">
        <#if priorityCards?size == 0>
            <div class="col-12"><div class="border rounded-3 p-4 text-center text-muted">Nothing urgent right now. Your workspace looks calm.</div></div>
        <#else>
            <#list priorityCards as card>
                <div class="col-lg-6 priority-card" data-ecosystem-id="${card.id}">
                    <div class="eco-card p-3 p-lg-4">
                        <div class="d-flex justify-content-between align-items-start gap-3 mb-3">
                            <div>
                                <div class="d-flex align-items-center gap-2 flex-wrap mb-2">
                                    <span class="${ui.statusBadgeClass(card.status)}">${ui.statusLabel(card.status)}</span>
                                </div>
                                <h4 class="h5 mb-1">${card.name}</h4>
                                <div class="text-muted small">${ui.ecosystemTypeLabel(card.type)}</div>
                            </div>
                            <a href="/ecosystems/${card.id}" class="btn btn-outline-success btn-sm">Open</a>
                        </div>
                        <p class="text-muted small mb-3">${card.description!"No description provided yet."}</p>
                        <div class="freshness-note ${ui.freshnessClass(card.lastRecordedAt)}">${ui.freshnessLabel(card.lastRecordedAt)}</div>
                    </div>
                </div>
            </#list>
        </#if>
    </div>
</section>

<section class="panel p-4">
    <div class="d-flex justify-content-between align-items-center mb-3 gap-3 flex-wrap">
        <div>
            <p class="section-label mb-1">Workspace</p>
            <h3 class="h5 mb-0">My Ecosystems</h3>
        </div>
        <div class="text-muted small">
            <#if workspacePage.totalElements == 0>
                No ecosystems found
            <#else>
                Showing ${workspacePage.items?size} of ${workspacePage.totalElements} ecosystems
            </#if>
        </div>
    </div>

    <div class="row g-3">
        <#if workspacePage.items?size == 0>
            <div class="col-12">
                <div class="border rounded-3 p-4 text-center">
                    <h6 class="mb-2">No ecosystems match these filters</h6>
                    <p class="text-muted mb-0">Try a different search or create a new ecosystem above.</p>
                </div>
            </div>
        <#else>
            <#list workspacePage.items as card>
                <div class="col-md-6 col-xl-4 workspace-card" data-ecosystem-id="${card.id}">
                    <div class="eco-card p-3 p-lg-4">
                        <div class="d-flex justify-content-between align-items-start gap-3 mb-3">
                            <div>
                                <div class="d-flex align-items-center gap-2 flex-wrap mb-2">
                                    <span class="${ui.statusBadgeClass(card.status)}">${ui.statusLabel(card.status)}</span>
                                    <span class="badge text-bg-light border pin-badge d-none">Pinned</span>
                                </div>
                                <h4 class="h5 mb-1">${card.name}</h4>
                                <div class="text-muted small">${ui.ecosystemTypeLabel(card.type)}</div>
                            </div>
                            <button type="button" class="btn btn-outline-secondary btn-sm pin-toggle" data-pin-toggle data-ecosystem-id="${card.id}">Pin</button>
                        </div>
                        <p class="text-muted small mb-3">${card.description!"No description provided yet."}</p>
                        <div class="freshness-note mb-3 ${ui.freshnessClass(card.lastRecordedAt)}">${ui.freshnessLabel(card.lastRecordedAt)}</div>
                        <div class="mini-metrics mb-3">
                            <div class="mini-metric"><div class="mini-label">Last activity</div><div class="mini-value">${ui.formatDateTime(card.lastRecordedAt)}</div></div>
                            <div class="mini-metric"><div class="mini-label">Logs last 7 days</div><div class="mini-value">${card.logsLast7Days}</div></div>
                            <div class="mini-metric"><div class="mini-label">Open tasks</div><div class="mini-value">${card.openTasks}</div></div>
                            <div class="mini-metric"><div class="mini-label">Overdue tasks</div><div class="mini-value">${card.overdueTasks}</div></div>
                        </div>
                        <div class="d-flex gap-2 flex-wrap">
                            <a href="/ecosystems/${card.id}" class="btn btn-outline-success flex-grow-1">Open Dashboard</a>
                            <button type="button" class="btn btn-outline-secondary" data-quick-toggle="log" data-ecosystem-id="${card.id}">Quick Log</button>
                            <button type="button" class="btn btn-outline-secondary" data-quick-toggle="task" data-ecosystem-id="${card.id}">Quick Task</button>
                        </div>
                        <div class="quick-action-panel mt-3 d-none" data-quick-panel="${card.id}">
                            <div class="border rounded-3 p-3 bg-light-subtle">
                                <div class="d-flex justify-content-between align-items-start gap-2 flex-wrap mb-3">
                                    <div>
                                        <div class="fw-semibold quick-panel-title">Quick Log</div>
                                        <div class="text-muted small">Create a lightweight update without leaving the workspace.</div>
                                    </div>
                                    <button type="button" class="btn btn-outline-secondary btn-sm" data-quick-close="${card.id}">Cancel</button>
                                </div>
                                <div class="quick-form-feedback mb-3" data-quick-feedback="${card.id}"></div>

                                <form class="quick-log-form d-none"
                                      data-quick-form="log"
                                      hx-post="/ui/ecosystems/${card.id}/logs"
                                      hx-target='[data-quick-feedback="${card.id}"]'
                                      hx-swap="innerHTML">
                                    <div class="row g-2">
                                        <div class="col-md-4">
                                            <select name="eventType" class="form-select" required>
                                                <#list ui.logEventTypes() as option>
                                                    <option value="${option}">${ui.eventTypeLabel(option)}</option>
                                                </#list>
                                            </select>
                                        </div>
                                        <div class="col-md-4"><input type="number" step="0.1" name="temperatureC" class="form-control" placeholder="Temp C"></div>
                                        <div class="col-md-4"><input type="number" name="humidityPercent" class="form-control" placeholder="Humidity %"></div>
                                        <div class="col-12"><input type="text" name="notes" maxlength="500" class="form-control" placeholder="Notes"></div>
                                        <div class="col-12 d-flex justify-content-end gap-2">
                                            <button type="button" class="btn btn-outline-secondary btn-sm" data-quick-close="${card.id}">Cancel</button>
                                            <button type="submit" class="btn btn-success btn-sm">Save Log</button>
                                        </div>
                                    </div>
                                </form>

                                <form class="quick-task-form d-none"
                                      data-quick-form="task"
                                      hx-post="/ui/ecosystems/${card.id}/tasks"
                                      hx-target='[data-quick-feedback="${card.id}"]'
                                      hx-swap="innerHTML">
                                    <div class="row g-2">
                                        <div class="col-12"><input type="text" name="title" maxlength="120" class="form-control" placeholder="Task title" required></div>
                                        <div class="col-md-6">
                                            <select name="taskType" class="form-select" required>
                                                <#list ui.taskTypes() as option>
                                                    <option value="${option}">${ui.taskTypeLabel(option)}</option>
                                                </#list>
                                            </select>
                                        </div>
                                        <div class="col-md-6"><input type="date" name="dueDate" class="form-control"></div>
                                        <div class="col-12 d-flex justify-content-end gap-2">
                                            <button type="button" class="btn btn-outline-secondary btn-sm" data-quick-close="${card.id}">Cancel</button>
                                            <button type="submit" class="btn btn-success btn-sm">Create Task</button>
                                        </div>
                                    </div>
                                </form>
                            </div>
                        </div>
                    </div>
                </div>
            </#list>
        </#if>
    </div>

    <#if workspacePage.totalPages gt 1>
        <div class="d-flex justify-content-between align-items-center mt-4 gap-3 flex-wrap">
            <button class="btn btn-outline-secondary" <#if !workspacePage.hasPrevious>disabled</#if>
                    hx-get="/ui/workspace?search=${search?url}&status=${status?url}&sort=${sort?url}&page=${workspacePage.page - 1}"
                    hx-target="#workspace-panel" hx-swap="innerHTML">Previous</button>
            <span class="text-muted small">Page ${workspacePage.page + 1} of ${workspacePage.totalPages}</span>
            <button class="btn btn-outline-success" <#if !workspacePage.hasNext>disabled</#if>
                    hx-get="/ui/workspace?search=${search?url}&status=${status?url}&sort=${sort?url}&page=${workspacePage.page + 1}"
                    hx-target="#workspace-panel" hx-swap="innerHTML">Next</button>
        </div>
    </#if>
</section>
