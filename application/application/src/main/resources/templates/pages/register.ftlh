<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>EcoTracker | Create Account</title>
    <link rel="icon" href="/favicon.svg" type="image/svg+xml">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="/css/auth.css" rel="stylesheet">
    <link href="/css/register.css" rel="stylesheet">
    <script src="https://unpkg.com/htmx.org@2.0.4"></script>
</head>
<body class="auth-page auth-page-register d-flex align-items-center py-5">
<div class="container register-shell">
    <div id="register-feedback" class="mb-4">
        <#if !auth.enabled>
            <div class="alert alert-info mb-0" role="alert">Authentication is disabled right now, so account creation is optional.</div>
        </#if>
    </div>

    <div class="auth-card-base p-4 p-lg-5">
        <div class="d-flex justify-content-between align-items-start flex-wrap gap-3 mb-4">
            <div>
                <p class="text-uppercase text-success fw-semibold mb-2">EcoTracker Account</p>
                <h1 class="h3 mb-2">Create a new user profile</h1>
                <p class="text-muted mb-0">The display name will be shown in the application header after login.</p>
            </div>
            <a href="/login" class="btn btn-outline-secondary">Back to Login</a>
        </div>

        <form hx-post="/ui/register" hx-target="#register-feedback" hx-swap="innerHTML">
            <div class="row g-3">
                <div class="col-md-6"><label class="form-label">Display name</label><input name="displayName" class="form-control" minlength="3" maxlength="60" required></div>
                <div class="col-md-6"><label class="form-label">Login</label><input name="username" class="form-control" minlength="3" maxlength="40" required></div>
                <div class="col-md-6"><label class="form-label">First name</label><input name="firstName" class="form-control" minlength="2" maxlength="60" required></div>
                <div class="col-md-6"><label class="form-label">Last name</label><input name="lastName" class="form-control" minlength="2" maxlength="60" required></div>
                <div class="col-md-6"><label class="form-label">Email</label><input name="email" type="email" class="form-control" maxlength="120" required></div>
                <div class="col-md-6"><label class="form-label">Location</label><input name="location" class="form-control" maxlength="80"></div>
                <div class="col-12"><label class="form-label">Bio</label><textarea name="bio" class="form-control" rows="4" maxlength="500"></textarea></div>
                <div class="col-md-6"><label class="form-label">Password</label><input name="password" type="password" class="form-control" minlength="6" maxlength="72" required></div>
            </div>
            <div class="d-flex justify-content-between align-items-center flex-wrap gap-3 mt-4">
                <p class="text-muted small mb-0">After registration you will be redirected back to login.</p>
                <button type="submit" class="btn btn-success px-4">Create Account</button>
            </div>
        </form>
    </div>
</div>
</body>
</html>
