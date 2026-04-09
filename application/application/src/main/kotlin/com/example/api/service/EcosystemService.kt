package com.example.api.service

import com.example.api.dto.CreateEcosystemRequest
import com.example.api.dto.EcosystemResponse
import com.example.api.dto.EcosystemSummaryResponse
import com.example.api.mapper.toEntity
import com.example.api.mapper.toResponse
import com.example.api.repository.EcosystemLogRepository
import com.example.api.repository.MaintenanceTaskRepository
import com.example.api.repository.EcosystemRepository
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

/**
 * Coordinates ecosystem lifecycle operations and dashboard summary calculations.
 */
@Service
class EcosystemService(
    private val ecosystemRepository: EcosystemRepository,
    private val ecosystemLogRepository: EcosystemLogRepository,
    private val maintenanceTaskRepository: MaintenanceTaskRepository
) {

    /**
     * Creates and persists a new ecosystem.
     */
    @Transactional
    fun createEcosystem(request: CreateEcosystemRequest): EcosystemResponse =
        ecosystemRepository.save(request.toEntity()).toResponse()

    /**
     * Returns all stored ecosystems mapped to API responses.
     */
    @Transactional(readOnly = true)
    fun getAllEcosystems(): List<EcosystemResponse> =
        ecosystemRepository.findAll().map { it.toResponse() }

    /**
     * Returns one ecosystem by id or throws a not-found error.
     */
    @Transactional(readOnly = true)
    fun getEcosystem(id: UUID): EcosystemResponse =
        ecosystemRepository.findById(id)
            .orElseThrow { notFound() }
            .toResponse()

    /**
     * Builds a compact dashboard summary from recent logs and task counters.
     */
    @Transactional(readOnly = true)
    fun getEcosystemSummary(id: UUID): EcosystemSummaryResponse {
        if (!ecosystemRepository.existsById(id)) {
            throw notFound()
        }

        val latestLog = ecosystemLogRepository.findTopByEcosystemIdOrderByRecordedAtDesc(id)
        val recentLogs = ecosystemLogRepository.findTop5ByEcosystemIdOrderByRecordedAtDesc(id)
        val logsLast7Days = ecosystemLogRepository.countByEcosystemIdAndRecordedAtAfter(id, LocalDateTime.now().minusDays(7))
        val openTasks = maintenanceTaskRepository.countByEcosystemIdAndStatus(id, "OPEN")
        val overdueTasks = maintenanceTaskRepository.countByEcosystemIdAndStatusAndDueDateBefore(id, "OPEN", LocalDate.now())

        val averageTemperature = recentLogs.mapNotNull { it.temperatureC }.average().takeIf { !it.isNaN() }
        val averageHumidity = recentLogs.mapNotNull { it.humidityPercent?.toDouble() }.average().takeIf { !it.isNaN() }

        return EcosystemSummaryResponse(
            ecosystemId = id,
            status = deriveStatus(latestLog, logsLast7Days, openTasks, overdueTasks),
            lastRecordedAt = latestLog?.recordedAt,
            latestEventType = latestLog?.eventType,
            currentTemperatureC = latestLog?.temperatureC,
            currentHumidityPercent = latestLog?.humidityPercent,
            averageTemperatureC = averageTemperature?.let { roundToSingleDecimal(it) },
            averageHumidityPercent = averageHumidity?.let { roundToSingleDecimal(it) },
            logsLast7Days = logsLast7Days,
            openTasks = openTasks,
            overdueTasks = overdueTasks
        )
    }

    /**
     * Deletes an ecosystem together with its logs and maintenance tasks.
     */
    @Transactional
    fun deleteEcosystem(id: UUID) {
        if (!ecosystemRepository.existsById(id)) {
            throw notFound()
        }

        maintenanceTaskRepository.deleteByEcosystemId(id)
        ecosystemLogRepository.deleteByEcosystemId(id)
        ecosystemRepository.deleteById(id)
    }

    /**
     * Creates the shared not-found exception for missing ecosystems.
     */
    private fun notFound(): ResponseStatusException =
        ResponseStatusException(HttpStatus.NOT_FOUND, "Ecosystem not found")

    /**
     * Derives a high-level dashboard status from recent measurements and task load.
     */
    private fun deriveStatus(
        latestLog: com.example.api.model.EcosystemLog?,
        logsLast7Days: Long,
        openTasks: Long,
        overdueTasks: Long
    ): String {
        val lowHumidity = latestLog?.humidityPercent?.let { it < 35 } ?: false
        val outOfBandTemperature = latestLog?.temperatureC?.let { it < 18 || it > 30 } ?: false
        val heavyOpenQueue = openTasks >= 3

        if (overdueTasks > 0 || lowHumidity || outOfBandTemperature || heavyOpenQueue) {
            return "NEEDS_ATTENTION"
        }

        if (latestLog == null || logsLast7Days == 0L) {
            return "NO_RECENT_DATA"
        }

        return "STABLE"
    }

    /**
     * Rounds numeric summary values to a single decimal place.
     */
    private fun roundToSingleDecimal(value: Double): Double =
        kotlin.math.round(value * 10.0) / 10.0
}
