package com.example.api.controller

import com.example.api.dto.AutomationRuleResponse
import com.example.api.dto.CreateAutomationRuleRequest
import com.example.api.dto.UpdateAutomationRuleEnabledRequest
import com.example.api.dto.UpdateAutomationRuleRequest
import com.example.api.exception.ApiErrorResponse
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
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import java.util.UUID

@RequestMapping("/api/v1/automation-rules")
@Tag(name = "Automation Rules", description = "Configure when suggested maintenance tasks should be generated.")
/**
 * Declares the HTTP contract for creating, listing, viewing, updating, enabling, and deleting automation rules.
 */
interface AutomationRuleControllerApi {
    /**
     * Creates a new automation rule from the request payload.
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create automation rule", description = "Creates a configurable rule used to generate suggested maintenance tasks.")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "201", description = "Automation rule created"),
            ApiResponse(
                responseCode = "400",
                description = "Request validation failed",
                content = [Content(schema = Schema(implementation = ApiErrorResponse::class))]
            )
        ]
    )
    fun createRule(@Valid @RequestBody request: CreateAutomationRuleRequest): AutomationRuleResponse

    /**
     * Updates an existing automation rule.
     */
    @PatchMapping("/{id}")
    @Operation(summary = "Update automation rule", description = "Updates the configuration of an existing automation rule.")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Automation rule updated"),
            ApiResponse(
                responseCode = "400",
                description = "Request validation failed",
                content = [Content(schema = Schema(implementation = ApiErrorResponse::class))]
            ),
            ApiResponse(
                responseCode = "404",
                description = "Automation rule not found",
                content = [Content(schema = Schema(implementation = ApiErrorResponse::class))]
            )
        ]
    )
    fun updateRule(
        @Parameter(description = "Automation rule identifier", example = "6f6fcb24-bfd7-4ab6-9f9d-1f8a0c4f6401")
        @PathVariable id: UUID,
        @Valid @RequestBody request: UpdateAutomationRuleRequest
    ): AutomationRuleResponse

    /**
     * Returns automation rules, optionally filtered by status and trigger family.
     */
    @GetMapping
    @Operation(summary = "List automation rules", description = "Returns automation rules used to generate suggested maintenance tasks. Supports status and trigger-family filtering.")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Automation rules returned successfully",
                content = [Content(array = ArraySchema(schema = Schema(implementation = AutomationRuleResponse::class)))]
            )
        ]
    )
    fun getRules(
        @Parameter(description = "Status filter.", example = "ACTIVE")
        @RequestParam(required = false) status: String?,
        @Parameter(description = "Trigger-family filter.", example = "AFTER_EVENT")
        @RequestParam(required = false) trigger: String?
    ): List<AutomationRuleResponse>

    /**
     * Returns one automation rule by id.
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get automation rule", description = "Returns one automation rule by its identifier.")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Automation rule found"),
            ApiResponse(
                responseCode = "404",
                description = "Automation rule not found",
                content = [Content(schema = Schema(implementation = ApiErrorResponse::class))]
            )
        ]
    )
    fun getRule(
        @Parameter(description = "Automation rule identifier", example = "6f6fcb24-bfd7-4ab6-9f9d-1f8a0c4f6401")
        @PathVariable id: UUID
    ): AutomationRuleResponse

    /**
     * Enables or disables an existing automation rule.
     */
    @PatchMapping("/{id}/enabled")
    @Operation(summary = "Set automation rule enabled state", description = "Enables or disables an existing automation rule.")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Automation rule state updated"),
            ApiResponse(
                responseCode = "404",
                description = "Automation rule not found",
                content = [Content(schema = Schema(implementation = ApiErrorResponse::class))]
            )
        ]
    )
    fun updateRuleEnabled(
        @Parameter(description = "Automation rule identifier", example = "6f6fcb24-bfd7-4ab6-9f9d-1f8a0c4f6401")
        @PathVariable id: UUID,
        @RequestBody request: UpdateAutomationRuleEnabledRequest
    ): AutomationRuleResponse

    /**
     * Deletes an automation rule.
     */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete automation rule", description = "Deletes an automation rule by its identifier.")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "204", description = "Automation rule deleted"),
            ApiResponse(
                responseCode = "404",
                description = "Automation rule not found",
                content = [Content(schema = Schema(implementation = ApiErrorResponse::class))]
            )
        ]
    )
    fun deleteRule(
        @Parameter(description = "Automation rule identifier", example = "6f6fcb24-bfd7-4ab6-9f9d-1f8a0c4f6401")
        @PathVariable id: UUID
    )
}
