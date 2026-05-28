<#if auditPage.items?size == 0>
    <div class="border rounded-3 p-4 text-center text-muted">No inventory changes have been recorded yet.</div>
<#else>
    <div class="audit-list">
        <#list auditPage.items as audit>
            <div class="audit-entry">
                <div class="audit-entry-main">
                    <div class="d-flex align-items-center gap-2 flex-wrap mb-1">
                        <span class="${ui.auditActionBadgeClass(audit.action)}">${ui.auditActionLabel(audit.action)}</span>
                        <span class="text-muted small">${ui.auditEntityTypeLabel(audit.entityType)}</span>
                    </div>
                    <div class="fw-semibold">${audit.entityName!"Unnamed item"}</div>
                    <#if audit.fieldName??>
                        <div class="audit-change-line">
                            <span class="audit-field">${audit.fieldName}</span>
                            <span class="audit-value">${audit.oldValue!"Empty"}</span>
                            <span class="audit-arrow">-&gt;</span>
                            <span class="audit-value">${audit.newValue!"Empty"}</span>
                        </div>
                    <#elseif audit.newValue??>
                        <div class="audit-change-line">${audit.newValue}</div>
                    <#elseif audit.oldValue??>
                        <div class="audit-change-line">${audit.oldValue}</div>
                    </#if>
                </div>
                <div class="audit-entry-meta text-muted small">
                    <div>${ui.formatDateTime(audit.createdAt)}</div>
                    <div>By ${ui.createdByLabel(audit.createdByDisplayName, audit.createdByUsername)}</div>
                </div>
            </div>
        </#list>
    </div>

    <#if auditPage.totalPages gt 1>
        <div class="d-flex justify-content-between align-items-center mt-4 gap-3 flex-wrap">
            <button class="btn btn-outline-secondary btn-sm" <#if !auditPage.hasPrevious>disabled</#if>
                    hx-get="/ui/audit?page=${auditPage.page - 1}"
                    hx-target="#audit-list-panel" hx-swap="innerHTML">Previous</button>
            <span class="text-muted small">Page ${auditPage.page + 1} of ${auditPage.totalPages}</span>
            <button class="btn btn-outline-success btn-sm" <#if !auditPage.hasNext>disabled</#if>
                    hx-get="/ui/audit?page=${auditPage.page + 1}"
                    hx-target="#audit-list-panel" hx-swap="innerHTML">Next</button>
        </div>
    </#if>
</#if>
