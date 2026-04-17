<#assign isEditing = editingRule??>
<section class="rules-panel p-4 sticky-editor">
    <div class="d-flex justify-content-between align-items-center gap-3 flex-wrap mb-3">
        <div>
            <p class="section-label mb-1">${isEditing?string("Edit", "Create")}</p>
            <h2 class="h5 mb-0">${isEditing?string("Rule editor", "New rule")}</h2>
        </div>
        <#if isEditing>
            <button type="button"
                    class="btn btn-outline-secondary btn-sm"
                    hx-get="/ui/automation-rules/editor"
                    hx-target="#rule-editor-panel"
                    hx-swap="innerHTML">New Rule</button>
        </#if>
    </div>

    <p class="text-muted small mb-3">
        Start with the MVP fields: scope, trigger, delay, generated task title, and duplicate protection.
    </p>

    <#if isEditing>
    <form data-rule-form
          hx-patch="/ui/automation-rules/${editingRule.id}"
          hx-target="#automation-rule-feedback"
          hx-swap="innerHTML">
    <#else>
    <form data-rule-form
          hx-post="/ui/automation-rules"
          hx-target="#automation-rule-feedback"
          hx-swap="innerHTML">
    </#if>
        <div class="d-grid gap-3">
            <div>
                <label class="form-label small text-muted mb-1">Rule name</label>
                <input type="text" class="form-control" name="name" maxlength="120" value="${((editingRule.name)!"")}" placeholder="Post-watering moisture check" required>
            </div>

            <div class="form-check form-switch">
                <input class="form-check-input" type="checkbox" role="switch" id="rule-enabled" name="enabled" value="true" <#if !isEditing || ((editingRule.enabled)!false)>checked</#if>>
                <label class="form-check-label" for="rule-enabled">Rule enabled</label>
            </div>

            <div class="editor-section">
                <div class="editor-section-title">Scope</div>
                <div class="d-grid gap-2">
                    <#list ui.automationRuleScopeTypes() as option>
                        <label class="radio-card">
                            <input type="radio" name="scopeType" value="${option}" <#if ((editingRule.scopeType)!"ALL_ECOSYSTEMS") == option>checked</#if>>
                            <span>
                                <strong>${ui.automationRuleScopeLabel(option)}</strong>
                                <small class="d-block text-muted">
                                    <#if option == "ALL_ECOSYSTEMS">
                                        Apply the rule across every ecosystem.
                                    <#else>
                                        Restrict the rule to one ecosystem type.
                                    </#if>
                                </small>
                            </span>
                        </label>
                    </#list>
                </div>
                <div class="mt-3" data-scope-target="ECOSYSTEM_TYPE">
                    <label class="form-label small text-muted mb-1">Ecosystem type</label>
                    <select class="form-select" name="ecosystemType">
                        <option value="">Select ecosystem type</option>
                        <#list ui.ecosystemTypes() as option>
                            <option value="${option}" <#if ((editingRule.ecosystemType)!"") == option>selected</#if>>${ui.ecosystemTypeLabel(option)}</option>
                        </#list>
                    </select>
                </div>
            </div>

            <div class="editor-section">
                <div class="editor-section-title">Trigger</div>
                <div class="d-grid gap-2">
                    <#list ui.automationRuleTriggerTypes() as option>
                        <label class="radio-card">
                            <input type="radio" name="triggerType" value="${option}" <#if ((editingRule.triggerType)!"AFTER_EVENT") == option>checked</#if>>
                            <span>
                                <strong>${ui.automationRuleTriggerLabel(option)}</strong>
                                <small class="d-block text-muted">
                                    <#if option == "AFTER_EVENT">
                                        React to a recorded event and optionally wait a few days.
                                    <#else>
                                        Trigger when a certain event has not been logged for a while.
                                    </#if>
                                </small>
                            </span>
                        </label>
                    </#list>
                </div>
                <div class="row g-3 mt-1">
                    <div class="col-12">
                        <label class="form-label small text-muted mb-1">Event type</label>
                        <select class="form-select" name="eventType">
                            <#list ui.logEventTypes() as option>
                                <option value="${option}" <#if ((editingRule.eventType)!"") == option>selected</#if>>${ui.eventTypeLabel(option)}</option>
                            </#list>
                        </select>
                    </div>
                    <div class="col-md-6" data-trigger-target="AFTER_EVENT">
                        <label class="form-label small text-muted mb-1">Delay in days</label>
                        <input type="number" class="form-control" name="delayDays" min="0" max="365" value="${((editingRule.delayDays)!0)}">
                    </div>
                    <div class="col-md-6" data-trigger-target="AFTER_INACTIVITY">
                        <label class="form-label small text-muted mb-1">Inactivity days</label>
                        <input type="number" class="form-control" name="inactivityDays" min="1" max="365" value="${((editingRule.inactivityDays)!7)}">
                    </div>
                </div>
            </div>

            <div class="editor-section">
                <div class="editor-section-title">Generated task</div>
                <div class="d-grid gap-3">
                    <div>
                        <label class="form-label small text-muted mb-1">Suggested task title</label>
                        <input type="text" class="form-control" name="taskTitle" maxlength="160" value="${((editingRule.taskTitle)!"")}" placeholder="Check soil moisture" required>
                    </div>
                    <div>
                        <label class="form-label small text-muted mb-1">Task type</label>
                        <select class="form-select" name="taskType">
                            <#list ui.taskTypes() as option>
                                <option value="${option}" <#if ((editingRule.taskType)!"INSPECTION") == option>selected</#if>>${ui.taskTypeLabel(option)}</option>
                            </#list>
                        </select>
                    </div>
                    <label class="checkbox-card">
                        <input type="checkbox" name="preventDuplicates" value="true" <#if !isEditing || ((editingRule.preventDuplicates)!false)>checked</#if>>
                        <span>
                            <strong>Do not create duplicate open task</strong>
                            <small class="d-block text-muted">Useful for recurring suggestions that should stay tidy.</small>
                        </span>
                    </label>
                </div>
            </div>

            <div class="preview-card" data-rule-preview>
                Preview will appear here as you adjust the rule.
            </div>

            <div class="d-flex justify-content-end gap-2 flex-wrap">
                <button type="button"
                        class="btn btn-outline-secondary"
                        hx-get="/ui/automation-rules/editor/empty"
                        hx-target="#rule-editor-panel"
                        hx-swap="innerHTML">Cancel</button>
                <button type="submit" class="btn btn-success">${isEditing?string("Save Changes", "Save Rule")}</button>
            </div>
        </div>
    </form>
</section>
