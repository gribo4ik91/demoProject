package com.example.api.controller

import com.example.api.dto.CreateEcosystemRequest
import com.example.api.dto.EcosystemResponse
import com.example.api.dto.EcosystemSummaryResponse
import com.example.api.dto.UpdateEcosystemRequest
import com.example.api.exception.ApiErrorResponse
import com.example.api.service.EcosystemService
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
 * Exposes REST endpoints for creating, listing, viewing, summarizing, and deleting ecosystems.
 */
@RestController
@RequestMapping("/api/v1/ecosystems")
@Tag(name = "Ecosystems", description = "Create, view, and delete tracked ecosystems.")
class EcosystemController(
    private val ecosystemService: EcosystemService
) {

    /**
     * Creates a new ecosystem from the request payload.
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create an ecosystem", description = "Creates a new ecosystem entry that can later receive activity logs.")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "201", description = "Ecosystem created"),
            ApiResponse(
                responseCode = "400",
                description = "Request validation failed",
                content = [Content(schema = Schema(implementation = ApiErrorResponse::class))]
            )
        ]
    )
    fun createEcosystem(@Valid @RequestBody request: CreateEcosystemRequest): EcosystemResponse =
        ecosystemService.createEcosystem(request)

    /**
     * Updates an existing ecosystem from the request payload.
     */
    @PatchMapping("/{id}")
    @Operation(summary = "Update an ecosystem", description = "Updates an existing ecosystem entry.")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Ecosystem updated"),
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
    fun updateEcosystem(
        @Parameter(description = "Ecosystem identifier", example = "2a5ab0f5-8a81-44ba-a8f6-f2862b4a7c0d")
        @PathVariable id: UUID,
        @Valid @RequestBody request: UpdateEcosystemRequest
    ): EcosystemResponse = ecosystemService.updateEcosystem(id, request)

    /**
     * Returns all ecosystems currently tracked by the application.
     */
    @GetMapping
    @Operation(summary = "List ecosystems", description = "Returns all ecosystems currently tracked by the application.")
    @ApiResponse(
        responseCode = "200",
        description = "Ecosystems returned successfully",
        content = [Content(array = ArraySchema(schema = Schema(implementation = EcosystemResponse::class)))]
    )
    fun getAllEcosystems(): List<EcosystemResponse> = ecosystemService.getAllEcosystems()

    /**
     * Returns a single ecosystem identified by its id.
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get ecosystem details", description = "Returns a single ecosystem by its identifier.")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Ecosystem found"),
            ApiResponse(
                responseCode = "404",
                description = "Ecosystem not found",
                content = [Content(schema = Schema(implementation = ApiErrorResponse::class))]
            )
        ]
    )
    fun getEcosystem(
        @Parameter(description = "Ecosystem identifier", example = "2a5ab0f5-8a81-44ba-a8f6-f2862b4a7c0d")
        @PathVariable id: UUID
    ): EcosystemResponse = ecosystemService.getEcosystem(id)

    /**
     * Returns a compact dashboard summary for the selected ecosystem.
     */
    @GetMapping("/{id}/summary")
    @Operation(summary = "Get ecosystem summary", description = "Returns dashboard-oriented status, latest readings, and recent activity trends for one ecosystem.")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Summary returned successfully"),
            ApiResponse(
                responseCode = "404",
                description = "Ecosystem not found",
                content = [Content(schema = Schema(implementation = ApiErrorResponse::class))]
            )
        ]
    )
    fun getEcosystemSummary(
        @Parameter(description = "Ecosystem identifier", example = "2a5ab0f5-8a81-44ba-a8f6-f2862b4a7c0d")
        @PathVariable id: UUID
    ): EcosystemSummaryResponse = ecosystemService.getEcosystemSummary(id)

    /**
     * Deletes the selected ecosystem together with its dependent data.
     */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete an ecosystem", description = "Deletes the ecosystem and its related activity logs.")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "204", description = "Ecosystem deleted"),
            ApiResponse(
                responseCode = "404",
                description = "Ecosystem not found",
                content = [Content(schema = Schema(implementation = ApiErrorResponse::class))]
            )
        ]
    )
    fun deleteEcosystem(
        @Parameter(description = "Ecosystem identifier", example = "2a5ab0f5-8a81-44ba-a8f6-f2862b4a7c0d")
        @PathVariable id: UUID
    ) = ecosystemService.deleteEcosystem(id)
}
