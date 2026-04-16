(function () {
    const { showAlert, hideAlert } = window.EcoTrackerAuthPages;
    const { getJson, sendJson } = window.EcoTrackerApi;

    window.addEventListener('load', () => {
        document.getElementById('profile-form').addEventListener('submit', saveProfile);
        loadProfile();
    });

    async function loadProfile() {
        try {
            const profile = await getJson('/api/v1/auth/profile', 'Could not load profile.');
            fillProfile(profile);
            hideAlert();
        } catch (error) {
            showAlert(error.message || 'Could not load profile.', 'danger');
        }
    }

    async function saveProfile(event) {
        event.preventDefault();
        const button = event.submitter;
        const payload = {
            displayName: document.getElementById('display-name').value.trim(),
            firstName: document.getElementById('first-name').value.trim(),
            lastName: document.getElementById('last-name').value.trim(),
            email: document.getElementById('email').value.trim(),
            location: document.getElementById('location').value.trim() || null,
            bio: document.getElementById('bio').value.trim() || null
        };

        button.disabled = true;

        try {
            const profile = await sendJson('/api/v1/auth/profile', 'PUT', payload, 'Could not update profile.');
            fillProfile(profile);
            showAlert('Profile updated successfully.', 'success');
        } catch (error) {
            showAlert(error.message || 'Could not update profile.', 'danger');
        } finally {
            button.disabled = false;
        }
    }

    function fillProfile(profile) {
        document.getElementById('profile-display-name').textContent = profile.displayName;
        document.getElementById('profile-login').textContent = `Login: ${profile.username}`;
        document.getElementById('profile-role').textContent = `Role: ${profile.role}`;
        document.getElementById('profile-created-at').textContent = `Created: ${new Date(profile.createdAt).toLocaleString()}`;
        document.getElementById('display-name').value = profile.displayName;
        document.getElementById('username').value = profile.username;
        document.getElementById('first-name').value = profile.firstName;
        document.getElementById('last-name').value = profile.lastName;
        document.getElementById('email').value = profile.email;
        document.getElementById('location').value = profile.location || '';
        document.getElementById('bio').value = profile.bio || '';
    }

})();
