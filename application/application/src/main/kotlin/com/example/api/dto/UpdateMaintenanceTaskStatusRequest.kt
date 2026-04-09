package com.example.api.dto

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank
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
    @field:NotBlank
    @field:Size(max = 20)
    val status: String,

    @field:Schema(
        description = "Optional reason for dismissing an auto-generated suggestion.",
        example = "ALREADY_HANDLED",
        allowableValues = ["TOO_SOON", "NOT_RELEVANT", "ALREADY_HANDLED"]
    )
    @field:Size(max = 40)
    val dismissalReason: String? = null
)
