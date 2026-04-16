<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>EcoTracker | Login</title>
    <link rel="icon" href="/favicon.svg" type="image/svg+xml">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="/css/auth.css" rel="stylesheet">
    <link href="/css/login.css" rel="stylesheet">
</head>
<body class="auth-page auth-page-login d-flex align-items-center py-5">
<div class="container auth-shell-login">
    <#if loginError?? && loginError>
        <div class="alert alert-danger mb-4" role="alert">Login failed. Check your username and password.</div>
    <#elseif loggedOut?? && loggedOut>
        <div class="alert alert-success mb-4" role="alert">You have been logged out.</div>
    <#elseif registeredUser?has_content>
        <div class="alert alert-success mb-4" role="alert">Account created for ${registeredUser}. You can log in now.</div>
    <#elseif !auth.enabled>
        <div class="alert alert-info mb-4" role="alert">Authentication is currently disabled in configuration, so this page is optional.</div>
    </#if>

    <div class="row g-4 align-items-stretch">
        <div class="col-lg-5">
            <div class="login-hero-panel h-100 p-4 p-lg-5">
                <p class="login-hero-kicker mb-3">EcoTracker Access</p>
                <h1 class="display-6 fw-semibold mb-3">Sign in to keep your ecosystems and routines in one place.</h1>
                <p class="mb-0 text-white-50">The workspace is now server-rendered and updated with htmx interactions.</p>
            </div>
        </div>

        <div class="col-lg-7">
            <div class="login-card p-4 p-lg-5 h-100">
                <h2 class="h4 mb-3">Log in</h2>
                <p class="text-muted small mb-4">Use your account login and password to open the application.</p>
                <form method="post" action="/login">
                    <div class="mb-3">
                        <label for="login-username" class="form-label">Login</label>
                        <input id="login-username" name="username" class="form-control" value="${registeredUser!""}" autocomplete="username" required>
                    </div>
                    <div class="mb-4">
                        <label for="login-password" class="form-label">Password</label>
                        <input id="login-password" name="password" type="password" class="form-control" autocomplete="current-password" required>
                    </div>
                    <button type="submit" class="btn btn-success w-100 mb-4">Log In</button>
                </form>

                <div class="auth-link-card p-4">
                    <h3 class="h5 mb-2">Need a new account?</h3>
                    <p class="text-muted small mb-3">Open the registration page to create a local demo user profile.</p>
                    <a href="/register" class="btn btn-outline-success">Create Account</a>
                </div>
            </div>
        </div>
    </div>
</div>
</body>
</html>
