package com.example.api.repository

import com.example.api.model.MaintenanceTask
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.time.LocalDate
import java.util.UUID

/**
 * Provides persistence operations and query helpers for maintenance tasks.
 */
@Repository
interface MaintenanceTaskRepository : JpaRepository<MaintenanceTask, UUID> {
    /**
     * Returns one task by id for the selected ecosystem, if present.
     */
    fun findByIdAndEcosystemId(id: UUID, ecosystemId: UUID): MaintenanceTask?

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

    /**
     * Returns open and overdue task counts grouped by ecosystem.
     */
    @Query(
        value = """
            SELECT
                CAST(ecosystem_id AS VARCHAR) AS ecosystemId,
                SUM(CASE WHEN status = 'OPEN' THEN 1 ELSE 0 END) AS openTasks,
                SUM(CASE WHEN status = 'OPEN' AND due_date < :today THEN 1 ELSE 0 END) AS overdueTasks
            FROM maintenance_tasks
            GROUP BY ecosystem_id
        """,
        nativeQuery = true
    )
    fun findTaskCountsByEcosystem(today: LocalDate): List<EcosystemTaskCountView>
}

/**
 * Projection for grouped maintenance task counters per ecosystem.
 */
interface EcosystemTaskCountView {
    /**
     * Returns the ecosystem identifier for the grouped counter row.
     */
    fun getEcosystemId(): String

    /**
     * Returns the number of open tasks for the ecosystem, if the query produced a value.
     */
    fun getOpenTasks(): Long?

    /**
     * Returns the number of overdue open tasks for the ecosystem, if the query produced a value.
     */
    fun getOverdueTasks(): Long?
}
