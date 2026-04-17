package com.example.api.dto

import io.swagger.v3.oas.annotations.media.Schema

/**
 * Carries the data required to enable or disable an automation rule.
 */
@Schema(description = "Payload used to enable or disable an automation rule.")
data class UpdateAutomationRuleEnabledRequest(
    @field:Schema(description = "Whether the rule should be enabled.", example = "true")
    val enabled: Boolean
)
