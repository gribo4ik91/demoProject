package com.example.api.dto

/**
 * Represents the current authentication state exposed to the frontend.
 */
data class AuthStatusResponse(
    val enabled: Boolean,
    val authenticated: Boolean,
    val username: String?,
    val displayName: String?
)
