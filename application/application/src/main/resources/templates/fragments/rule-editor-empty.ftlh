<div class="rules-panel p-4 sticky-editor empty-editor-state">
    <p class="section-label mb-1">Builder</p>
    <h2 class="h5 mb-2">No rule selected</h2>
    <p class="text-muted mb-4">
        Choose <strong>Create Rule</strong> to start a new automation or click <strong>Edit</strong> on an existing rule.
    </p>
    <div class="d-grid gap-3">
        <div class="empty-editor-card">
            <div class="small text-muted mb-1">Suggested starting point</div>
            <div class="fw-semibold">After watering, suggest a moisture check in 1 day</div>
        </div>
        <button type="button"
                class="btn btn-success"
                hx-get="/ui/automation-rules/editor"
                hx-target="#rule-editor-panel"
                hx-swap="innerHTML">Create Rule</button>
    </div>
</div>
