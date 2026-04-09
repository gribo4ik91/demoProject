package com.example.api.service

import com.example.api.dto.EcosystemLogResponse
import com.example.api.dto.LogRequest
import com.example.api.dto.PagedResponse
import com.example.api.mapper.toResponse
import com.example.api.model.EcosystemLog
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import com.example.api.repository.EcosystemLogRepository
import com.example.api.repository.EcosystemRepository
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException
import java.util.UUID

/**
 * Handles creation, retrieval, and pagination of ecosystem activity logs.
 */
@Service
class EcosystemLogService(
    private val ecosystemLogRepository: EcosystemLogRepository,
    private val ecosystemRepository: EcosystemRepository,
    private val maintenanceTaskService: MaintenanceTaskService
) {

    /**
     * Creates a new log entry and triggers any related suggested maintenance tasks.
     */
    @Transactional
    fun addLog(ecosystemId: UUID, request: LogRequest): EcosystemLogResponse {
        val ecosystem = ecosystemRepository.findById(ecosystemId)
            .orElseThrow { notFound() }

        val normalizedEventType = request.eventType.trim().uppercase()

        val newLog = EcosystemLog(
            ecosystem = ecosystem,
            temperatureC = request.temperatureC,
            humidityPercent = request.humidityPercent,
            eventType = normalizedEventType,
            notes = request.notes?.trim()?.takeIf { it.isNotEmpty() }
        )

        val savedLog = ecosystemLogRepository.save(newLog)

        maintenanceTaskService.createSuggestedTaskIfNeeded(ecosystem, normalizedEventType)

        return savedLog.toResponse()
    }

    /**
     * Returns paginated logs for one ecosystem with optional event-type filtering.
     */
    @Transactional(readOnly = true)
    fun getLogs(
        ecosystemId: UUID,
        eventType: String?,
        page: Int,
        size: Int
    ): PagedResponse<EcosystemLogResponse> {
        if (!ecosystemRepository.existsById(ecosystemId)) {
            throw notFound()
        }

        val safePage = page.coerceAtLeast(0)
        val safeSize = size.coerceIn(1, 50)
        val pageable = PageRequest.of(safePage, safeSize, Sort.by(Sort.Direction.DESC, "recordedAt"))
        val normalizedEventType = eventType?.trim()?.takeIf { it.isNotEmpty() }

        val resultPage = if (normalizedEventType == null) {
            ecosystemLogRepository.findByEcosystemId(ecosystemId, pageable)
        } else {
            ecosystemLogRepository.findByEcosystemIdAndEventType(ecosystemId, normalizedEventType, pageable)
        }

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

    /**
     * Creates the shared not-found exception for missing ecosystems.
     */
    private fun notFound(): ResponseStatusException =
        ResponseStatusException(HttpStatus.NOT_FOUND, "Ecosystem not found")
}
