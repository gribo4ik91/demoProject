package com.example.api.dto

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size

/**
 * Carries the data required to change the status of a maintenance task.
 */
@Schema(description = "Payload used to update the status of a maintenance task.")
data class UpdateMaintenanceTaskStatusRequest(
    @field:Schema(
        description = "New status for the task.",
        example = "DONE",
        allowableValues = ["OPEN", "DONE", "DISMISSED"]
    )
    @field:NotBlank(message = "Task status must not be blank")
    @field:Size(max = 20, message = "Task status must be 20 characters or fewer")
    @field:Pattern(
        regexp = ValidationPatterns.TASK_STATUS,
        message = "Task status must be one of OPEN, DONE, or DISMISSED"
    )
    val status: String,

    @field:Schema(
        description = "Optional reason for dismissing an auto-generated suggestion.",
        example = "ALREADY_HANDLED",
        allowableValues = ["TOO_SOON", "NOT_RELEVANT", "ALREADY_HANDLED"]
    )
    @field:Size(max = 40, message = "Dismissal reason must be 40 characters or fewer")
    @field:Pattern(
        regexp = ValidationPatterns.DISMISSAL_REASON_OPTIONAL,
        message = "Dismissal reason must be one of TOO_SOON, NOT_RELEVANT, or ALREADY_HANDLED"
    )
    val dismissalReason: String? = null
)
