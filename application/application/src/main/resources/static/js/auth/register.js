(function () {
    const { showAlert, getAuthStatus } = window.EcoTrackerAuthPages;
    const { sendJson } = window.EcoTrackerApi;

    window.addEventListener('load', async () => {
        document.getElementById('register-form').addEventListener('submit', registerUser);
        const status = await getAuthStatus();
        if (!status) {
            return;
        }

        if (!status.enabled) {
            showAlert('Authentication is disabled right now, so account creation is optional.', 'info');
        } else if (status.authenticated) {
            window.location.href = '/';
        }
    });

    async function registerUser(event) {
        event.preventDefault();
        const button = event.submitter;
        const payload = {
            displayName: document.getElementById('register-display-name').value.trim(),
            username: document.getElementById('register-username').value.trim(),
            firstName: document.getElementById('register-first-name').value.trim(),
            lastName: document.getElementById('register-last-name').value.trim(),
            email: document.getElementById('register-email').value.trim(),
            location: document.getElementById('register-location').value.trim() || null,
            bio: document.getElementById('register-bio').value.trim() || null,
            password: document.getElementById('register-password').value
        };

        button.disabled = true;

        try {
            await sendJson('/api/v1/auth/register', 'POST', payload, 'Could not create the user.');
            window.location.href = `/login?registered=${encodeURIComponent(payload.username)}`;
        } catch (error) {
            showAlert(error.message || 'Could not create the user.', 'danger');
        } finally {
            button.disabled = false;
        }
    }

})();
