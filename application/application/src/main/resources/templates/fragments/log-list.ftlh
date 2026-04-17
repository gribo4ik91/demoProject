<div class="d-flex justify-content-between align-items-center mb-3 gap-3 flex-wrap">
    <p class="text-muted small mb-0">
        <#if logPage.totalElements == 0>
            No activity logs found.
        <#else>
            ${logPage.totalElements} log<#if logPage.totalElements != 1>s</#if> total
        </#if>
    </p>
    <span class="text-muted small">Page ${logPage.page + 1} of <#if logPage.totalPages == 0>1<#else>${logPage.totalPages}</#if></span>
</div>

<div class="list-group">
    <#if logPage.items?size == 0>
        <div class="text-muted">No logs for this filter yet.</div>
    <#else>
        <#list logPage.items as log>
            <div class="list-group-item">
                <div class="d-flex justify-content-between align-items-start gap-3 flex-wrap mb-2">
                    <div>
                        <div class="d-flex align-items-center gap-2 flex-wrap mb-1">
                            <span class="${ui.eventTypeBadgeClass(log.eventType)}">${ui.eventTypeLabel(log.eventType)}</span>
                        </div>
                        <strong>${ui.formatDateTime(log.recordedAt)}</strong>
                        <div class="text-muted small">Created by ${ui.createdByLabel(log.createdByDisplayName, log.createdByUsername)}</div>
                    </div>
                    <div class="small text-muted">
                        Temp: ${log.temperatureC!"-"} C<br>
                        Humidity: ${log.humidityPercent!"-"}%
                    </div>
                </div>
                <p class="mb-2">${log.notes!"No notes provided."}</p>

                <details>
                    <summary class="small text-primary">Edit log</summary>
                    <form class="row g-2 mt-2"
                          hx-patch="/ui/ecosystems/${ecosystemId}/logs/${log.id}"
                          hx-target="#log-form-feedback"
                          hx-swap="innerHTML">
                        <div class="col-md-2">
                            <input type="number" step="0.1" name="temperatureC" class="form-control" value="${log.temperatureC!""}" placeholder="Temp C">
                        </div>
                        <div class="col-md-2">
                            <input type="number" name="humidityPercent" class="form-control" value="${log.humidityPercent!""}" placeholder="Humidity %">
                        </div>
                        <div class="col-md-3">
                            <select name="eventType" class="form-select" required>
                                <#list ui.logEventTypes() as option>
                                    <option value="${option}" <#if option == log.eventType>selected</#if>>${ui.eventTypeLabel(option)}</option>
                                </#list>
                            </select>
                        </div>
                        <div class="col-md-5">
                            <input type="text" name="notes" class="form-control" maxlength="500" value="${log.notes!""}" placeholder="Notes">
                        </div>
                        <div class="col-12 d-flex justify-content-end gap-2">
                            <button type="button" class="btn btn-outline-secondary btn-sm" data-close-details>Cancel</button>
                            <button type="submit" class="btn btn-outline-primary btn-sm">Save Changes</button>
                        </div>
                    </form>
                </details>
            </div>
        </#list>
    </#if>
</div>

<div class="d-flex justify-content-between align-items-center mt-3 gap-3 flex-wrap">
    <button class="btn btn-outline-secondary btn-sm" <#if !logPage.hasPrevious>disabled</#if>
            hx-get="/ui/ecosystems/${ecosystemId}/logs?eventType=${logEventType?url}&page=${logPage.page - 1}"
            hx-target="#logs-panel" hx-swap="innerHTML">Previous</button>
    <button class="btn btn-outline-secondary btn-sm" <#if !logPage.hasNext>disabled</#if>
            hx-get="/ui/ecosystems/${ecosystemId}/logs?eventType=${logEventType?url}&page=${logPage.page + 1}"
            hx-target="#logs-panel" hx-swap="innerHTML">Next</button>
</div>
