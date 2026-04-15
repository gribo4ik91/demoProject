package com.example.api.dto

import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDateTime
import java.util.UUID

/**
 * Represents the compact summary shown on the ecosystem dashboard.
 */
@Schema(description = "Compact dashboard summary for an ecosystem.")
data class EcosystemSummaryResponse(
    @field:Schema(description = "Identifier of the ecosystem.", example = "2a5ab0f5-8a81-44ba-a8f6-f2862b4a7c0d")
    val ecosystemId: UUID,

    @field:Schema(
        description = "High-level operational status derived from recent readings.",
        example = "STABLE",
        allowableValues = ["NO_RECENT_DATA", "STABLE", "NEEDS_ATTENTION"]
    )
    val status: String,

    @field:Schema(description = "Timestamp of the latest recorded activity.", example = "2026-04-07T15:00:00")
    val lastRecordedAt: LocalDateTime?,

    @field:Schema(description = "Type of the latest recorded event.", example = "WATERING")
    val latestEventType: String?,

    @field:Schema(description = "Most recent temperature reading in Celsius.", example = "24.0")
    val currentTemperatureC: Double?,

    @field:Schema(description = "Most recent humidity percentage.", example = "58")
    val currentHumidityPercent: Int?,

    @field:Schema(description = "Average temperature across recent measurable logs.", example = "23.7")
    val averageTemperatureC: Double?,

    @field:Schema(description = "Average humidity across recent measurable logs.", example = "56.5")
    val averageHumidityPercent: Double?,

    @field:Schema(description = "Number of logs recorded in the last 7 days.", example = "4")
    val logsLast7Days: Long,

    @field:Schema(description = "Number of logs recorded in the last 30 days.", example = "12")
    val logsLast30Days: Long,

    @field:Schema(description = "Number of distinct active logging days in the last 30 days.", example = "8")
    val activeDaysLast30Days: Int,

    @field:Schema(description = "Current consecutive-day logging streak based on recorded activity.", example = "3")
    val loggingStreakDays: Int,

    @field:Schema(description = "Temperature delta between the latest measurable window and the previous one.", example = "1.2")
    val temperatureTrendDeltaC: Double?,

    @field:Schema(description = "Humidity delta between the latest measurable window and the previous one.", example = "-4.0")
    val humidityTrendDeltaPercent: Double?,

    @field:Schema(description = "Number of currently open maintenance tasks.", example = "2")
    val openTasks: Long,

    @field:Schema(description = "Number of overdue open maintenance tasks.", example = "1")
    val overdueTasks: Long
)
