package com.example.api.model

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.LocalDateTime
import java.util.UUID

/**
 * JPA entity representing a configurable rule that can generate suggested maintenance tasks.
 */
@Entity
@Table(name = "automation_rules")
data class AutomationRule(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val id: UUID? = null,

    @Column(nullable = false, length = 120)
    val name: String,

    @Column(nullable = false)
    val enabled: Boolean = true,

    @Column(name = "scope_type", nullable = false, length = 40)
    val scopeType: String,

    @Column(name = "ecosystem_type", length = 50)
    val ecosystemType: String? = null,

    @Column(name = "trigger_type", nullable = false, length = 40)
    val triggerType: String,

    @Column(name = "event_type", length = 50)
    val eventType: String? = null,

    @Column(name = "inactivity_days")
    val inactivityDays: Int? = null,

    @Column(name = "delay_days")
    val delayDays: Int? = null,

    @Column(name = "task_title", nullable = false, length = 160)
    val taskTitle: String,

    @Column(name = "task_type", nullable = false, length = 50)
    val taskType: String,

    @Column(name = "prevent_duplicates", nullable = false)
    val preventDuplicates: Boolean = true,

    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "updated_at", nullable = false)
    val updatedAt: LocalDateTime = LocalDateTime.now()
)
