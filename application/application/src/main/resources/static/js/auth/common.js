window.EcoTrackerAuthPages = (() => {
    function showAlert(message, type) {
        const alert = document.getElementById('page-alert');
        if (!alert) {
            return;
        }

        alert.className = `alert alert-${type} mb-4`;
        alert.textContent = message;
    }

    function hideAlert() {
        const alert = document.getElementById('page-alert');
        if (!alert) {
            return;
        }

        alert.className = 'alert d-none mb-4';
        alert.textContent = '';
    }

    async function getAuthStatus() {
        try {
            const response = await fetch('/api/v1/auth/status');
            if (!response.ok) {
                return null;
            }

            return response.json();
        } catch (error) {
            console.error(error);
            return null;
        }
    }

    return {
        showAlert,
        hideAlert,
        getAuthStatus
    };
})();
