package com.example.api.dto

import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDateTime
import java.util.UUID

/**
 * Represents the enriched ecosystem card shown on the workspace home page.
 */
@Schema(description = "Enriched workspace card for an ecosystem.")
data class EcosystemWorkspaceCardResponse(
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

    @field:Schema(description = "High-level operational status derived from recent readings.", example = "STABLE")
    val status: String,

    @field:Schema(description = "Timestamp of the latest recorded activity.", example = "2026-04-07T15:00:00")
    val lastRecordedAt: LocalDateTime?,

    @field:Schema(description = "Number of logs recorded in the last 7 days.", example = "4")
    val logsLast7Days: Long,

    @field:Schema(description = "Number of currently open maintenance tasks.", example = "2")
    val openTasks: Long,

    @field:Schema(description = "Number of overdue open maintenance tasks.", example = "1")
    val overdueTasks: Long,

    @field:Schema(description = "Timestamp when the ecosystem was created.", example = "2026-04-07T14:30:00")
    val createdAt: LocalDateTime
)
