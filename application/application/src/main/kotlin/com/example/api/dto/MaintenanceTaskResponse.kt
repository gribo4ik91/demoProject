package com.example.api.dto

import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

/**
 * Represents a maintenance task returned by the API.
 */
@Schema(description = "API representation of an ecosystem maintenance task.")
data class MaintenanceTaskResponse(
    @field:Schema(description = "Task identifier.", example = "7e74cb50-b2c6-4ba4-a4ea-c4ef290d9880")
    val id: UUID?,

    @field:Schema(description = "Owning ecosystem identifier.", example = "2a5ab0f5-8a81-44ba-a8f6-f2862b4a7c0d")
    val ecosystemId: UUID?,

    @field:Schema(description = "Short task title.", example = "Refill water reservoir")
    val title: String,

    @field:Schema(
        description = "Task category.",
        example = "WATERING",
        allowableValues = ["WATERING", "FEEDING", "CLEANING", "INSPECTION"]
    )
    val taskType: String,

    @field:Schema(description = "Optional due date.", example = "2026-04-10")
    val dueDate: LocalDate?,

    @field:Schema(description = "Current task status.", example = "OPEN", allowableValues = ["OPEN", "DONE", "DISMISSED"])
    val status: String,

    @field:Schema(description = "Whether the task was generated automatically from an activity event.", example = "true")
    val autoCreated: Boolean,

    @field:Schema(
        description = "Optional reason why an auto-generated suggestion was dismissed.",
        example = "ALREADY_HANDLED",
        allowableValues = ["TOO_SOON", "NOT_RELEVANT", "ALREADY_HANDLED"]
    )
    val dismissalReason: String?,

    @field:Schema(description = "Timestamp when the task was created.", example = "2026-04-07T16:00:00")
    val createdAt: LocalDateTime
)
