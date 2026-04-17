<div class="d-grid gap-3">
    <#if rules?size == 0>
        <div class="empty-rules-state">
            <h3 class="h6 mb-2">No rules match the current filters</h3>
            <p class="text-muted mb-0">Try switching the filters or create your first automation rule.</p>
        </div>
    <#else>
        <#list rules as rule>
            <article class="rule-card">
                <div class="d-flex justify-content-between align-items-start gap-3 flex-wrap mb-3">
                    <div>
                        <div class="d-flex align-items-center gap-2 flex-wrap mb-2">
                            <h3 class="h5 mb-0">${rule.name}</h3>
                            <span class="${ui.automationRuleStatusBadgeClass(rule.enabled)}">${ui.automationRuleStatusLabel(rule.enabled)}</span>
                            <span class="badge rounded-pill text-bg-light border">${ui.automationRuleTriggerLabel(rule.triggerType)}</span>
                        </div>
                        <div class="text-muted small">Updated ${ui.formatDateTime(rule.updatedAt)}</div>
                    </div>
                    <div class="d-flex gap-2 flex-wrap">
                        <button type="button"
                                class="btn btn-outline-success btn-sm"
                                hx-get="/ui/automation-rules/${rule.id}/editor"
                                hx-target="#rule-editor-panel"
                                hx-swap="innerHTML">Edit</button>
                        <button type="button"
                                class="btn btn-outline-secondary btn-sm"
                                hx-patch="/ui/automation-rules/${rule.id}/enabled"
                                hx-vals='{"enabled":${(!rule.enabled)?c}}'
                                hx-target="#automation-rule-feedback"
                                hx-swap="innerHTML">${rule.enabled?string("Disable", "Enable")}</button>
                        <button type="button"
                                class="btn btn-outline-danger btn-sm"
                                hx-delete="/ui/automation-rules/${rule.id}"
                                hx-confirm="Delete rule '${rule.name}'?"
                                hx-target="#automation-rule-feedback"
                                hx-swap="innerHTML">Delete</button>
                    </div>
                </div>

                <div class="rule-meta-grid mb-3">
                    <div class="rule-meta-item">
                        <div class="small text-muted">Scope</div>
                        <div class="fw-semibold">
                            ${ui.automationRuleScopeLabel(rule.scopeType)}
                            <#if rule.ecosystemType??>
                                <span class="text-muted"> | ${ui.ecosystemTypeLabel(rule.ecosystemType)}</span>
                            </#if>
                        </div>
                    </div>
                    <div class="rule-meta-item">
                        <div class="small text-muted">Trigger</div>
                        <div class="fw-semibold">
                            <#if rule.triggerType == "AFTER_EVENT">
                                ${ui.eventTypeLabel(rule.eventType)} + ${rule.delayDays!0} day<#if (rule.delayDays!0) != 1>s</#if>
                            <#else>
                                No ${ui.eventTypeLabel(rule.eventType)?lower_case} for ${rule.inactivityDays!0} day<#if (rule.inactivityDays!0) != 1>s</#if>
                            </#if>
                        </div>
                    </div>
                    <div class="rule-meta-item">
                        <div class="small text-muted">Generated task</div>
                        <div class="fw-semibold">${rule.taskTitle}</div>
                    </div>
                    <div class="rule-meta-item">
                        <div class="small text-muted">Protection</div>
                        <div class="fw-semibold">${rule.preventDuplicates?string("No duplicate open task", "Duplicates allowed")}</div>
                    </div>
                </div>

                <details class="rule-preview">
                    <summary>Preview result</summary>
                    <div class="rule-preview-body">
                        <#if rule.triggerType == "AFTER_EVENT">
                            When a <strong>${ui.eventTypeLabel(rule.eventType)}</strong> log is recorded
                            <#if rule.scopeType == "ECOSYSTEM_TYPE">
                                for <strong>${ui.ecosystemTypeLabel(rule.ecosystemType)}</strong> ecosystems,
                            <#else>
                                for any ecosystem,
                            </#if>
                            EcoTracker will suggest <strong>${rule.taskTitle}</strong> as an
                            <strong>${ui.taskTypeLabel(rule.taskType)}</strong> task after
                            <strong>${rule.delayDays!0}</strong> day<#if (rule.delayDays!0) != 1>s</#if>.
                        <#else>
                            If there is no <strong>${ui.eventTypeLabel(rule.eventType)?lower_case}</strong>
                            <#if rule.scopeType == "ECOSYSTEM_TYPE">
                                recorded for <strong>${ui.ecosystemTypeLabel(rule.ecosystemType)}</strong> ecosystems
                            <#else>
                                recorded across the tracked ecosystems
                            </#if>
                            for <strong>${rule.inactivityDays!0}</strong> day<#if (rule.inactivityDays!0) != 1>s</#if>,
                            EcoTracker will suggest <strong>${rule.taskTitle}</strong>.
                        </#if>
                    </div>
                </details>
            </article>
        </#list>
    </#if>
</div>
