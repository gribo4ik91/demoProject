package com.example.api.service

import com.example.api.dto.CreateMaintenanceTaskRequest
import com.example.api.dto.MaintenanceTaskResponse
import com.example.api.dto.UpdateMaintenanceTaskRequest
import com.example.api.dto.UpdateMaintenanceTaskStatusRequest
import com.example.api.mapper.toResponse
import com.example.api.model.AuditEntityTypes
import com.example.api.model.Ecosystem
import com.example.api.model.MaintenanceTask
import com.example.api.repository.EcosystemRepository
import com.example.api.repository.MaintenanceTaskRepository
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

/**
 * Handles creation, filtering, status changes, and suggestion logic for maintenance tasks.
 */
@Service
class MaintenanceTaskService(
    private val maintenanceTaskRepository: MaintenanceTaskRepository,
    private val ecosystemRepository: EcosystemRepository,
    private val authService: AuthService,
    private val automationRuleService: AutomationRuleService,
    private val auditLogService: AuditLogService
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    companion object {
        private const val OPEN_STATUS = "OPEN"
        private const val DONE_STATUS = "DONE"
        private const val DISMISSED_STATUS = "DISMISSED"
        private val ALLOWED_DISMISSAL_REASONS = setOf("TOO_SOON", "NOT_RELEVANT", "ALREADY_HANDLED")
        private val DISMISSAL_COOLDOWNS_DAYS = mapOf(
            "TOO_SOON" to 3L,
            "ALREADY_HANDLED" to 7L,
            "NOT_RELEVANT" to 30L
        )
    }

    /**
     * Creates a manual maintenance task for the selected ecosystem.
     */
    @Transactional
    fun createTask(username: String?, ecosystemId: UUID, request: CreateMaintenanceTaskRequest): MaintenanceTaskResponse {
        logger.info("Creating maintenance task ecosystemId={} title={}", ecosystemId, request.title.trim())
        val ecosystem = ecosystemRepository.findById(ecosystemId)
            .orElseThrow { ecosystemNotFound() }
        val actor = authService.resolveActorSnapshot(username)
        val normalizedTitle = request.title.trim()
        val normalizedTaskType = request.taskType.trim().uppercase()
        ensureUniqueOpenManualTask(ecosystemId, normalizedTitle, normalizedTaskType, request.dueDate)

        val task = MaintenanceTask(
            ecosystem = ecosystem,
            title = normalizedTitle,
            taskType = normalizedTaskType,
            dueDate = request.dueDate,
            autoCreated = false,
            dismissalReason = null,
            createdByUser = actor.user,
            createdByUsername = actor.username,
            createdByDisplayName = actor.displayName
        )

        val saved = maintenanceTaskRepository.save(task)
        auditLogService.recordCreated(
            entityType = AuditEntityTypes.TASK,
            entityId = saved.id,
            entityName = saved.title,
            newValue = "Created manual task ${saved.title} (${saved.taskType}) due ${saved.dueDate ?: "No due date"}",
            actor = actor
        )

        return saved.toResponse()
    }

    /**
     * Updates a manual maintenance task for the selected ecosystem.
     */
    @Transactional
    fun updateTask(
        username: String?,
        ecosystemId: UUID,
        taskId: UUID,
        request: UpdateMaintenanceTaskRequest
    ): MaintenanceTaskResponse {
        logger.info("Updating maintenance task ecosystemId={} taskId={}", ecosystemId, taskId)
        if (!ecosystemRepository.existsById(ecosystemId)) {
            throw ecosystemNotFound()
        }

        val existing = maintenanceTaskRepository.findByIdAndEcosystemId(taskId, ecosystemId)
            ?: throw taskNotFound()

        if (existing.autoCreated) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Suggested tasks cannot be edited manually")
        }
        val actor = authService.resolveActorSnapshot(username)
        val normalizedTitle = request.title.trim()
        val normalizedTaskType = request.taskType.trim().uppercase()
        ensureUniqueOpenManualTask(ecosystemId, normalizedTitle, normalizedTaskType, request.dueDate, taskId)

        val updatedTask = existing.copy(
            title = normalizedTitle,
            taskType = normalizedTaskType,
            dueDate = request.dueDate
        )
        auditLogService.recordUpdated(
            entityType = AuditEntityTypes.TASK,
            entityId = taskId,
            entityName = updatedTask.title,
            changes = listOf(
                AuditFieldChange("title", existing.title, updatedTask.title),
                AuditFieldChange("taskType", existing.taskType, updatedTask.taskType),
                AuditFieldChange("dueDate", existing.dueDate, updatedTask.dueDate)
            ),
            actor = actor
        )

        return maintenanceTaskRepository.save(updatedTask).toResponse()
    }

    /**
     * Returns maintenance tasks for one ecosystem using the requested status filter.
     */
    @Transactional(readOnly = true)
    fun getTasks(ecosystemId: UUID, filter: String?): List<MaintenanceTaskResponse> {
        logger.info("Loading maintenance tasks ecosystemId={} filter={}", ecosystemId, filter ?: "ALL")
        if (!ecosystemRepository.existsById(ecosystemId)) {
            throw ecosystemNotFound()
        }

        val normalizedFilter = filter?.trim()?.uppercase() ?: "ALL"
        val tasks = when (normalizedFilter) {
            "ALL" -> sortTasks(maintenanceTaskRepository.findByEcosystemIdOrderByStatusAscDueDateAscCreatedAtDesc(ecosystemId))
            "OPEN" -> sortTasks(maintenanceTaskRepository.findByEcosystemIdAndStatusOrderByDueDateAscCreatedAtDesc(ecosystemId, OPEN_STATUS))
            "DONE" -> sortTasks(maintenanceTaskRepository.findByEcosystemIdAndStatusOrderByDueDateAscCreatedAtDesc(ecosystemId, DONE_STATUS))
            "DISMISSED" -> sortTasks(
                maintenanceTaskRepository.findByEcosystemIdAndStatusOrderByDueDateAscCreatedAtDesc(
                    ecosystemId,
                    DISMISSED_STATUS
                )
            )
            "OVERDUE" -> maintenanceTaskRepository.findByEcosystemIdAndStatusAndDueDateBeforeOrderByDueDateAscCreatedAtDesc(
                ecosystemId,
                OPEN_STATUS,
                LocalDate.now()
            ).let(::sortTasks)
            else -> throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Unsupported task filter")
        }

        return tasks
            .map { it.toResponse() }
    }

    /**
     * Updates the status of an existing maintenance task and validates dismissal rules.
     */
    @Transactional
    fun updateTaskStatus(
        username: String?,
        ecosystemId: UUID,
        taskId: UUID,
        request: UpdateMaintenanceTaskStatusRequest
    ): MaintenanceTaskResponse {
        val normalizedStatus = request.status.trim().uppercase()
        logger.info(
            "Updating maintenance task status ecosystemId={} taskId={} status={} dismissalReason={}",
            ecosystemId,
            taskId,
            normalizedStatus,
            request.dismissalReason ?: "NONE"
        )
        if (normalizedStatus !in setOf(OPEN_STATUS, DONE_STATUS, DISMISSED_STATUS)) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Unsupported task status")
        }

        val task = maintenanceTaskRepository.findById(taskId)
            .orElseThrow { taskNotFound() }

        if (task.ecosystem.id != ecosystemId) {
            throw taskNotFound()
        }

        if (normalizedStatus == DISMISSED_STATUS && !task.autoCreated) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Only suggested tasks can be dismissed")
        }

        val normalizedDismissalReason = request.dismissalReason?.trim()?.uppercase()?.takeIf { it.isNotEmpty() }

        if (normalizedStatus == DISMISSED_STATUS) {
            if (normalizedDismissalReason == null) {
                throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Dismissal reason is required for dismissed suggestions")
            }

            if (normalizedDismissalReason !in ALLOWED_DISMISSAL_REASONS) {
                throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Unsupported dismissal reason")
            }
        }

        if (normalizedStatus != DISMISSED_STATUS && normalizedDismissalReason != null) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Dismissal reason can only be set when dismissing a suggestion")
        }
        val actor = authService.resolveActorSnapshot(username)
        val updatedTask = task.copy(
            status = normalizedStatus,
            dismissalReason = if (normalizedStatus == DISMISSED_STATUS) normalizedDismissalReason else null
        )
        auditLogService.recordUpdated(
            entityType = AuditEntityTypes.TASK,
            entityId = taskId,
            entityName = task.title,
            changes = listOf(
                AuditFieldChange("status", task.status, updatedTask.status),
                AuditFieldChange("dismissalReason", task.dismissalReason, updatedTask.dismissalReason)
            ),
            actor = actor
        )

        return maintenanceTaskRepository.save(updatedTask).toResponse()
    }

    /**
     * Creates a suggested follow-up task when an activity event matches a suggestion rule.
     */
    @Transactional
    fun createSuggestedTaskIfNeeded(ecosystem: Ecosystem, eventType: String) {
        val ecosystemId = ecosystem.id ?: return
        val rules = automationRuleService.getActiveEventRules(ecosystem.type, eventType)
        if (rules.isEmpty()) {
            return
        }

        rules.forEach { rule ->
            if (rule.preventDuplicates) {
                val duplicateExists = maintenanceTaskRepository.existsByEcosystemIdAndStatusAndTaskTypeAndTitle(
                    ecosystemId = ecosystemId,
                    status = OPEN_STATUS,
                    taskType = rule.taskType,
                    title = rule.taskTitle
                )

                if (duplicateExists) {
                    return@forEach
                }
            }

            val latestDismissedMatch = maintenanceTaskRepository.findTopByEcosystemIdAndStatusAndTaskTypeAndTitleOrderByCreatedAtDesc(
                ecosystemId = ecosystemId,
                status = DISMISSED_STATUS,
                taskType = rule.taskType,
                title = rule.taskTitle
            )

            if (latestDismissedMatch != null && shouldSkipSuggestedTask(latestDismissedMatch, LocalDateTime.now())) {
                logger.info(
                    "Skipping suggested task ecosystemId={} eventType={} ruleId={} reason=cooldown",
                    ecosystemId,
                    eventType,
                    rule.id
                )
                return@forEach
            }

            logger.info(
                "Creating suggested maintenance task ecosystemId={} eventType={} ruleId={}",
                ecosystemId,
                eventType,
                rule.id
            )
            val systemActor = authService.systemActorSnapshot()
            val saved = maintenanceTaskRepository.save(
                MaintenanceTask(
                    ecosystem = ecosystem,
                    title = rule.taskTitle,
                    taskType = rule.taskType,
                    dueDate = LocalDate.now().plusDays((rule.delayDays ?: 0).toLong()),
                    status = OPEN_STATUS,
                    autoCreated = true,
                    dismissalReason = null,
                    createdByUser = null,
                    createdByUsername = systemActor.username,
                    createdByDisplayName = systemActor.displayName
                )
            )
            auditLogService.recordCreated(
                entityType = AuditEntityTypes.TASK,
                entityId = saved.id,
                entityName = saved.title,
                newValue = "Created suggested task ${saved.title} (${saved.taskType})",
                actor = systemActor
            )
        }
    }

    /**
     * Creates the shared not-found exception for missing ecosystems.
     */
    private fun ecosystemNotFound(): ResponseStatusException =
        ResponseStatusException(HttpStatus.NOT_FOUND, "Ecosystem not found")

    /**
     * Creates the shared not-found exception for missing maintenance tasks.
     */
    private fun taskNotFound(): ResponseStatusException =
        ResponseStatusException(HttpStatus.NOT_FOUND, "Maintenance task not found")

    /**
     * Blocks accidental duplicate open manual tasks for the same ecosystem.
     */
    private fun ensureUniqueOpenManualTask(
        ecosystemId: UUID,
        title: String,
        taskType: String,
        dueDate: LocalDate?,
        excludedTaskId: UUID? = null
    ) {
        val duplicateExists = maintenanceTaskRepository.existsDuplicateOpenManualTask(
            ecosystemId = ecosystemId,
            taskType = taskType,
            title = title,
            dueDate = dueDate,
            excludedTaskId = excludedTaskId
        )
        if (duplicateExists) {
            throw ResponseStatusException(HttpStatus.CONFLICT, "Open task already exists for this ecosystem")
        }
    }

    /**
     * Applies the dashboard task ordering used by the UI.
     */
    private fun sortTasks(tasks: List<MaintenanceTask>): List<MaintenanceTask> =
        tasks.sortedWith(
            compareBy<MaintenanceTask> { statusRank(it.status) }
                .thenBy { it.dueDate ?: LocalDate.MAX }
                .thenByDescending { it.createdAt }
        )

    /**
     * Maps task statuses to their sorting priority.
     */
    private fun statusRank(status: String): Int =
        when (status) {
            OPEN_STATUS -> 0
            DONE_STATUS -> 1
            DISMISSED_STATUS -> 2
            else -> 3
        }

    /**
     * Checks whether a dismissed suggestion is still inside its cooldown window.
     */
    private fun shouldSkipSuggestedTask(task: MaintenanceTask, now: LocalDateTime): Boolean {
        val dismissalReason = task.dismissalReason ?: return false
        val cooldownDays = DISMISSAL_COOLDOWNS_DAYS[dismissalReason] ?: return false
        val availableAt = task.createdAt.plusDays(cooldownDays)
        return now.isBefore(availableAt)
    }
}
