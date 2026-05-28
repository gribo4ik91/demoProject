package com.example.api.model

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.LocalDateTime
import java.util.UUID

object AuditActions {
    const val CREATED = "CREATED"
    const val UPDATED = "UPDATED"
    const val DELETED = "DELETED"
}

object AuditEntityTypes {
    const val ECOSYSTEM = "ECOSYSTEM"
    const val LOG = "LOG"
    const val TASK = "TASK"
    const val AUTOMATION_RULE = "AUTOMATION_RULE"
}

/**
 * JPA entity representing a user-visible change made to inventory data.
 */
@Entity
@Table(name = "audit_logs")
data class AuditLog(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val id: UUID? = null,

    @Column(name = "entity_type", nullable = false, length = 40)
    val entityType: String,

    @Column(name = "entity_id")
    val entityId: UUID? = null,

    @Column(name = "entity_name", length = 160)
    val entityName: String? = null,

    @Column(nullable = false, length = 40)
    val action: String,

    @Column(name = "field_name", length = 80)
    val fieldName: String? = null,

    @Column(name = "old_value", columnDefinition = "TEXT")
    val oldValue: String? = null,

    @Column(name = "new_value", columnDefinition = "TEXT")
    val newValue: String? = null,

    @Column(name = "created_by_username", length = 40)
    val createdByUsername: String? = null,

    @Column(name = "created_by_display_name", length = 60)
    val createdByDisplayName: String? = null,

    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: LocalDateTime = LocalDateTime.now()
)
