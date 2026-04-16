(function () {
    const state = window.EcoTrackerEcosystemState;
    const { getJson, sendJson, deleteRequest, loadAuthStatus } = window.EcoTrackerApi;
    const { escapeHtml } = window.EcoTrackerUtils;

    window.EcoTrackerEcosystemPage = {
        showAlert,
        hideAlert,
        showFormError,
        clearFormError,
        validateEcosystemForm,
        validateTaskForm,
        validateLogForm,
        redirectToHome
    };

    window.addEventListener('load', async () => {
        bindEvents();
        if (!state.ecosystemId) {
            showAlert('No ecosystem ID provided in URL. Redirecting to the home page.', 'warning', 'Missing identifier');
            redirectToHome();
            return;
        }

        await loadAuthStatus();
        await loadDetails();
        await Promise.all([
            window.EcoTrackerSummaryModule.loadSummary(),
            window.EcoTrackerTasksModule.loadTasks(),
            window.EcoTrackerLogsModule.loadLogs()
        ]);
    });

    async function loadDetails() {
        try {
            const eco = await getJson(`/api/v1/ecosystems/${state.ecosystemId}`, 'Ecosystem not found.');
            state.currentEcosystem = eco;
            document.getElementById('page-title').innerText = eco.name;
            document.getElementById('bc-name').innerText = eco.name;
            hideAlert();
        } catch (error) {
            showAlert(error.message || 'Could not load ecosystem details. Returning to the home page.', 'danger', 'Details unavailable');
            redirectToHome();
        }
    }

    function deleteEcosystem() {
        document.getElementById('delete-confirm-panel').classList.remove('d-none');
        document.getElementById('delete-confirm-panel').scrollIntoView({ behavior: 'smooth', block: 'nearest' });
    }

    function cancelDeleteEcosystem() {
        document.getElementById('delete-confirm-panel').classList.add('d-none');
    }

    async function confirmDeleteEcosystem() {
        try {
            await deleteRequest(`/api/v1/ecosystems/${state.ecosystemId}`, 'Failed to delete ecosystem.');
            showAlert('Ecosystem deleted successfully. Redirecting to the home page.', 'success', 'Ecosystem deleted');
            redirectToHome();
        } catch (error) {
            showAlert(error.message || 'Failed to delete ecosystem.', 'danger', 'Delete failed');
        }
    }

    async function submitEcosystemEdit(event) {
        event.preventDefault();
        const validationError = validateEcosystemForm(
            document.getElementById('edit-eco-name').value,
            document.getElementById('edit-eco-desc').value
        );
        if (validationError) {
            showFormError('edit-eco-error', validationError);
            return;
        }

        const submitButton = event.submitter;
        submitButton.disabled = true;
        clearFormError('edit-eco-error');

        try {
            await sendJson(`/api/v1/ecosystems/${state.ecosystemId}`, 'PATCH', {
                name: document.getElementById('edit-eco-name').value,
                type: document.getElementById('edit-eco-type').value,
                description: document.getElementById('edit-eco-desc').value || null
            }, 'Failed to update ecosystem.');

            cancelEcosystemEdit();
            showAlert('Ecosystem updated successfully.', 'success', 'Ecosystem saved');
            await loadDetails();
            await window.EcoTrackerSummaryModule.loadSummary();
        } catch (error) {
            showFormError('edit-eco-error', error.message || 'Failed to update ecosystem.');
        } finally {
            submitButton.disabled = false;
        }
    }

    function editEcosystem() {
        if (!state.currentEcosystem) {
            showAlert('Ecosystem details are still loading.', 'warning', 'Please wait');
            return;
        }

        clearFormError('edit-eco-error');
        document.getElementById('edit-eco-name').value = state.currentEcosystem.name || '';
        document.getElementById('edit-eco-type').value = state.currentEcosystem.type || 'FORMICARIUM';
        document.getElementById('edit-eco-desc').value = state.currentEcosystem.description || '';
        document.getElementById('ecosystem-edit-panel').classList.remove('d-none');
        document.getElementById('ecosystem-edit-panel').scrollIntoView({ behavior: 'smooth', block: 'nearest' });
    }

    function cancelEcosystemEdit() {
        document.getElementById('edit-ecosystem-form').reset();
        clearFormError('edit-eco-error');
        document.getElementById('ecosystem-edit-panel').classList.add('d-none');
    }

    function redirectToHome(delayMs = 900) {
        window.setTimeout(() => {
            window.location.href = 'index.html';
        }, delayMs);
    }

    function showAlert(message, type, title = null) {
        const stack = document.getElementById('toast-stack');
        const toastId = `toast-${++state.toastCounter}`;
        const variant = normalizeToastType(type);
        const toast = document.createElement('div');
        toast.id = toastId;
        toast.className = `toast-card toast-card-${variant}`;
        toast.setAttribute('role', 'status');
        toast.innerHTML = `
            <div class="toast-accent toast-accent-${variant}"></div>
            <div class="p-3">
                <div class="d-flex justify-content-between align-items-start gap-3">
                    <div>
                        <div class="fw-semibold">${escapeHtml(title || toastTitleForType(variant))}</div>
                        <div class="small text-muted mt-1">${escapeHtml(message)}</div>
                    </div>
                    <button type="button" class="toast-close" aria-label="Close notification" data-action="dismiss-toast" data-toast-id="${toastId}">x</button>
                </div>
            </div>
        `;

        stack.appendChild(toast);
        if (variant !== 'danger') window.setTimeout(() => dismissToast(toastId), 3200);
    }

    function hideAlert() {
        document.getElementById('toast-stack').innerHTML = '';
    }

    function dismissToast(toastId) {
        const toast = document.getElementById(toastId);
        if (!toast) return;
        toast.classList.add('is-leaving');
        window.setTimeout(() => toast.remove(), 180);
    }

    function normalizeToastType(type) {
        return type === 'success' || type === 'danger' || type === 'warning' ? type : 'info';
    }

    function toastTitleForType(type) {
        if (type === 'success') return 'Saved';
        if (type === 'danger') return 'Something went wrong';
        if (type === 'warning') return 'Heads up';
        return 'Update';
    }

    function showFormError(elementId, message) {
        const error = document.getElementById(elementId);
        if (!error) return;
        error.textContent = message;
        error.classList.remove('d-none');
    }

    function clearFormError(elementId) {
        const error = document.getElementById(elementId);
        if (!error) return;
        error.textContent = '';
        error.classList.add('d-none');
    }

    function validateEcosystemForm(name, description) {
        const trimmedName = name.trim();
        if (!trimmedName) return 'Name is required.';
        if (trimmedName.length > 100) return 'Name must be 100 characters or fewer.';
        if (description.trim().length > 500) return 'Description must be 500 characters or fewer.';
        return null;
    }

    function validateTaskForm(title, dueDate) {
        const trimmedTitle = title.trim();
        if (!trimmedTitle) return 'Task title is required.';
        if (trimmedTitle.length > 120) return 'Task title must be 120 characters or fewer.';
        if (dueDate && Number.isNaN(Date.parse(dueDate))) return 'Due date must be a valid date.';
        return null;
    }

    function validateLogForm(temperature, humidity, eventType, notes) {
        if (!eventType.trim()) return 'Event type is required.';

        if (temperature.trim()) {
            const temperatureValue = Number(temperature);
            if (Number.isNaN(temperatureValue) || temperatureValue < -100 || temperatureValue > 100) {
                return 'Temperature must be between -100 and 100.';
            }
        }

        if (humidity.trim()) {
            const humidityValue = Number(humidity);
            if (!Number.isInteger(humidityValue) || humidityValue < 0 || humidityValue > 100) {
                return 'Humidity must be a whole number between 0 and 100.';
            }
        }

        if (notes.trim().length > 500) return 'Notes must be 500 characters or fewer.';
        return null;
    }

    function bindEvents() {
        document.getElementById('edit-ecosystem-button').addEventListener('click', editEcosystem);
        document.getElementById('delete-ecosystem-button').addEventListener('click', deleteEcosystem);
        document.getElementById('cancel-ecosystem-edit-top').addEventListener('click', cancelEcosystemEdit);
        document.getElementById('cancel-ecosystem-edit-bottom').addEventListener('click', cancelEcosystemEdit);
        document.getElementById('edit-ecosystem-form').addEventListener('submit', submitEcosystemEdit);
        document.getElementById('cancel-delete-ecosystem').addEventListener('click', cancelDeleteEcosystem);
        document.getElementById('confirm-delete-ecosystem').addEventListener('click', confirmDeleteEcosystem);
        document.getElementById('add-task-form').addEventListener('submit', window.EcoTrackerTasksModule.submitTask);
        document.getElementById('edit-task-form').addEventListener('submit', window.EcoTrackerTasksModule.submitTaskEdit);
        document.getElementById('cancel-task-edit-top').addEventListener('click', window.EcoTrackerTasksModule.cancelTaskEdit);
        document.getElementById('cancel-task-edit-bottom').addEventListener('click', window.EcoTrackerTasksModule.cancelTaskEdit);
        document.querySelectorAll('input[name="task-filter"]').forEach((input) => input.addEventListener('change', window.EcoTrackerTasksModule.loadTasks));
        document.getElementById('task-source-filter').addEventListener('change', window.EcoTrackerTasksModule.loadTasks);
        document.getElementById('tasks-container').addEventListener('click', handleTaskContainerClick);
        document.getElementById('add-log-form').addEventListener('submit', window.EcoTrackerLogsModule.submitLog);
        document.getElementById('edit-log-form').addEventListener('submit', window.EcoTrackerLogsModule.submitLogEdit);
        document.getElementById('cancel-log-edit-top').addEventListener('click', window.EcoTrackerLogsModule.cancelLogEdit);
        document.getElementById('cancel-log-edit-bottom').addEventListener('click', window.EcoTrackerLogsModule.cancelLogEdit);
        document.getElementById('log-filter-event').addEventListener('change', window.EcoTrackerLogsModule.applyLogFilters);
        document.getElementById('logs-prev-button').addEventListener('click', () => window.EcoTrackerLogsModule.changeLogPage(-1));
        document.getElementById('logs-next-button').addEventListener('click', () => window.EcoTrackerLogsModule.changeLogPage(1));
        document.getElementById('logs-container').addEventListener('click', handleLogsContainerClick);
        document.getElementById('toast-stack').addEventListener('click', handleToastStackClick);
    }

    function handleTaskContainerClick(event) {
        const actionElement = event.target.closest('[data-action]');
        if (!actionElement) {
            return;
        }

        const { action } = actionElement.dataset;
        if (action === 'edit-task') {
            window.EcoTrackerTasksModule.editTask(actionElement.dataset.task);
        } else if (action === 'task-status') {
            window.EcoTrackerTasksModule.updateTaskStatus(actionElement.dataset.taskId, actionElement.dataset.status);
        } else if (action === 'dismiss-suggested-task') {
            window.EcoTrackerTasksModule.dismissSuggestedTask(actionElement.dataset.taskId, actionElement.dataset.selectId);
        }
    }

    function handleLogsContainerClick(event) {
        const editButton = event.target.closest('[data-action="edit-log"]');
        if (!editButton) {
            return;
        }

        window.EcoTrackerLogsModule.editLog(editButton.dataset.log);
    }

    function handleToastStackClick(event) {
        const dismissButton = event.target.closest('[data-action="dismiss-toast"]');
        if (!dismissButton) {
            return;
        }

        dismissToast(dismissButton.dataset.toastId);
    }
})();
