window.EcoTrackerTasksModule = (() => {
    const state = window.EcoTrackerEcosystemState;
    const { getJson, sendJson } = window.EcoTrackerApi;
    const { escapeHtml, parseEncodedPayload } = window.EcoTrackerUtils;

    async function loadTasks() {
        const container = document.getElementById('tasks-container');
        const tasksMeta = document.getElementById('tasks-meta');
        const filter = document.querySelector('input[name="task-filter"]:checked').value;
        const sourceFilter = document.getElementById('task-source-filter').value;
        setTaskListLoading(true);
        container.innerHTML = '<div class="text-muted">Loading maintenance tasks...</div>';
        tasksMeta.textContent = 'Loading maintenance tasks...';

        try {
            const [tasks, allTasks] = await Promise.all([
                getJson(`/api/v1/ecosystems/${state.ecosystemId}/tasks?filter=${encodeURIComponent(filter)}`, 'Failed to load maintenance tasks.'),
                filter === 'ALL'
                    ? Promise.resolve(null)
                    : getJson(`/api/v1/ecosystems/${state.ecosystemId}/tasks?filter=ALL`, 'Failed to load maintenance task counters.')
            ]);

            const fullTaskList = allTasks || tasks;
            updateTaskFilterCounts(fullTaskList, sourceFilter);
            const visibleTasks = tasks.filter((task) => matchesTaskSourceFilter(task, sourceFilter));

            if (visibleTasks.length === 0) {
                tasksMeta.textContent = tasks.length === 0 ? '0 tasks returned for the current status filter' : `Showing 0 of ${tasks.length} tasks`;
                container.innerHTML = '<div class="border rounded-3 p-4 text-center bg-light-subtle text-muted task-card">No maintenance tasks match the current filter.</div>';
                return;
            }

            tasksMeta.textContent = visibleTasks.length === tasks.length
                ? `Showing all ${visibleTasks.length} tasks`
                : `Showing ${visibleTasks.length} of ${tasks.length} tasks`;

            container.innerHTML = visibleTasks.map((task) => {
                const statusBadge = resolveTaskStatusBadge(task.status);
                const sourceBadge = task.autoCreated ? '<span class="status-pill status-pill-suggested">Suggested</span>' : '';
                const dueDate = task.dueDate ? new Date(task.dueDate).toLocaleDateString() : 'No due date';
                const dismissalReason = task.dismissalReason ? ` | Reason: ${formatDismissalReason(task.dismissalReason)}` : '';

                return `
                    <div class="border rounded-3 p-3 mb-2 task-card">
                        <div class="task-card-layout">
                            <strong class="task-title">${escapeHtml(task.title)}</strong>
                            <div class="task-detail-row">
                                <div class="task-meta-group">
                                    <div class="task-badge-row">
                                        ${statusBadge}
                                        ${sourceBadge}
                                    </div>
                                    <p class="small text-muted task-detail-text">${escapeHtml(formatTaskType(task.taskType))} | Due: ${dueDate}${escapeHtml(dismissalReason)}</p>
                                </div>
                                <div class="task-action-group">
                                    ${buildTaskActions(task)}
                                </div>
                            </div>
                        </div>
                    </div>
                `;
            }).join('');
        } catch (error) {
            resetTaskFilterCounts();
            tasksMeta.textContent = 'Failed to load tasks';
            container.innerHTML = '<div class="border rounded-3 p-3 bg-danger-subtle text-danger-emphasis task-card">Could not load maintenance tasks.</div>';
            window.EcoTrackerEcosystemPage.showAlert(error.message || 'Failed to load maintenance tasks.', 'danger', 'Tasks failed');
        } finally {
            setTaskListLoading(false);
        }
    }

    async function submitTask(event) {
        event.preventDefault();
        const submitButton = event.submitter;
        submitButton.disabled = true;

        try {
            await sendJson(`/api/v1/ecosystems/${state.ecosystemId}/tasks`, 'POST', {
                title: document.getElementById('task-title').value,
                taskType: document.getElementById('task-type').value,
                dueDate: document.getElementById('task-due-date').value || null
            }, 'Failed to create maintenance task.');

            document.getElementById('add-task-form').reset();
            window.EcoTrackerEcosystemPage.showAlert('Maintenance task created successfully.', 'success', 'Task created');
            await window.EcoTrackerSummaryModule.loadSummary();
            await loadTasks();
        } catch (error) {
            window.EcoTrackerEcosystemPage.showAlert(error.message || 'Failed to create maintenance task.', 'danger', 'Task creation failed');
        } finally {
            submitButton.disabled = false;
        }
    }

    async function submitTaskEdit(event) {
        event.preventDefault();
        if (!state.editingTaskId) return;

        const validationError = window.EcoTrackerEcosystemPage.validateTaskForm(
            document.getElementById('edit-task-title').value,
            document.getElementById('edit-task-due-date').value
        );
        if (validationError) {
            window.EcoTrackerEcosystemPage.showFormError('edit-task-error', validationError);
            return;
        }

        const submitButton = event.submitter;
        submitButton.disabled = true;
        window.EcoTrackerEcosystemPage.clearFormError('edit-task-error');

        try {
            await sendJson(`/api/v1/ecosystems/${state.ecosystemId}/tasks/${state.editingTaskId}`, 'PATCH', {
                title: document.getElementById('edit-task-title').value,
                taskType: document.getElementById('edit-task-type').value,
                dueDate: document.getElementById('edit-task-due-date').value || null
            }, 'Failed to update maintenance task.');

            cancelTaskEdit();
            window.EcoTrackerEcosystemPage.showAlert('Maintenance task updated successfully.', 'success', 'Task saved');
            await window.EcoTrackerSummaryModule.loadSummary();
            await loadTasks();
        } catch (error) {
            window.EcoTrackerEcosystemPage.showFormError('edit-task-error', error.message || 'Failed to update maintenance task.');
        } finally {
            submitButton.disabled = false;
        }
    }

    async function updateTaskStatus(taskId, status, dismissalReason = null) {
        try {
            await sendJson(`/api/v1/ecosystems/${state.ecosystemId}/tasks/${taskId}/status`, 'PATCH', { status, dismissalReason }, 'Failed to update maintenance task.');
            window.EcoTrackerEcosystemPage.showAlert('Maintenance task updated successfully.', 'success', 'Task updated');
            await window.EcoTrackerSummaryModule.loadSummary();
            await loadTasks();
        } catch (error) {
            window.EcoTrackerEcosystemPage.showAlert(error.message || 'Failed to update maintenance task.', 'danger', 'Task update failed');
        }
    }

    function editTask(taskData) {
        const task = parseEncodedPayload(taskData);
        state.editingTaskId = task.id;
        window.EcoTrackerEcosystemPage.clearFormError('edit-task-error');
        document.getElementById('edit-task-title').value = task.title || '';
        document.getElementById('edit-task-type').value = task.taskType || 'WATERING';
        document.getElementById('edit-task-due-date').value = task.dueDate || '';
        document.getElementById('task-edit-panel').classList.remove('d-none');
        document.getElementById('task-edit-panel').scrollIntoView({ behavior: 'smooth', block: 'nearest' });
    }

    function dismissSuggestedTask(taskId, selectId) {
        const dismissalReason = document.getElementById(selectId)?.value;
        if (!dismissalReason) {
            window.EcoTrackerEcosystemPage.showAlert('Choose a dismissal reason so the system knows when to suggest this task again.', 'warning', 'Dismiss reason required');
            return;
        }

        updateTaskStatus(taskId, 'DISMISSED', dismissalReason);
    }

    function cancelTaskEdit() {
        state.editingTaskId = null;
        document.getElementById('edit-task-form').reset();
        window.EcoTrackerEcosystemPage.clearFormError('edit-task-error');
        document.getElementById('task-edit-panel').classList.add('d-none');
    }

    function formatTaskType(taskType) {
        const labels = { WATERING: 'Watering', FEEDING: 'Feeding', CLEANING: 'Cleaning', INSPECTION: 'Inspection' };
        return labels[taskType] || taskType;
    }

    function resolveTaskStatusBadge(status) {
        if (status === 'DONE') return '<span class="status-pill status-pill-done">Done</span>';
        if (status === 'DISMISSED') return '<span class="status-pill status-pill-dismissed">Dismissed</span>';
        return '<span class="status-pill status-pill-open">Open</span>';
    }

    function buildTaskActions(task) {
        if (task.status === 'OPEN' && task.autoCreated) {
            const reasonSelectId = `dismiss-reason-${task.id}`;
            return `
                <div class="task-actions-panel">
                    <select id="${reasonSelectId}" class="form-select form-select-sm task-action-select" aria-label="Dismissal reason" title="This reason controls when the suggestion can appear again.">
                        <option value="" selected disabled>Dismiss reason</option>
                        <option value="TOO_SOON">Too soon</option>
                        <option value="NOT_RELEVANT">Not relevant</option>
                        <option value="ALREADY_HANDLED">Already handled</option>
                    </select>
                    <div class="task-actions-row">
                        <button class="btn btn-outline-success btn-sm" data-action="task-status" data-task-id="${task.id}" data-status="DONE">Done</button>
                        <button class="btn btn-outline-secondary btn-sm" data-action="dismiss-suggested-task" data-task-id="${task.id}" data-select-id="${reasonSelectId}">Dismiss</button>
                    </div>
                </div>
            `;
        }

        if (task.status === 'OPEN') {
            return `
                <div class="task-actions-row">
                    <button class="btn btn-outline-secondary btn-sm" data-action="edit-task" data-task='${encodeURIComponent(JSON.stringify(task))}'>Edit</button>
                    <button class="btn btn-outline-secondary btn-sm" data-action="task-status" data-task-id="${task.id}" data-status="DONE">Mark Done</button>
                </div>
            `;
        }

        if (task.status === 'DISMISSED') return `<div class="task-actions-row"><button class="btn btn-outline-secondary btn-sm" data-action="task-status" data-task-id="${task.id}" data-status="OPEN">Restore</button></div>`;
        if (task.autoCreated) return `<div class="task-actions-row"><button class="btn btn-outline-secondary btn-sm" data-action="task-status" data-task-id="${task.id}" data-status="OPEN">Reopen</button></div>`;

        return `
            <div class="task-actions-row">
                <button class="btn btn-outline-secondary btn-sm" data-action="edit-task" data-task='${encodeURIComponent(JSON.stringify(task))}'>Edit</button>
                <button class="btn btn-outline-secondary btn-sm" data-action="task-status" data-task-id="${task.id}" data-status="OPEN">Reopen</button>
            </div>
        `;
    }

    function formatDismissalReason(reason) {
        const labels = { TOO_SOON: 'Too soon', NOT_RELEVANT: 'Not relevant', ALREADY_HANDLED: 'Already handled' };
        return labels[reason] || reason;
    }

    function matchesTaskSourceFilter(task, sourceFilter) {
        if (sourceFilter === 'MANUAL') return !task.autoCreated;
        if (sourceFilter === 'SUGGESTED') return task.autoCreated;
        return true;
    }

    function updateTaskFilterCounts(allTasks, sourceFilter) {
        const visibleTasks = allTasks.filter((task) => matchesTaskSourceFilter(task, sourceFilter));
        const today = new Date();
        today.setHours(0, 0, 0, 0);

        const counts = {
            ALL: visibleTasks.length,
            OPEN: visibleTasks.filter((task) => task.status === 'OPEN').length,
            DONE: visibleTasks.filter((task) => task.status === 'DONE').length,
            DISMISSED: visibleTasks.filter((task) => task.status === 'DISMISSED').length,
            OVERDUE: visibleTasks.filter((task) => {
                if (task.status !== 'OPEN' || !task.dueDate) return false;
                const dueDate = new Date(task.dueDate);
                dueDate.setHours(0, 0, 0, 0);
                return dueDate < today;
            }).length
        };

        document.getElementById('task-filter-all-label').textContent = `All (${counts.ALL})`;
        document.getElementById('task-filter-open-label').textContent = `Open (${counts.OPEN})`;
        document.getElementById('task-filter-done-label').textContent = `Done (${counts.DONE})`;
        document.getElementById('task-filter-dismissed-label').textContent = `Dismissed (${counts.DISMISSED})`;
        document.getElementById('task-filter-overdue-label').textContent = `Overdue (${counts.OVERDUE})`;
    }

    function resetTaskFilterCounts() {
        document.getElementById('task-filter-all-label').textContent = 'All';
        document.getElementById('task-filter-open-label').textContent = 'Open';
        document.getElementById('task-filter-done-label').textContent = 'Done';
        document.getElementById('task-filter-dismissed-label').textContent = 'Dismissed';
        document.getElementById('task-filter-overdue-label').textContent = 'Overdue';
    }

    function setTaskListLoading(isLoading) {
        document.getElementById('tasks-container').classList.toggle('is-loading', isLoading);
    }

    return {
        loadTasks,
        submitTask,
        submitTaskEdit,
        updateTaskStatus,
        editTask,
        dismissSuggestedTask,
        cancelTaskEdit
    };
})();
