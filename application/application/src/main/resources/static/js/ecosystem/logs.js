window.EcoTrackerLogsModule = (() => {
    const state = window.EcoTrackerEcosystemState;
    const { getJson, sendJson } = window.EcoTrackerApi;
    const {
        escapeHtml,
        parseEncodedPayload,
        parseNullableNumber,
        parseNullableInteger,
        valueToText
    } = window.EcoTrackerUtils;

    async function loadLogs() {
        const container = document.getElementById('logs-container');
        const logsMeta = document.getElementById('logs-meta');
        const pageIndicator = document.getElementById('logs-page-indicator');
        const prevButton = document.getElementById('logs-prev-button');
        const nextButton = document.getElementById('logs-next-button');
        const eventType = document.getElementById('log-filter-event').value;

        container.innerHTML = '<div class="text-muted">Loading activity logs...</div>';
        logsMeta.textContent = 'Loading activity logs...';
        pageIndicator.textContent = `Page ${state.currentLogPage + 1}`;
        prevButton.disabled = true;
        nextButton.disabled = true;

        try {
            const params = new URLSearchParams({
                page: String(state.currentLogPage),
                size: String(state.logPageSize)
            });
            if (eventType) params.set('eventType', eventType);

            const result = await getJson(`/api/v1/ecosystems/${state.ecosystemId}/logs?${params.toString()}`, 'Failed to load activity logs.');
            const logs = result.items;

            if (logs.length === 0) {
                container.innerHTML = '<div class="log-card text-center text-muted py-4">No logs match the current filter yet.</div>';
                logsMeta.textContent = '0 matching logs';
                pageIndicator.textContent = `Page ${result.page + 1}`;
                return;
            }

            container.innerHTML = logs.map((log) => `
                <div class="log-card">
                    <div class="d-flex w-100 justify-content-between">
                        <div class="d-flex align-items-center gap-2">
                            <span class="log-event-pill ${resolveLogEventClass(log.eventType)}">${escapeHtml(formatLogEventType(log.eventType))}</span>
                        </div>
                        <small class="text-muted">${new Date(log.recordedAt).toLocaleString()}</small>
                    </div>
                    <div class="log-meta-row mt-2 mb-2">
                        <span>Temp: ${log.temperatureC ?? '--'} C</span>
                        <span>Humidity: ${log.humidityPercent ?? '--'}%</span>
                    </div>
                    <small class="text-muted d-block mb-1">Created by: ${escapeHtml(log.createdByDisplayName || log.createdByUsername || 'Legacy record')}</small>
                    <small class="text-muted d-block mb-2">${escapeHtml(log.notes || 'No notes')}</small>
                    <button class="btn btn-outline-secondary btn-sm" data-action="edit-log" data-log='${encodeURIComponent(JSON.stringify(log))}'>Edit</button>
                </div>
            `).join('');

            const startItem = result.totalElements === 0 ? 0 : result.page * result.size + 1;
            const endItem = Math.min(result.totalElements, startItem + logs.length - 1);
            logsMeta.textContent = `Showing ${startItem}-${endItem} of ${result.totalElements} logs`;
            pageIndicator.textContent = `Page ${result.page + 1} of ${Math.max(result.totalPages, 1)}`;
            prevButton.disabled = !result.hasPrevious;
            nextButton.disabled = !result.hasNext;
        } catch (error) {
            container.innerHTML = '<div class="log-card text-center text-danger-emphasis bg-danger-subtle py-4">Could not load activity logs.</div>';
            logsMeta.textContent = 'Failed to load logs';
            window.EcoTrackerEcosystemPage.showAlert(error.message || 'Failed to load logs.', 'danger', 'Logs failed');
        }
    }

    async function submitLog(event) {
        event.preventDefault();
        const submitButton = event.submitter;
        submitButton.disabled = true;

        try {
            await sendJson(`/api/v1/ecosystems/${state.ecosystemId}/logs`, 'POST', {
                temperatureC: document.getElementById('log-temp').value || null,
                humidityPercent: document.getElementById('log-humidity').value || null,
                eventType: document.getElementById('log-event').value,
                notes: document.getElementById('log-notes').value || null
            }, 'Failed to add log entry.');

            document.getElementById('add-log-form').reset();
            state.currentLogPage = 0;
            window.EcoTrackerEcosystemPage.showAlert('Log entry added successfully.', 'success', 'Log created');
            await window.EcoTrackerSummaryModule.loadSummary();
            await loadLogs();
        } catch (error) {
            window.EcoTrackerEcosystemPage.showAlert(error.message || 'Failed to add log entry.', 'danger', 'Log creation failed');
        } finally {
            submitButton.disabled = false;
        }
    }

    async function submitLogEdit(event) {
        event.preventDefault();
        if (!state.editingLogId) return;

        const validationError = window.EcoTrackerEcosystemPage.validateLogForm(
            document.getElementById('edit-log-temp').value,
            document.getElementById('edit-log-humidity').value,
            document.getElementById('edit-log-event').value,
            document.getElementById('edit-log-notes').value
        );
        if (validationError) {
            window.EcoTrackerEcosystemPage.showFormError('edit-log-error', validationError);
            return;
        }

        const submitButton = event.submitter;
        submitButton.disabled = true;
        window.EcoTrackerEcosystemPage.clearFormError('edit-log-error');

        try {
            await sendJson(`/api/v1/ecosystems/${state.ecosystemId}/logs/${state.editingLogId}`, 'PATCH', {
                temperatureC: parseNullableNumber(document.getElementById('edit-log-temp').value),
                humidityPercent: parseNullableInteger(document.getElementById('edit-log-humidity').value),
                eventType: document.getElementById('edit-log-event').value,
                notes: document.getElementById('edit-log-notes').value || null
            }, 'Failed to update log entry.');

            cancelLogEdit();
            window.EcoTrackerEcosystemPage.showAlert('Log entry updated successfully.', 'success', 'Log saved');
            await window.EcoTrackerSummaryModule.loadSummary();
            await loadLogs();
        } catch (error) {
            window.EcoTrackerEcosystemPage.showFormError('edit-log-error', error.message || 'Failed to update log entry.');
        } finally {
            submitButton.disabled = false;
        }
    }

    function editLog(logData) {
        const log = parseEncodedPayload(logData);
        state.editingLogId = log.id;
        window.EcoTrackerEcosystemPage.clearFormError('edit-log-error');
        document.getElementById('edit-log-temp').value = valueToText(log.temperatureC);
        document.getElementById('edit-log-humidity').value = valueToText(log.humidityPercent);
        document.getElementById('edit-log-event').value = log.eventType || 'OBSERVATION';
        document.getElementById('edit-log-notes').value = log.notes || '';
        document.getElementById('log-edit-panel').classList.remove('d-none');
        document.getElementById('log-edit-panel').scrollIntoView({ behavior: 'smooth', block: 'nearest' });
    }

    function applyLogFilters() {
        state.currentLogPage = 0;
        loadLogs();
    }

    function changeLogPage(delta) {
        state.currentLogPage = Math.max(0, state.currentLogPage + delta);
        loadLogs();
    }

    function cancelLogEdit() {
        state.editingLogId = null;
        document.getElementById('edit-log-form').reset();
        window.EcoTrackerEcosystemPage.clearFormError('edit-log-error');
        document.getElementById('log-edit-panel').classList.add('d-none');
    }

    function formatLogEventType(eventType) {
        const labels = { OBSERVATION: 'Observation', FEEDING: 'Feeding', WATERING: 'Watering' };
        return labels[eventType] || eventType;
    }

    function resolveLogEventClass(eventType) {
        const classes = { OBSERVATION: 'log-event-observation', FEEDING: 'log-event-feeding', WATERING: 'log-event-watering' };
        return classes[eventType] || 'log-event-observation';
    }

    return {
        loadLogs,
        submitLog,
        submitLogEdit,
        editLog,
        applyLogFilters,
        changeLogPage,
        cancelLogEdit
    };
})();
