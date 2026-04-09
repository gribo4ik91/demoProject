package com.example.api.repository

import com.example.api.model.MaintenanceTask
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.time.LocalDate
import java.util.UUID

/**
 * Provides persistence operations and query helpers for maintenance tasks.
 */
@Repository
interface MaintenanceTaskRepository : JpaRepository<MaintenanceTask, UUID> {
    /**
     * Returns all tasks for one ecosystem using the default dashboard ordering.
     */
    fun findByEcosystemIdOrderByStatusAscDueDateAscCreatedAtDesc(ecosystemId: UUID): List<MaintenanceTask>

    /**
     * Returns tasks for one ecosystem filtered by status.
     */
    fun findByEcosystemIdAndStatusOrderByDueDateAscCreatedAtDesc(ecosystemId: UUID, status: String): List<MaintenanceTask>

    /**
     * Deletes all tasks associated with a specific ecosystem.
     */
    fun deleteByEcosystemId(ecosystemId: UUID)

    /**
     * Checks whether an open task with the same generated signature already exists.
     */
    fun existsByEcosystemIdAndStatusAndTaskTypeAndTitle(
        ecosystemId: UUID,
        status: String,
        taskType: String,
        title: String
    ): Boolean

    /**
     * Returns the most recently dismissed matching suggested task, if any.
     */
    fun findTopByEcosystemIdAndStatusAndTaskTypeAndTitleOrderByCreatedAtDesc(
        ecosystemId: UUID,
        status: String,
        taskType: String,
        title: String
    ): MaintenanceTask?

    /**
     * Returns overdue tasks for one ecosystem and status ordered by due date.
     */
    fun findByEcosystemIdAndStatusAndDueDateBeforeOrderByDueDateAscCreatedAtDesc(
        ecosystemId: UUID,
        status: String,
        dueDate: LocalDate
    ): List<MaintenanceTask>

    /**
     * Counts tasks for one ecosystem and status.
     */
    fun countByEcosystemIdAndStatus(ecosystemId: UUID, status: String): Long

    /**
     * Counts tasks that are overdue for one ecosystem and status.
     */
    fun countByEcosystemIdAndStatusAndDueDateBefore(ecosystemId: UUID, status: String, dueDate: LocalDate): Long
}
