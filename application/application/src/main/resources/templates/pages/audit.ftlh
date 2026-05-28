<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>EcoTracker | Audit Log</title>
    <link rel="icon" href="/favicon.svg" type="image/svg+xml">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="/css/home.css" rel="stylesheet">
    <script src="https://unpkg.com/htmx.org@2.0.4"></script>
</head>
<body>
<div class="container shell py-5">
    <div class="d-flex justify-content-between align-items-center mb-4 gap-3 flex-wrap">
        <div>
            <p class="text-uppercase text-success fw-semibold mb-1">EcoTracker Audit</p>
            <h1 class="h3 mb-0">Inventory Change Log</h1>
        </div>
        <div class="d-flex align-items-center gap-2 flex-wrap">
            <a href="/" class="btn btn-outline-secondary btn-sm">Back to Home</a>
            <#if auth.authenticated>
                <a href="/profile" class="btn btn-outline-success btn-sm">My Profile</a>
                <form action="/logout" method="post" class="mb-0">
                    <button type="submit" class="btn btn-outline-secondary btn-sm">Log out</button>
                </form>
            </#if>
        </div>
    </div>

    <section class="panel p-4 mb-4">
        <div class="d-flex justify-content-between align-items-center mb-3 gap-3 flex-wrap">
            <div>
                <p class="section-label mb-1">History</p>
                <h2 class="h5 mb-0">All Inventory Changes</h2>
            </div>
            <div class="text-muted small">
                <#if auditPage.totalElements == 0>
                    No changes recorded yet
                <#else>
                    ${auditPage.totalElements} total change<#if auditPage.totalElements != 1>s</#if>
                </#if>
            </div>
        </div>

        <div id="audit-list-panel">
            <#include "../fragments/audit-list.ftlh">
        </div>
    </section>
</div>
<script src="/js/ssr-ui.js"></script>
</body>
</html>
