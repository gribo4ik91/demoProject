package com.example.api.dto

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import java.time.LocalDate

/**
 * Carries the data required to update an existing manual maintenance reminder.
 */
@Schema(description = "Payload used to update an existing manual maintenance reminder.")
data class UpdateMaintenanceTaskRequest(
    @field:Schema(description = "Short maintenance task title.", example = "Refill water reservoir")
    @field:NotBlank
    @field:Size(max = 120)
    val title: String,

    @field:Schema(
        description = "Task category.",
        example = "WATERING",
        allowableValues = ["WATERING", "FEEDING", "CLEANING", "INSPECTION"]
    )
    @field:NotBlank
    @field:Size(max = 50)
    val taskType: String,

    @field:Schema(description = "Optional due date for the reminder.", example = "2026-04-10")
    val dueDate: LocalDate? = null
)
