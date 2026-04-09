package com.example.api.dto

import java.time.LocalDateTime
import java.util.UUID

/**
 * Represents the public profile data returned for an authenticated user.
 */
data class AuthUserResponse(
    val id: UUID,
    val displayName: String,
    val username: String,
    val firstName: String,
    val lastName: String,
    val email: String,
    val location: String?,
    val bio: String?,
    val createdAt: LocalDateTime
)
