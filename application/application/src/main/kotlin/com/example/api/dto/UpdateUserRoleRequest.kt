package com.example.api.dto

import jakarta.validation.constraints.Pattern

/**
 * Requests changing a user's role within the supported hierarchy.
 */
data class UpdateUserRoleRequest(
    @field:Pattern(
        regexp = "SUPER_ADMIN|ADMIN|USER",
        message = "Role must be one of SUPER_ADMIN, ADMIN, or USER"
    )
    val role: String
)
