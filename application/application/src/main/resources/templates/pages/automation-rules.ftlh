<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>EcoTracker | Automation Rules</title>
    <link rel="icon" href="/favicon.svg" type="image/svg+xml">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="/css/automation-rules.css" rel="stylesheet">
    <script src="https://unpkg.com/htmx.org@2.0.4"></script>
</head>
<body class="rules-page py-5">
<div class="container rules-shell">
    <div id="automation-rule-feedback" class="mb-4"></div>

    <div class="d-flex justify-content-between align-items-center flex-wrap gap-3 mb-4">
        <div>
            <p class="text-uppercase text-success fw-semibold mb-2">EcoTracker Automations</p>
            <h1 class="h3 mb-1">Suggested Task Rules</h1>
            <p class="text-muted mb-0">Configure when EcoTracker should propose follow-up care tasks.</p>
        </div>
        <div class="d-flex gap-2 flex-wrap">
            <a href="/" class="btn btn-outline-secondary">Back to Home</a>
            <a href="/profile" class="btn btn-outline-secondary">My Profile</a>
            <form action="/logout" method="post" class="mb-0">
                <button type="submit" class="btn btn-outline-secondary">Log out</button>
            </form>
        </div>
    </div>

    <section class="rules-hero p-4 mb-4">
        <div class="row g-3 align-items-stretch">
            <div class="col-md-4">
                <div class="metric-card h-100">
                    <div class="metric-label">All rules</div>
                    <div class="metric-value">${rules?size}</div>
                    <div class="metric-note">Reusable automation logic for suggested tasks.</div>
                </div>
            </div>
            <div class="col-md-4">
                <div class="metric-card h-100">
                    <div class="metric-label">Active rules</div>
                    <div class="metric-value">${activeRuleCount}</div>
                    <div class="metric-note">Enabled rules can generate suggestions.</div>
                </div>
            </div>
            <div class="col-md-4">
                <div class="metric-card h-100">
                    <div class="metric-label">Coverage</div>
                    <div class="metric-value">${eventRuleCount} / ${inactivityRuleCount}</div>
                    <div class="metric-note">Event-based and inactivity-based rule families.</div>
                </div>
            </div>
        </div>
    </section>

    <div class="row g-4 align-items-start">
        <div class="col-xl-8">
            <section class="rules-panel p-4">
                <div class="d-flex justify-content-between align-items-center gap-3 flex-wrap mb-3">
                    <div>
                        <p class="section-label mb-1">Library</p>
                        <h2 class="h5 mb-0">Automation rule list</h2>
                    </div>
                    <button type="button"
                            class="btn btn-success"
                            hx-get="/ui/automation-rules/editor"
                            hx-target="#rule-editor-panel"
                            hx-swap="innerHTML">Create Rule</button>
                </div>

                <form id="rule-filter-form" class="row g-2 mb-3">
                    <div class="col-md-6">
                        <label class="form-label small text-muted mb-1">Status</label>
                        <select class="form-select"
                                name="statusFilter"
                                hx-get="/ui/automation-rules/list"
                                hx-include="#rule-filter-form"
                                hx-target="#rule-list-panel"
                                hx-swap="innerHTML">
                            <#list ui.automationRuleStatusFilters() as option>
                                <option value="${option}" <#if option == ruleStatusFilter>selected</#if>>${option?cap_first}</option>
                            </#list>
                        </select>
                    </div>
                    <div class="col-md-6">
                        <label class="form-label small text-muted mb-1">Rule family</label>
                        <select class="form-select"
                                name="triggerFilter"
                                hx-get="/ui/automation-rules/list"
                                hx-include="#rule-filter-form"
                                hx-target="#rule-list-panel"
                                hx-swap="innerHTML">
                            <#list ui.automationRuleTriggerFilters() as option>
                                <option value="${option}" <#if option == ruleTriggerFilter>selected</#if>>${ui.automationRuleTriggerLabel(option)}</option>
                            </#list>
                        </select>
                    </div>
                </form>

                <div id="rule-list-panel">
                    <#include "../fragments/rule-list.ftlh">
                </div>
            </section>
        </div>

        <div class="col-xl-4">
            <div id="rule-editor-panel">
                <#include "../fragments/rule-editor-empty.ftlh">
            </div>
        </div>
    </div>
</div>
<script src="/js/ssr-ui.js"></script>
</body>
</html>
