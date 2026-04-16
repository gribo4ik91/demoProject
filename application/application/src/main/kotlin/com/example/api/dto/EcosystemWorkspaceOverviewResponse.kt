package com.example.api.dto

import io.swagger.v3.oas.annotations.media.Schema

/**
 * Represents the aggregated workspace overview shown at the top of the home page.
 */
@Schema(description = "Aggregated workspace overview for the home page.")
data class EcosystemWorkspaceOverviewResponse(
    @field:Schema(description = "Total number of ecosystems in the workspace.", example = "8")
    val totalEcosystems: Int,

    @field:Schema(description = "Number of ecosystems currently marked as needing attention.", example = "2")
    val needsAttention: Int,

    @field:Schema(description = "Number of ecosystems currently marked as stable.", example = "4")
    val stable: Int,

    @field:Schema(description = "Number of ecosystems with no recent data.", example = "2")
    val noRecentData: Int,

    @field:Schema(description = "Total number of open tasks across the workspace.", example = "11")
    val openTasks: Long,

    @field:Schema(description = "Total number of overdue tasks across the workspace.", example = "3")
    val overdueTasks: Long
)
