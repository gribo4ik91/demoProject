package com.example.api.controller

import com.example.api.dto.EcosystemLogResponse
import com.example.api.dto.LogRequest
import com.example.api.dto.PagedResponse
import com.example.api.exception.ApiErrorResponse
import com.example.api.service.EcosystemLogService
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
 * Exposes REST endpoints for recording and browsing activity logs for one ecosystem.
 */
@RestController
@RequestMapping("/api/v1/ecosystems/{ecosystemId}/logs")
@Tag(name = "Ecosystem Logs", description = "Create and review activity logs for a specific ecosystem.")
class EcosystemLogController(
    private val ecosystemLogService: EcosystemLogService
) {

    /**
     * Creates a new log entry for the selected ecosystem.
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Add ecosystem log", description = "Creates a new activity or observation record for the selected ecosystem.")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "201", description = "Log entry created"),
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
    fun addLog(
        @Parameter(description = "Ecosystem identifier", example = "2a5ab0f5-8a81-44ba-a8f6-f2862b4a7c0d")
        @PathVariable ecosystemId: UUID,
        @Valid @RequestBody request: LogRequest
    ): EcosystemLogResponse = ecosystemLogService.addLog(ecosystemId, request)

    /**
     * Returns a paged list of logs, optionally filtered by event type.
     */
    @GetMapping
    @Operation(summary = "List ecosystem logs", description = "Returns paginated activity logs for the selected ecosystem ordered from newest to oldest. Supports filtering by event type.")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Logs returned successfully",
                content = [Content(schema = Schema(implementation = PagedResponse::class))]
            ),
            ApiResponse(
                responseCode = "404",
                description = "Ecosystem not found",
                content = [Content(schema = Schema(implementation = ApiErrorResponse::class))]
            )
        ]
    )
    fun getLogs(
        @Parameter(description = "Ecosystem identifier", example = "2a5ab0f5-8a81-44ba-a8f6-f2862b4a7c0d")
        @PathVariable ecosystemId: UUID,
        @Parameter(description = "Optional event type filter.", example = "WATERING")
        @RequestParam(required = false) eventType: String?,
        @Parameter(description = "Zero-based page index.", example = "0")
        @RequestParam(defaultValue = "0") page: Int,
        @Parameter(description = "Page size between 1 and 50.", example = "5")
        @RequestParam(defaultValue = "5") size: Int
    ): PagedResponse<EcosystemLogResponse> =
        ecosystemLogService.getLogs(ecosystemId, eventType, page, size)
}
