window.EcoTrackerSummaryModule = (() => {
    const state = window.EcoTrackerEcosystemState;
    const { getJson } = window.EcoTrackerApi;
    const { escapeHtml } = window.EcoTrackerUtils;

    async function loadSummary() {
        const container = document.getElementById('summary-container');
        setSummaryLoading(true);
        container.innerHTML = '<div class="col-12 text-muted">Loading summary...</div>';

        try {
            const summary = await getJson(`/api/v1/ecosystems/${state.ecosystemId}/summary`, 'Failed to load ecosystem summary.');
            const statusMeta = resolveStatusMeta(summary.status);
            const lastRecorded = summary.lastRecordedAt ? new Date(summary.lastRecordedAt).toLocaleString() : 'No data yet';
            const taskMessage = summary.overdueTasks > 0 ? `${summary.overdueTasks} overdue` : `${summary.openTasks} open`;
            const temperatureDelta = formatTrendDelta(summary.temperatureTrendDeltaC, 'C');
            const humidityDelta = formatTrendDelta(summary.humidityTrendDeltaPercent, '%');

            container.innerHTML = `
                <div class="col-md-3 summary-card">
                    <div class="summary-panel p-3 h-100 ${statusMeta.panelClass}">
                        <p class="summary-panel-muted small mb-2">Status</p>
                        <h5 class="mb-1">${statusMeta.label}</h5>
                        <p class="mb-0 small">${statusMeta.description}</p>
                    </div>
                </div>
                <div class="col-md-3 summary-card">
                    <div class="summary-panel p-3 h-100 bg-white">
                        <p class="summary-panel-muted small mb-2">Current Reading</p>
                        <h5 class="mb-1">${formatMetric(summary.currentTemperatureC, 'C')} / ${formatMetric(summary.currentHumidityPercent, '%')}</h5>
                        <p class="mb-0 small text-muted">Latest event: ${escapeHtml(summary.latestEventType || 'No activity yet')}</p>
                    </div>
                </div>
                <div class="col-md-3 summary-card">
                    <div class="summary-panel p-3 h-100 bg-white">
                        <p class="summary-panel-muted small mb-2">Recent Trend</p>
                        <h5 class="mb-1">${formatMetric(summary.averageTemperatureC, 'C')} / ${formatMetric(summary.averageHumidityPercent, '%')}</h5>
                        <p class="mb-0 small text-muted">Temp ${temperatureDelta} | Humidity ${humidityDelta}</p>
                    </div>
                </div>
                <div class="col-md-3 summary-card">
                    <div class="summary-panel p-3 h-100 bg-white">
                        <p class="summary-panel-muted small mb-2">Care Queue</p>
                        <h5 class="mb-1">${taskMessage}</h5>
                        <p class="mb-1 small text-muted">${summary.logsLast7Days} logs in 7 days</p>
                        <small class="text-muted">Last update: ${lastRecorded}</small>
                    </div>
                </div>
                <div class="col-md-4 summary-card">
                    <div class="summary-panel p-3 h-100 bg-white">
                        <p class="summary-panel-muted small mb-2">30-Day Activity</p>
                        <h5 class="mb-1">${summary.logsLast30Days} logs</h5>
                        <p class="mb-1 small text-muted">${summary.activeDaysLast30Days} active days in the last month</p>
                        <small class="text-muted">${summary.loggingStreakDays}-day logging streak</small>
                    </div>
                </div>
            `;
        } catch (error) {
            container.innerHTML = `
                <div class="col-12 summary-card">
                    <div class="border rounded-3 p-3 bg-danger-subtle text-danger-emphasis">
                        Could not load ecosystem summary.
                    </div>
                </div>
            `;
            window.EcoTrackerEcosystemPage.showAlert(error.message || 'Failed to load summary.', 'danger', 'Summary failed');
        } finally {
            setSummaryLoading(false);
        }
    }

    function setSummaryLoading(isLoading) {
        document.getElementById('summary-container').classList.toggle('is-loading', isLoading);
    }

    function resolveStatusMeta(status) {
        if (status === 'STABLE') {
            return { label: 'Stable', description: 'Recent readings look healthy and active.', panelClass: 'summary-panel-status-stable' };
        }

        if (status === 'NEEDS_ATTENTION') {
            return { label: 'Needs Attention', description: 'Recent readings suggest the setup may need intervention.', panelClass: 'summary-panel-status-warning' };
        }

        return { label: 'No Recent Data', description: 'Add a few observations to unlock a live summary.', panelClass: 'summary-panel-status-neutral' };
    }

    function formatMetric(value, suffix) {
        return value === null || value === undefined ? `-- ${suffix}` : `${value} ${suffix}`;
    }

    function formatTrendDelta(value, suffix) {
        if (value === null || value === undefined) return 'not enough data';
        if (value === 0) return `stable (0 ${suffix})`;
        return `${value > 0 ? 'up' : 'down'} ${Math.abs(value)} ${suffix}`;
    }

    return {
        loadSummary,
        setSummaryLoading
    };
})();
