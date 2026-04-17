package com.example.api.dto

import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDateTime
import java.util.UUID

/**
 * Represents the API view of a tracked ecosystem.
 */
@Schema(description = "API representation of a tracked ecosystem.")
data class EcosystemResponse(
    @field:Schema(description = "Unique ecosystem identifier.", example = "2a5ab0f5-8a81-44ba-a8f6-f2862b4a7c0d")
    val id: UUID?,

    @field:Schema(description = "Display name for the ecosystem.", example = "Rainforest Capsule")
    val name: String,

    @field:Schema(
        description = "High-level ecosystem type.",
        example = "FLORARIUM",
        allowableValues = ["FORMICARIUM", "FLORARIUM", "INDOOR_PLANTS", "DIY_INCUBATOR"]
    )
    val type: String,

    @field:Schema(description = "Optional free-text description.", example = "High humidity setup for moss and ferns")
    val description: String?,

    @field:Schema(description = "Login of the user who created the ecosystem, if known.", example = "demo-user")
    val createdByUsername: String?,

    @field:Schema(description = "Display name of the user who created the ecosystem, if known.", example = "Demo Gardener")
    val createdByDisplayName: String?,

    @field:Schema(description = "Timestamp when the ecosystem was created, if available.", example = "2026-04-07T14:30:00")
    val createdAt: LocalDateTime?
)
