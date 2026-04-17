<div class="mb-3">
    <p class="text-muted small mb-0">
        <#if tasks?size == 0>
            No tasks match the current filters.
        <#else>
            ${tasks?size} task<#if tasks?size != 1>s</#if>, ${openTaskCount} open
        </#if>
    </p>
</div>

<div class="d-grid gap-3">
    <#if tasks?size == 0>
        <div class="text-muted">No maintenance tasks found.</div>
    <#else>
        <#list tasks as task>
            <div class="task-card border rounded-4 p-3">
                <div class="task-card-layout">
                    <div class="task-meta-group">
                        <div class="task-badge-row">
                            <span class="${ui.taskStatusBadgeClass(task.status)}">${ui.taskStatusLabel(task.status)}</span>
                            <span class="${ui.taskTypeBadgeClass(task.taskType)}">${ui.taskTypeLabel(task.taskType)}</span>
                            <#if task.autoCreated><span class="${ui.taskSourceBadgeClass(task.autoCreated)}">Suggested</span></#if>
                            <#if ui.isTaskOverdue(task)><span class="badge text-bg-danger">Overdue</span></#if>
                        </div>
                        <strong class="task-title">${task.title}</strong>
                        <p class="task-detail-text text-muted small mb-0">
                            Due ${ui.formatDate(task.dueDate)} | Created by ${ui.createdByLabel(task.createdByDisplayName, task.createdByUsername)}
                        </p>
                        <#if task.dismissalReason??>
                            <p class="task-detail-text text-muted small mb-0">Dismissed as: ${ui.dismissalReasonLabel(task.dismissalReason)}</p>
                        </#if>
                    </div>

                    <div class="task-actions-row">
                        <#if task.status != "DONE">
                            <button class="btn btn-outline-success btn-sm"
                                    hx-patch="/ui/ecosystems/${ecosystemId}/tasks/${task.id}/status"
                                    hx-vals='{"status":"DONE"}'
                                    hx-target="#task-form-feedback"
                                    hx-swap="innerHTML">Mark Done</button>
                        </#if>
                        <#if task.status != "OPEN">
                            <button class="btn btn-outline-secondary btn-sm"
                                    hx-patch="/ui/ecosystems/${ecosystemId}/tasks/${task.id}/status"
                                    hx-vals='{"status":"OPEN"}'
                                    hx-target="#task-form-feedback"
                                    hx-swap="innerHTML">Reopen</button>
                        </#if>
                        <#if task.autoCreated && task.status != "DISMISSED">
                            <button class="btn btn-outline-danger btn-sm"
                                    hx-patch="/ui/ecosystems/${ecosystemId}/tasks/${task.id}/status"
                                    hx-vals='{"status":"DISMISSED","dismissalReason":"ALREADY_HANDLED"}'
                                    hx-confirm="Dismiss this suggested task as already handled?"
                                    hx-target="#task-form-feedback"
                                    hx-swap="innerHTML">Dismiss</button>
                        </#if>
                    </div>

                    <#if ui.canEditTask(task)>
                        <details class="mt-2">
                            <summary class="small text-success">Edit task</summary>
                            <form class="row g-2 mt-2"
                                  hx-patch="/ui/ecosystems/${ecosystemId}/tasks/${task.id}"
                                  hx-target="#task-form-feedback"
                                  hx-swap="innerHTML">
                                <div class="col-md-5">
                                    <input type="text" name="title" class="form-control" maxlength="120" value="${task.title}" required>
                                </div>
                                <div class="col-md-4">
                                    <select name="taskType" class="form-select" required>
                                        <#list ui.taskTypes() as option>
                                            <option value="${option}" <#if option == task.taskType>selected</#if>>${ui.taskTypeLabel(option)}</option>
                                        </#list>
                                    </select>
                                </div>
                                <div class="col-md-3">
                                    <input type="date" name="dueDate" class="form-control" value="${task.dueDate!""}">
                                </div>
                                <div class="col-12 d-flex justify-content-end gap-2">
                                    <button type="button" class="btn btn-outline-secondary btn-sm" data-close-details>Cancel</button>
                                    <button type="submit" class="btn btn-outline-success btn-sm">Save Changes</button>
                                </div>
                            </form>
                        </details>
                    </#if>
                </div>
            </div>
        </#list>
    </#if>
</div>
