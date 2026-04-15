package com.example.api.controller

import com.example.api.dto.CreateMaintenanceTaskRequest
import com.example.api.dto.MaintenanceTaskResponse
import com.example.api.dto.UpdateMaintenanceTaskRequest
import com.example.api.dto.UpdateMaintenanceTaskStatusRequest
import com.example.api.exception.ApiErrorResponse
import com.example.api.service.MaintenanceTaskService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.ArraySchema
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import java.util.UUID

/**
 * Exposes REST endpoints for creating, listing, and updating maintenance tasks for an ecosystem.
 */
@RestController
@RequestMapping("/api/v1/ecosystems/{ecosystemId}/tasks")
@Tag(name = "Maintenance Tasks", description = "Create and manage recurring or one-off ecosystem care reminders.")
class MaintenanceTaskController(
    private val maintenanceTaskService: MaintenanceTaskService
) {

    /**
     * Creates a manual maintenance task for the selected ecosystem.
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create maintenance task", description = "Adds a maintenance reminder for the selected ecosystem.")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "201", description = "Task created"),
            ApiResponse(
                responseCode = "400",
                description = "Request validation failed",
                content = [Content(schema = Schema(implementation = ApiErrorResponse::class))]
            ),
            ApiResponse(
                responseCode = "404",
                description = "Ecosystem not found",
                content = [Content(schema = Schema(implementation = ApiErrorResponse::class))]
            )
        ]
    )
    fun createTask(
        @Parameter(description = "Ecosystem identifier", example = "2a5ab0f5-8a81-44ba-a8f6-f2862b4a7c0d")
        @PathVariable ecosystemId: UUID,
        @Valid @RequestBody request: CreateMaintenanceTaskRequest
    ): MaintenanceTaskResponse = maintenanceTaskService.createTask(ecosystemId, request)

    /**
     * Updates a manual maintenance task for the selected ecosystem.
     */
    @PatchMapping("/{taskId}")
    @Operation(summary = "Update maintenance task", description = "Updates a manual maintenance reminder for the selected ecosystem.")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Task updated"),
            ApiResponse(
                responseCode = "400",
                description = "Request validation failed or task is not editable",
                content = [Content(schema = Schema(implementation = ApiErrorResponse::class))]
            ),
            ApiResponse(
                responseCode = "404",
                description = "Task or ecosystem not found",
                content = [Content(schema = Schema(implementation = ApiErrorResponse::class))]
            )
        ]
    )
    fun updateTask(
        @Parameter(description = "Ecosystem identifier", example = "2a5ab0f5-8a81-44ba-a8f6-f2862b4a7c0d")
        @PathVariable ecosystemId: UUID,
        @Parameter(description = "Maintenance task identifier", example = "7e74cb50-b2c6-4ba4-a4ea-c4ef290d9880")
        @PathVariable taskId: UUID,
        @Valid @RequestBody request: UpdateMaintenanceTaskRequest
    ): MaintenanceTaskResponse = maintenanceTaskService.updateTask(ecosystemId, taskId, request)

    /**
     * Returns maintenance tasks for the selected ecosystem, optionally filtered by status.
     */
    @GetMapping
    @Operation(summary = "List maintenance tasks", description = "Returns maintenance reminders for the ecosystem ordered by status and due date. Supports task-state filtering.")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Tasks returned successfully",
                content = [Content(array = ArraySchema(schema = Schema(implementation = MaintenanceTaskResponse::class)))]
            ),
            ApiResponse(
                responseCode = "400",
                description = "Unsupported filter value",
                content = [Content(schema = Schema(implementation = ApiErrorResponse::class))]
            ),
            ApiResponse(
                responseCode = "404",
                description = "Ecosystem not found",
                content = [Content(schema = Schema(implementation = ApiErrorResponse::class))]
            )
        ]
    )
    fun getTasks(
        @Parameter(description = "Ecosystem identifier", example = "2a5ab0f5-8a81-44ba-a8f6-f2862b4a7c0d")
        @PathVariable ecosystemId: UUID,
        @Parameter(description = "Task filter.", example = "OVERDUE")
        @RequestParam(required = false) filter: String?
    ): List<MaintenanceTaskResponse> = maintenanceTaskService.getTasks(ecosystemId, filter)

    /**
     * Updates the status of an existing maintenance task.
     */
    @PatchMapping("/{taskId}/status")
    @Operation(summary = "Update task status", description = "Marks a maintenance task as open, done, or dismissed.")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Task updated"),
            ApiResponse(
                responseCode = "400",
                description = "Invalid status or transition supplied",
                content = [Content(schema = Schema(implementation = ApiErrorResponse::class))]
            ),
            ApiResponse(
                responseCode = "404",
                description = "Task or ecosystem not found",
                content = [Content(schema = Schema(implementation = ApiErrorResponse::class))]
            )
        ]
    )
    fun updateTaskStatus(
        @Parameter(description = "Ecosystem identifier", example = "2a5ab0f5-8a81-44ba-a8f6-f2862b4a7c0d")
        @PathVariable ecosystemId: UUID,
        @Parameter(description = "Maintenance task identifier", example = "7e74cb50-b2c6-4ba4-a4ea-c4ef290d9880")
        @PathVariable taskId: UUID,
        @Valid @RequestBody request: UpdateMaintenanceTaskStatusRequest
    ): MaintenanceTaskResponse = maintenanceTaskService.updateTaskStatus(ecosystemId, taskId, request)
}
