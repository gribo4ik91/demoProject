package com.example.api.controller

import com.example.api.dto.CreateMaintenanceTaskRequest
import com.example.api.dto.MaintenanceTaskResponse
import com.example.api.dto.UpdateMaintenanceTaskRequest
import com.example.api.dto.UpdateMaintenanceTaskStatusRequest
import com.example.api.service.MaintenanceTaskService
import jakarta.validation.Valid
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

/**
 * Implements the maintenance-task endpoint contract and delegates requests to [MaintenanceTaskService].
 */
@RestController
class MaintenanceTaskController(
    private val maintenanceTaskService: MaintenanceTaskService
) : MaintenanceTaskControllerApi {

    /**
     * Creates a manual maintenance task for the selected ecosystem.
     */
    override fun createTask(
        authentication: Authentication?,
        ecosystemId: UUID,
        @Valid request: CreateMaintenanceTaskRequest
    ): MaintenanceTaskResponse = maintenanceTaskService.createTask(authentication?.name, ecosystemId, request)

    /**
     * Updates a manual maintenance task for the selected ecosystem.
     */
    override fun updateTask(
        ecosystemId: UUID,
        taskId: UUID,
        @Valid request: UpdateMaintenanceTaskRequest
    ): MaintenanceTaskResponse = maintenanceTaskService.updateTask(ecosystemId, taskId, request)

    /**
     * Returns maintenance tasks for the selected ecosystem, optionally filtered by status.
     */
    override fun getTasks(
        ecosystemId: UUID,
        filter: String?
    ): List<MaintenanceTaskResponse> = maintenanceTaskService.getTasks(ecosystemId, filter)

    /**
     * Updates the status of an existing maintenance task.
     */
    override fun updateTaskStatus(
        ecosystemId: UUID,
        taskId: UUID,
        @Valid request: UpdateMaintenanceTaskStatusRequest
    ): MaintenanceTaskResponse = maintenanceTaskService.updateTaskStatus(ecosystemId, taskId, request)
}
