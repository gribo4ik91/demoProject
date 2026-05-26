package com.example.api.dto

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size
import java.time.LocalDate

/**
 * Carries the data required to create a manual maintenance reminder.
 */
@Schema(description = "Payload used to create a new maintenance reminder for an ecosystem.")
data class CreateMaintenanceTaskRequest(
    @field:Schema(description = "Short maintenance task title.", example = "Refill water reservoir")
    @field:NotBlank(message = "Task title must not be blank")
    @field:Size(max = 120, message = "Task title must be 120 characters or fewer")
    @field:Pattern(
        regexp = ValidationPatterns.TASK_TITLE,
        message = "Task title may contain letters, numbers, spaces, and basic punctuation"
    )
    val title: String,

    @field:Schema(
        description = "Task category.",
        example = "WATERING",
        allowableValues = ["WATERING", "FEEDING", "CLEANING", "INSPECTION"]
    )
    @field:NotBlank(message = "Task type must not be blank")
    @field:Size(max = 50, message = "Task type must be 50 characters or fewer")
    @field:Pattern(
        regexp = ValidationPatterns.TASK_TYPE,
        message = "Task type must be one of WATERING, FEEDING, CLEANING, or INSPECTION"
    )
    val taskType: String,

    @field:Schema(description = "Optional due date for the reminder.", example = "2026-04-10")
    val dueDate: LocalDate? = null
)
