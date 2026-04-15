package com.example.api.dto

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

/**
 * Carries the data required to update an existing ecosystem log entry.
 */
@Schema(description = "Payload used to update an observation or activity event for an ecosystem.")
data class UpdateLogRequest(
    @field:Schema(
        description = "Temperature reading in Celsius at the time of the event.",
        example = "23.4"
    )
    @field:Min(-100)
    @field:Max(100)
    val temperatureC: Double?,

    @field:Schema(
        description = "Humidity percentage for the environment.",
        example = "58"
    )
    @field:Min(0)
    @field:Max(100)
    val humidityPercent: Int?,

    @field:Schema(
        description = "Event category for the recorded activity.",
        example = "WATERING",
        allowableValues = ["OBSERVATION", "FEEDING", "WATERING"]
    )
    @field:NotBlank
    @field:Size(max = 50)
    val eventType: String,

    @field:Schema(
        description = "Optional human-readable note for the activity.",
        example = "Misted the terrarium walls after a dry reading."
    )
    @field:Size(max = 500)
    val notes: String?
)
