package com.example.api.mapper

import com.example.api.dto.MaintenanceTaskResponse
import com.example.api.model.MaintenanceTask

/**
 * Converts a maintenance task entity into its API response representation.
 */
fun MaintenanceTask.toResponse(): MaintenanceTaskResponse =
    MaintenanceTaskResponse(
        id = id,
        ecosystemId = ecosystem.id,
        title = title,
        taskType = taskType,
        dueDate = dueDate,
        status = status,
        autoCreated = autoCreated,
        dismissalReason = dismissalReason,
        createdAt = createdAt
    )
