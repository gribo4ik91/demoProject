package com.example.api.controller

import com.example.api.dto.AuthStatusResponse
import com.example.api.dto.MaintenanceTaskResponse
import com.example.api.repository.AppUserRepository
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.core.Authentication
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.stereotype.Component
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * Provides shared labels, formatting helpers, and authentication view state for Freemarker templates.
 */
@Component
class UiSupport(
    private val appUserRepository: AppUserRepository,
    @Value("\${app.auth.enabled:false}") private val authEnabled: Boolean
) {

    private val dateFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy")
    private val dateTimeFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm")

    fun ecosystemTypes(): List<String> = listOf("FORMICARIUM", "FLORARIUM", "INDOOR_PLANTS", "DIY_INCUBATOR")

    fun logEventTypes(): List<String> = listOf("OBSERVATION", "FEEDING", "WATERING")

    fun taskTypes(): List<String> = listOf("WATERING", "FEEDING", "CLEANING", "INSPECTION")

    fun taskFilters(): List<String> = listOf("ALL", "OPEN", "DONE", "OVERDUE", "DISMISSED")

    fun taskSources(): List<String> = listOf("ALL", "MANUAL", "SUGGESTED")

    fun automationRuleStatusFilters(): List<String> = listOf("ALL", "ACTIVE", "DISABLED")

    fun automationRuleTriggerFilters(): List<String> = listOf("ALL", "AFTER_EVENT", "AFTER_INACTIVITY")

    fun automationRuleScopeTypes(): List<String> = listOf("ALL_ECOSYSTEMS", "ECOSYSTEM_TYPE")

    fun automationRuleTriggerTypes(): List<String> = listOf("AFTER_EVENT", "AFTER_INACTIVITY")

    fun authStatus(authentication: Authentication?): AuthStatusResponse {
        val principal = authentication?.principal as? UserDetails
        val authenticated = authEnabled &&
            authentication != null &&
            authentication.isAuthenticated &&
            principal != null &&
            principal.username != "anonymousUser"
        val user = if (authenticated) appUserRepository.findByUsername(principal!!.username) else null

        return AuthStatusResponse(
            enabled = authEnabled,
            authenticated = authenticated,
            username = if (authenticated) principal?.username else null,
            displayName = user?.displayName,
            role = user?.role
        )
    }

    fun ecosystemTypeLabel(value: String?): String =
        when (value) {
            "FORMICARIUM" -> "Formicarium"
            "FLORARIUM" -> "Florarium"
            "INDOOR_PLANTS" -> "Indoor Plants"
            "DIY_INCUBATOR" -> "DIY Incubator"
            else -> value ?: "Unknown"
        }

    fun statusLabel(value: String?): String =
        when (value) {
            "NEEDS_ATTENTION" -> "Needs Attention"
            "NO_RECENT_DATA" -> "No Recent Data"
            "STABLE" -> "Stable"
            else -> value ?: "Unknown"
        }

    fun statusBadgeClass(value: String?): String =
        when (value) {
            "NEEDS_ATTENTION" -> "status-pill status-warning"
            "STABLE" -> "status-pill status-stable"
            else -> "status-pill status-neutral"
        }

    fun taskStatusBadgeClass(value: String?): String =
        when (value) {
            "DONE" -> "badge text-bg-success"
            "DISMISSED" -> "badge text-bg-secondary"
            else -> "badge text-bg-warning text-dark"
        }

    fun taskStatusLabel(value: String?): String =
        when (value) {
            "DONE" -> "Done"
            "DISMISSED" -> "Dismissed"
            else -> "Open"
        }

    fun eventTypeLabel(value: String?): String =
        when (value) {
            "OBSERVATION" -> "Observation"
            "FEEDING" -> "Feeding"
            "WATERING" -> "Watering"
            else -> value ?: "Unknown"
        }

    fun taskTypeLabel(value: String?): String =
        when (value) {
            "WATERING" -> "Watering"
            "FEEDING" -> "Feeding"
            "CLEANING" -> "Cleaning"
            "INSPECTION" -> "Inspection"
            else -> value ?: "Unknown"
        }

    fun taskTypeBadgeClass(value: String?): String =
        when (value) {
            "WATERING" -> "badge badge-eco badge-eco-watering"
            "FEEDING" -> "badge badge-eco badge-eco-feeding"
            "INSPECTION" -> "badge badge-eco badge-eco-inspection"
            "CLEANING" -> "badge badge-eco badge-eco-cleaning"
            else -> "badge text-bg-light border"
        }

    fun taskSourceBadgeClass(autoCreated: Boolean): String =
        if (autoCreated) "badge badge-eco badge-eco-suggested" else "badge badge-eco badge-eco-manual"

    fun automationRuleStatusLabel(enabled: Boolean): String = if (enabled) "Active" else "Disabled"

    fun automationRuleStatusBadgeClass(enabled: Boolean): String =
        if (enabled) "badge rounded-pill text-bg-success" else "badge rounded-pill text-bg-secondary"

    fun automationRuleScopeLabel(value: String?): String =
        when (value) {
            "ALL_ECOSYSTEMS" -> "All ecosystems"
            "ECOSYSTEM_TYPE" -> "By ecosystem type"
            else -> value ?: "Unknown"
        }

    fun automationRuleTriggerLabel(value: String?): String =
        when (value) {
            "AFTER_EVENT" -> "After event"
            "AFTER_INACTIVITY" -> "After inactivity"
            else -> value ?: "Unknown"
        }

    fun eventTypeBadgeClass(value: String?): String =
        when (value) {
            "OBSERVATION" -> "badge badge-eco badge-eco-observation"
            "FEEDING" -> "badge badge-eco badge-eco-feeding"
            "WATERING" -> "badge badge-eco badge-eco-watering"
            else -> "badge text-bg-light border"
        }

    fun dismissalReasonLabel(value: String?): String =
        when (value) {
            "TOO_SOON" -> "Too soon"
            "NOT_RELEVANT" -> "Not relevant"
            "ALREADY_HANDLED" -> "Already handled"
            else -> value ?: "None"
        }

    fun roleBadgeClass(value: String?): String =
        when (value) {
            "SUPER_ADMIN" -> "badge rounded-pill text-bg-danger"
            "ADMIN" -> "badge rounded-pill text-bg-success"
            else -> "badge rounded-pill text-bg-secondary"
        }

    fun formatDateTime(value: LocalDateTime?): String = value?.format(dateTimeFormatter) ?: "No data yet"

    fun formatDate(value: LocalDate?): String = value?.format(dateFormatter) ?: "No due date"

    fun freshnessLabel(lastRecordedAt: LocalDateTime?): String {
        if (lastRecordedAt == null) {
            return "No observations recorded yet"
        }

        val ageDays = daysSince(lastRecordedAt)
        return if (ageDays >= 7) {
            "Stale activity: last updated $ageDays day${plural(ageDays)} ago"
        } else {
            "Fresh activity: updated ${if (ageDays == 0L) "today" else "$ageDays day${plural(ageDays)} ago"}"
        }
    }

    fun freshnessClass(lastRecordedAt: LocalDateTime?): String {
        if (lastRecordedAt == null) {
            return "freshness-empty"
        }
        return if (daysSince(lastRecordedAt) >= 7) "freshness-stale" else "freshness-fresh"
    }

    fun createdByLabel(displayName: String?, username: String?): String =
        when {
            !displayName.isNullOrBlank() && !username.isNullOrBlank() -> "$displayName (@$username)"
            !displayName.isNullOrBlank() -> displayName
            !username.isNullOrBlank() -> "@$username"
            else -> "Unknown"
        }

    fun isTaskOverdue(task: MaintenanceTaskResponse): Boolean =
        task.status == "OPEN" && task.dueDate != null && task.dueDate.isBefore(LocalDate.now())

    fun canEditTask(task: MaintenanceTaskResponse): Boolean = !task.autoCreated

    private fun daysSince(value: LocalDateTime): Long =
        java.time.Duration.between(value, LocalDateTime.now()).toDays().coerceAtLeast(0)

    private fun plural(value: Long): String = if (value == 1L) "" else "s"
}
