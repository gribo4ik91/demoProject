window.EcoTrackerEcosystemState = {
    ecosystemId: new URLSearchParams(window.location.search).get('id'),
    currentLogPage: 0,
    currentEcosystem: null,
    editingTaskId: null,
    editingLogId: null,
    toastCounter: 0,
    logPageSize: 5
};
