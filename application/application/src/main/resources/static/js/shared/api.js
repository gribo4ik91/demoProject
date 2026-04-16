window.EcoTrackerApi = (() => {
    const utils = window.EcoTrackerUtils;

    async function requestJson(url, options = {}, fallbackMessage = 'Request failed.') {
        const response = await fetch(url, options);
        const body = await readResponseBody(response);

        if (!response.ok) {
            const message = utils.resolveApiErrorMessage(body, fallbackMessage);
            const error = new Error(message);
            error.body = body;
            throw error;
        }

        return body;
    }

    async function getJson(url, fallbackMessage = 'Request failed.') {
        return requestJson(url, {}, fallbackMessage);
    }

    async function sendJson(url, method, payload, fallbackMessage = 'Request failed.') {
        return requestJson(url, {
            method,
            headers: { 'Content-Type': 'application/json' },
            body: payload === undefined ? undefined : JSON.stringify(payload)
        }, fallbackMessage);
    }

    async function deleteRequest(url, fallbackMessage = 'Request failed.') {
        return requestJson(url, { method: 'DELETE' }, fallbackMessage);
    }

    async function loadAuthStatus() {
        try {
            const response = await fetch('/api/v1/auth/status');
            if (!response.ok) {
                return null;
            }

            const status = await response.json();
            if (!status.enabled || !status.authenticated) {
                return status;
            }

            const controls = document.getElementById('auth-controls');
            if (controls) {
                controls.classList.remove('d-none');
                controls.classList.add('d-flex');
            }

            const authUser = document.getElementById('auth-user');
            if (authUser) {
                authUser.textContent = `Signed in as ${status.displayName || status.username}`;
            }

            return status;
        } catch (error) {
            console.error(error);
            return null;
        }
    }

    async function readResponseBody(response) {
        if (response.status === 204) {
            return null;
        }

        const contentType = response.headers.get('content-type') || '';
        if (contentType.includes('application/json')) {
            return response.json().catch(() => null);
        }

        return response.text().catch(() => null);
    }

    return {
        requestJson,
        getJson,
        sendJson,
        deleteRequest,
        loadAuthStatus
    };
})();
