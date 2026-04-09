package com.example.api.exception

import io.swagger.v3.oas.annotations.media.Schema
import java.time.OffsetDateTime

/**
 * Represents the standard JSON error body returned by the API.
 */
@Schema(description = "Standard API error response.")
data class ApiErrorResponse(
    @field:Schema(description = "Timestamp when the error response was generated.", example = "2026-04-07T14:40:00+03:00")
    val timestamp: OffsetDateTime,

    @field:Schema(description = "HTTP status code.", example = "400")
    val status: Int,

    @field:Schema(description = "HTTP reason phrase.", example = "Bad Request")
    val error: String,

    @field:Schema(description = "Human-readable summary of the problem.", example = "Validation failed")
    val message: String,

    @field:Schema(description = "Request path that produced the error.", example = "/api/v1/ecosystems")
    val path: String,

    @field:Schema(description = "Field-level validation errors when the request payload is invalid.")
    val validationErrors: Map<String, String> = emptyMap()
)
