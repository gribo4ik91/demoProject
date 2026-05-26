package com.example.api.dto

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.DecimalMax
import jakarta.validation.constraints.DecimalMin
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size

/**
 * Carries the data required to record a new ecosystem log entry.
 */
@Schema(description = "Payload used to record an observation or activity event for an ecosystem.")
data class LogRequest(
    @field:Schema(
        description = "Temperature reading in Celsius at the time of the event.",
        example = "23.4"
    )
    @field:DecimalMin(value = "-100.0", message = "Temperature must be at least -100 C")
    @field:DecimalMax(value = "100.0", message = "Temperature must be 100 C or lower")
    val temperatureC: Double?,

    @field:Schema(
        description = "Humidity percentage for the environment.",
        example = "58"
    )
    @field:Min(value = 0, message = "Humidity must be at least 0%")
    @field:Max(value = 100, message = "Humidity must be 100% or lower")
    val humidityPercent: Int?,

    @field:Schema(
        description = "Event category for the recorded activity.",
        example = "WATERING",
        allowableValues = ["OBSERVATION", "FEEDING", "WATERING"]
    )
    @field:NotBlank(message = "Event type must not be blank")
    @field:Size(max = 50, message = "Event type must be 50 characters or fewer")
    @field:Pattern(
        regexp = ValidationPatterns.LOG_EVENT_TYPE,
        message = "Event type must be one of OBSERVATION, FEEDING, or WATERING"
    )
    val eventType: String,

    @field:Schema(
        description = "Optional human-readable note for the activity.",
        example = "Misted the terrarium walls after a dry reading."
    )
    @field:Size(max = 500, message = "Notes must be 500 characters or fewer")
    val notes: String?
)
