package com.example.api.dto

import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDateTime
import java.util.UUID

/**
 * Represents a single ecosystem log entry returned by the API.
 */
@Schema(description = "API representation of an ecosystem activity log entry.")
data class EcosystemLogResponse(
    @field:Schema(description = "Unique log identifier.", example = "83e5dc0a-6195-43bb-b6de-8341ee076329")
    val id: UUID?,

    @field:Schema(description = "Identifier of the owning ecosystem.", example = "2a5ab0f5-8a81-44ba-a8f6-f2862b4a7c0d")
    val ecosystemId: UUID?,

    @field:Schema(description = "Temperature reading in Celsius.", example = "24.0")
    val temperatureC: Double?,

    @field:Schema(description = "Humidity percentage.", example = "58")
    val humidityPercent: Int?,

    @field:Schema(
        description = "Event category.",
        example = "WATERING",
        allowableValues = ["OBSERVATION", "FEEDING", "WATERING"]
    )
    val eventType: String,

    @field:Schema(description = "Optional note recorded for the event.", example = "After misting")
    val notes: String?,

    @field:Schema(description = "Timestamp when the event was recorded.", example = "2026-04-07T15:00:00")
    val recordedAt: LocalDateTime
)
