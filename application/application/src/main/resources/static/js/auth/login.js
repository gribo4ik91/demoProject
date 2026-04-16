(function () {
    const { showAlert, getAuthStatus } = window.EcoTrackerAuthPages;

    window.addEventListener('load', async () => {
        const params = new URLSearchParams(window.location.search);
        if (params.has('error')) {
            showAlert('Login failed. Check your username and password.', 'danger');
        } else if (params.has('logout')) {
            showAlert('You have been logged out.', 'success');
        } else if (params.has('registered')) {
            const login = params.get('registered');
            showAlert(`Account created for ${login}. You can log in now.`, 'success');
            document.getElementById('login-username').value = login || '';
        }

        const status = await getAuthStatus();
        if (!status) {
            return;
        }

        if (!status.enabled) {
            showAlert('Authentication is currently disabled in configuration, so this page is optional.', 'info');
        } else if (status.authenticated) {
            window.location.href = '/';
        }
    });
})();
