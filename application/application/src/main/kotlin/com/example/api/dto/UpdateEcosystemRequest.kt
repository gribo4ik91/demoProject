package com.example.api.dto

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank
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
    @field:NotBlank
    @field:Size(max = 100)
    val name: String,

    @field:Schema(
        description = "High-level ecosystem type.",
        example = "FLORARIUM",
        allowableValues = ["FORMICARIUM", "FLORARIUM", "INDOOR_PLANTS", "DIY_INCUBATOR"]
    )
    @field:NotBlank
    @field:Size(max = 50)
    val type: String,

    @field:Schema(
        description = "Optional free-text description shown in the dashboard.",
        example = "High humidity setup for moss and ferns"
    )
    @field:Size(max = 500)
    val description: String? = null
)
