<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>EcoTracker | My Profile</title>
    <link rel="icon" href="/favicon.svg" type="image/svg+xml">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="/css/auth.css" rel="stylesheet">
    <link href="/css/profile.css" rel="stylesheet">
    <script src="https://unpkg.com/htmx.org@2.0.4"></script>
</head>
<body class="auth-page auth-page-profile py-5">
<div class="container profile-shell">
    <div id="profile-feedback" class="mb-4"></div>

    <div class="d-flex justify-content-between align-items-center flex-wrap gap-3 mb-4">
        <div>
            <p class="text-uppercase text-success fw-semibold mb-2">EcoTracker Profile</p>
            <h1 class="h3 mb-0">My profile</h1>
        </div>
        <div class="d-flex gap-2">
            <a href="/" class="btn btn-outline-secondary">Back to Home</a>
            <a href="/users" class="btn btn-outline-secondary">Users</a>
            <form action="/logout" method="post" class="mb-0">
                <button type="submit" class="btn btn-outline-secondary">Log out</button>
            </form>
        </div>
    </div>

    <div class="row g-4">
        <div class="col-lg-4">
            <div class="auth-side-panel p-4 h-100">
                <p class="small text-white-50 mb-2">Signed in profile</p>
                <h2 class="h3 mb-2">${profile.displayName}</h2>
                <p class="mb-3 text-white-50">Login: ${profile.username}</p>
                <p class="mb-3 text-white-50">Role: ${profile.role}</p>
                <p class="small text-white-50 mb-0">Created: ${ui.formatDateTime(profile.createdAt)}</p>
            </div>
        </div>

        <div class="col-lg-8">
            <div class="auth-card-base p-4 p-lg-5">
                <form hx-put="/ui/profile" hx-target="#profile-feedback" hx-swap="innerHTML">
                    <div class="row g-3">
                        <div class="col-md-6"><label class="form-label">Display name</label><input name="displayName" class="form-control" value="${profile.displayName}" minlength="3" maxlength="60" required></div>
                        <div class="col-md-6"><label class="form-label">Login</label><input class="form-control" value="${profile.username}" disabled></div>
                        <div class="col-md-6"><label class="form-label">First name</label><input name="firstName" class="form-control" value="${profile.firstName}" minlength="2" maxlength="60" required></div>
                        <div class="col-md-6"><label class="form-label">Last name</label><input name="lastName" class="form-control" value="${profile.lastName}" minlength="2" maxlength="60" required></div>
                        <div class="col-md-6"><label class="form-label">Email</label><input name="email" type="email" class="form-control" value="${profile.email}" maxlength="120" required></div>
                        <div class="col-md-6"><label class="form-label">Location</label><input name="location" class="form-control" value="${profile.location!""}" maxlength="80"></div>
                        <div class="col-12"><label class="form-label">Bio</label><textarea name="bio" class="form-control" rows="4" maxlength="500">${profile.bio!""}</textarea></div>
                    </div>
                    <div class="d-flex justify-content-between align-items-center flex-wrap gap-3 mt-4">
                        <p class="text-muted small mb-0">Login and password stay unchanged here.</p>
                        <button type="submit" class="btn btn-success px-4">Save Profile</button>
                    </div>
                </form>
            </div>
        </div>
    </div>
</div>
</body>
</html>
