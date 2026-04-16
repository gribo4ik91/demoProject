(function () {
    const { showAlert, hideAlert, getAuthStatus } = window.EcoTrackerAuthPages;
    const { getJson, deleteRequest } = window.EcoTrackerApi;

    let authStatus = null;
    let users = [];

    window.addEventListener('load', loadPage);

    async function loadPage() {
        try {
            authStatus = await getAuthStatus();
            updateRoleSummary(authStatus);
            users = await getJson('/api/v1/auth/users', 'Could not load users.');
            renderUsers();
            hideAlert();
        } catch (error) {
            showAlert(error.message || 'Could not load users.', 'danger');
            renderErrorState();
        }
    }

    function updateRoleSummary(status) {
        const role = status?.role || 'USER';
        document.getElementById('users-subtitle').textContent = status?.displayName
            ? `Signed in as ${status.displayName}.`
            : 'Signed-in users can see the full directory.';
        document.getElementById('users-role-badge').textContent = role;
        document.getElementById('users-role-badge').className = `badge rounded-pill ${role === 'ADMIN' ? 'text-bg-success' : 'text-bg-secondary'}`;
        document.getElementById('users-role-note').textContent = role === 'ADMIN'
            ? 'You can review all accounts and delete users except your own admin account.'
            : 'You can view all registered users, but only admins can delete accounts.';
    }

    function renderUsers() {
        const container = document.getElementById('users-list');
        document.getElementById('users-count').textContent = `${users.length} user${users.length === 1 ? '' : 's'}`;

        if (users.length === 0) {
            container.innerHTML = '<div class="col-12"><div class="border rounded-3 p-4 text-center text-muted">No users found.</div></div>';
            return;
        }

        container.innerHTML = users.map((user) => `
            <div class="col-md-6 col-xl-4">
                <div class="user-card p-4">
                    <div class="d-flex justify-content-between align-items-start gap-3 mb-3">
                        <div>
                            <h3 class="h5 mb-1">${window.EcoTrackerUtils.escapeHtml(user.displayName)}</h3>
                            <div class="text-muted small">@${window.EcoTrackerUtils.escapeHtml(user.username)}</div>
                        </div>
                        <span class="badge rounded-pill ${user.role === 'ADMIN' ? 'text-bg-success' : 'text-bg-light border text-dark'}">${window.EcoTrackerUtils.escapeHtml(user.role)}</span>
                    </div>
                    <div class="user-meta mb-3">
                        <div>${window.EcoTrackerUtils.escapeHtml(`${user.firstName} ${user.lastName}`)}</div>
                        <div>${window.EcoTrackerUtils.escapeHtml(user.email)}</div>
                        <div>${window.EcoTrackerUtils.escapeHtml(user.location || 'Location not set')}</div>
                        <div>Created: ${new Date(user.createdAt).toLocaleString()}</div>
                    </div>
                    <div class="d-flex justify-content-between align-items-center gap-2">
                        <span class="text-muted small">${isCurrentUser(user) ? 'This is your account' : 'Account member'}</span>
                        ${buildDeleteButton(user)}
                    </div>
                </div>
            </div>
        `).join('');

        container.querySelectorAll('[data-action="delete-user"]').forEach((button) => {
            button.addEventListener('click', () => deleteUser(button.dataset.userId));
        });
    }

    function buildDeleteButton(user) {
        if (authStatus?.role !== 'ADMIN' || isCurrentUser(user)) {
            return '';
        }

        return `<button type="button" class="btn btn-outline-danger btn-sm" data-action="delete-user" data-user-id="${user.id}">Delete</button>`;
    }

    function isCurrentUser(user) {
        return authStatus?.username && authStatus.username === user.username;
    }

    async function deleteUser(userId) {
        const user = users.find((item) => item.id === userId);
        if (!user) {
            return;
        }

        const confirmed = window.confirm(`Delete user "${user.displayName}" (@${user.username})?`);
        if (!confirmed) {
            return;
        }

        try {
            await deleteRequest(`/api/v1/auth/users/${userId}`, 'Could not delete user.');
            users = users.filter((item) => item.id !== userId);
            renderUsers();
            showAlert(`User ${user.displayName} deleted successfully.`, 'success');
        } catch (error) {
            showAlert(error.message || 'Could not delete user.', 'danger');
        }
    }

    function renderErrorState() {
        document.getElementById('users-count').textContent = 'Directory unavailable';
        document.getElementById('users-list').innerHTML = '<div class="col-12"><div class="border rounded-3 p-4 text-center text-danger">Could not load the user directory.</div></div>';
    }
})();
