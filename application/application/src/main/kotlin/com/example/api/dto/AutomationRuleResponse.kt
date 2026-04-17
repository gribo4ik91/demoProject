package com.example.api.dto

import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDateTime
import java.util.UUID

/**
 * Represents a configurable automation rule returned to the UI or API.
 */
@Schema(description = "Configurable rule that can generate suggested maintenance tasks.")
data class AutomationRuleResponse(
    @field:Schema(description = "Rule identifier.")
    val id: UUID?,
    @field:Schema(description = "Display name shown in the rule list.")
    val name: String,
    @field:Schema(description = "Whether the rule is currently enabled.")
    val enabled: Boolean,
    @field:Schema(description = "Rule scope.", allowableValues = ["ALL_ECOSYSTEMS", "ECOSYSTEM_TYPE"])
    val scopeType: String,
    @field:Schema(description = "Optional ecosystem type restriction.")
    val ecosystemType: String?,
    @field:Schema(description = "Rule trigger type.", allowableValues = ["AFTER_EVENT", "AFTER_INACTIVITY"])
    val triggerType: String,
    @field:Schema(description = "Optional event type tied to the rule.")
    val eventType: String?,
    @field:Schema(description = "Number of inactivity days before the rule becomes eligible.")
    val inactivityDays: Int?,
    @field:Schema(description = "Number of days to wait after the triggering event.")
    val delayDays: Int?,
    @field:Schema(description = "Generated suggested task title.")
    val taskTitle: String,
    @field:Schema(description = "Generated task type.")
    val taskType: String,
    @field:Schema(description = "Whether duplicate open tasks should be prevented.")
    val preventDuplicates: Boolean,
    @field:Schema(description = "Creation timestamp.")
    val createdAt: LocalDateTime,
    @field:Schema(description = "Last update timestamp.")
    val updatedAt: LocalDateTime
)
