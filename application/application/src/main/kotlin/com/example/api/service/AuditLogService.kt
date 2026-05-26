package com.example.api.service

import com.example.api.dto.AuditLogResponse
import com.example.api.dto.PagedResponse
import com.example.api.model.AuditActions
import com.example.api.model.AuditLog
import com.example.api.repository.AuditLogRepository
import com.example.api.service.AuthService.ActorSnapshot
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

data class AuditFieldChange(
    val fieldName: String,
    val oldValue: Any?,
    val newValue: Any?
)

/**
 * Records and reads the user-visible inventory audit trail.
 */
@Service
class AuditLogService(
    private val auditLogRepository: AuditLogRepository
) {
    /**
     * Records that an inventory object was created.
     */
    @Transactional
    fun recordCreated(
        entityType: String,
        entityId: UUID?,
        entityName: String?,
        newValue: String?,
        actor: ActorSnapshot
    ) {
        saveEntry(
            entityType = entityType,
            entityId = entityId,
            entityName = entityName,
            action = AuditActions.CREATED,
            fieldName = null,
            oldValue = null,
            newValue = newValue,
            actor = actor
        )
    }

    /**
     * Records the fields that changed during an update.
     */
    @Transactional
    fun recordUpdated(
        entityType: String,
        entityId: UUID?,
        entityName: String?,
        changes: List<AuditFieldChange>,
        actor: ActorSnapshot
    ) {
        changes
            .filter { it.oldValue != it.newValue }
            .forEach { change ->
                saveEntry(
                    entityType = entityType,
                    entityId = entityId,
                    entityName = entityName,
                    action = AuditActions.UPDATED,
                    fieldName = change.fieldName,
                    oldValue = change.oldValue?.toString(),
                    newValue = change.newValue?.toString(),
                    actor = actor
                )
            }
    }

    /**
     * Records that an inventory object was deleted.
     */
    @Transactional
    fun recordDeleted(
        entityType: String,
        entityId: UUID?,
        entityName: String?,
        oldValue: String?,
        actor: ActorSnapshot
    ) {
        saveEntry(
            entityType = entityType,
            entityId = entityId,
            entityName = entityName,
            action = AuditActions.DELETED,
            fieldName = null,
            oldValue = oldValue,
            newValue = null,
            actor = actor
        )
    }

    /**
     * Returns paginated audit entries for the home page.
     */
    @Transactional(readOnly = true)
    fun getAuditLogs(page: Int = 0, size: Int = 8): PagedResponse<AuditLogResponse> {
        val safePage = page.coerceAtLeast(0)
        val safeSize = size.coerceIn(1, 20)
        val pageable = PageRequest.of(
            safePage,
            safeSize,
            Sort.by(Sort.Direction.DESC, "createdAt").and(Sort.by(Sort.Direction.DESC, "id"))
        )
        val resultPage = auditLogRepository.findAll(pageable)

        return PagedResponse(
            page = resultPage.number,
            size = resultPage.size,
            totalElements = resultPage.totalElements,
            totalPages = resultPage.totalPages,
            hasNext = resultPage.hasNext(),
            hasPrevious = resultPage.hasPrevious(),
            items = resultPage.content.map { it.toResponse() }
        )
    }

    private fun saveEntry(
        entityType: String,
        entityId: UUID?,
        entityName: String?,
        action: String,
        fieldName: String?,
        oldValue: String?,
        newValue: String?,
        actor: ActorSnapshot
    ) {
        auditLogRepository.save(
            AuditLog(
                entityType = entityType,
                entityId = entityId,
                entityName = entityName,
                action = action,
                fieldName = fieldName,
                oldValue = oldValue,
                newValue = newValue,
                createdByUsername = actor.username,
                createdByDisplayName = actor.displayName
            )
        )
    }

    private fun AuditLog.toResponse(): AuditLogResponse =
        AuditLogResponse(
            id = id,
            entityType = entityType,
            entityId = entityId,
            entityName = entityName,
            action = action,
            fieldName = fieldName,
            oldValue = oldValue,
            newValue = newValue,
            createdByUsername = createdByUsername,
            createdByDisplayName = createdByDisplayName,
            createdAt = createdAt
        )
}
