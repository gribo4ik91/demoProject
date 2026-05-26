package com.example.api.dto

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size

/**
 * Carries the data required to update an existing tracked ecosystem.
 */
@Schema(description = "Payload used to update an existing tracked ecosystem.")
data class UpdateEcosystemRequest(
    @field:Schema(
        description = "Display name for the ecosystem.",
        example = "Rainforest Capsule"
    )
    @field:NotBlank(message = "Ecosystem name must not be blank")
    @field:Size(max = 100, message = "Ecosystem name must be 100 characters or fewer")
    @field:Pattern(
        regexp = ValidationPatterns.ECOSYSTEM_NAME,
        message = "Ecosystem name may contain letters, numbers, spaces, apostrophes, dots, underscores, parentheses, and hyphens"
    )
    val name: String,

    @field:Schema(
        description = "High-level ecosystem type.",
        example = "FLORARIUM",
        allowableValues = ["FORMICARIUM", "FLORARIUM", "INDOOR_PLANTS", "DIY_INCUBATOR"]
    )
    @field:NotBlank(message = "Ecosystem type must not be blank")
    @field:Size(max = 50, message = "Ecosystem type must be 50 characters or fewer")
    @field:Pattern(
        regexp = ValidationPatterns.ECOSYSTEM_TYPE,
        message = "Ecosystem type must be one of FORMICARIUM, FLORARIUM, INDOOR_PLANTS, or DIY_INCUBATOR"
    )
    val type: String,

    @field:Schema(
        description = "Free-text description shown in the dashboard.",
        example = "High humidity setup for moss and ferns"
    )
    @field:NotBlank(message = "Description must not be blank")
    @field:Size(max = 500, message = "Description must be 500 characters or fewer")
    val description: String? = null
)
