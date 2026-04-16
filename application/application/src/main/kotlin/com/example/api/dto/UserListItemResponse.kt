package com.example.api.dto

import java.time.LocalDateTime
import java.util.UUID

/**
 * Represents one account in the user directory page.
 */
data class UserListItemResponse(
    val id: UUID,
    val displayName: String,
    val username: String,
    val role: String,
    val firstName: String,
    val lastName: String,
    val email: String,
    val location: String?,
    val createdAt: LocalDateTime
)
