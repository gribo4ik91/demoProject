package com.example.api.model

import jakarta.persistence.*
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

/**
 * JPA entity representing a manual or auto-generated maintenance reminder.
 */
@Entity
@Table(name = "maintenance_tasks")
data class MaintenanceTask(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val id: UUID? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ecosystem_id", nullable = false)
    val ecosystem: Ecosystem,

    @Column(nullable = false)
    val title: String,

    @Column(name = "task_type", nullable = false)
    val taskType: String,

    @Column(name = "due_date")
    val dueDate: LocalDate? = null,

    @Column(nullable = false)
    val status: String = "OPEN",

    @Column(name = "auto_created", nullable = false)
    val autoCreated: Boolean = false,

    @Column(name = "dismissal_reason")
    val dismissalReason: String? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_user_id")
    val createdByUser: AppUser? = null,

    @Column(name = "created_by_username", length = 40)
    val createdByUsername: String? = null,

    @Column(name = "created_by_display_name", length = 60)
    val createdByDisplayName: String? = null,

    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: LocalDateTime = LocalDateTime.now()
)
