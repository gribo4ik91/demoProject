package com.example.api.dto

import java.time.LocalDateTime
import java.util.UUID

/**
 * Exposes a single audit trail entry for UI rendering.
 */
data class AuditLogResponse(
    val id: UUID?,
    val entityType: String,
    val entityId: UUID?,
    val entityName: String?,
    val action: String,
    val fieldName: String?,
    val oldValue: String?,
    val newValue: String?,
    val createdByUsername: String?,
    val createdByDisplayName: String?,
    val createdAt: LocalDateTime
)
