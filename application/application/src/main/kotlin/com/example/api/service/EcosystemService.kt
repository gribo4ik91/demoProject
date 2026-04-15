package com.example.api.service

import com.example.api.dto.CreateEcosystemRequest
import com.example.api.dto.EcosystemResponse
import com.example.api.dto.EcosystemSummaryResponse
import com.example.api.dto.UpdateEcosystemRequest
import com.example.api.mapper.applyTo
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
     * Updates and persists an existing ecosystem.
     */
    @Transactional
    fun updateEcosystem(id: UUID, request: UpdateEcosystemRequest): EcosystemResponse {
        val existing = ecosystemRepository.findById(id)
            .orElseThrow { notFound() }

        return ecosystemRepository.save(request.applyTo(existing)).toResponse()
    }

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
        val allLogs = ecosystemLogRepository.findByEcosystemIdOrderByRecordedAtDesc(id)
        val recentLogs = ecosystemLogRepository.findTop5ByEcosystemIdOrderByRecordedAtDesc(id)
        val logsLast7Days = ecosystemLogRepository.countByEcosystemIdAndRecordedAtAfter(id, LocalDateTime.now().minusDays(7))
        val logsLast30Days = ecosystemLogRepository.countByEcosystemIdAndRecordedAtAfter(id, LocalDateTime.now().minusDays(30))
        val openTasks = maintenanceTaskRepository.countByEcosystemIdAndStatus(id, "OPEN")
        val overdueTasks = maintenanceTaskRepository.countByEcosystemIdAndStatusAndDueDateBefore(id, "OPEN", LocalDate.now())

        val averageTemperature = recentLogs.mapNotNull { it.temperatureC }.average().takeIf { !it.isNaN() }
        val averageHumidity = recentLogs.mapNotNull { it.humidityPercent?.toDouble() }.average().takeIf { !it.isNaN() }
        val activeDaysLast30Days = allLogs
            .asSequence()
            .filter { it.recordedAt.isAfter(LocalDateTime.now().minusDays(30)) }
            .map { it.recordedAt.toLocalDate() }
            .distinct()
            .count()
        val loggingStreakDays = calculateLoggingStreakDays(allLogs)
        val temperatureTrendDelta = calculateWindowedDelta(allLogs.mapNotNull { it.temperatureC })
        val humidityTrendDelta = calculateWindowedDelta(allLogs.mapNotNull { it.humidityPercent?.toDouble() })

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
            logsLast30Days = logsLast30Days,
            activeDaysLast30Days = activeDaysLast30Days,
            loggingStreakDays = loggingStreakDays,
            temperatureTrendDeltaC = temperatureTrendDelta?.let { roundToSingleDecimal(it) },
            humidityTrendDeltaPercent = humidityTrendDelta?.let { roundToSingleDecimal(it) },
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

    /**
     * Calculates a simple trend delta between the latest measurable window and the previous one.
     */
    private fun calculateWindowedDelta(values: List<Double>, windowSize: Int = 3): Double? {
        if (values.size < windowSize * 2) {
            return null
        }

        val currentWindowAverage = values.take(windowSize).average()
        val previousWindowAverage = values.drop(windowSize).take(windowSize).average()
        return currentWindowAverage - previousWindowAverage
    }

    /**
     * Counts the current consecutive-day logging streak based on the latest recorded day.
     */
    private fun calculateLoggingStreakDays(logs: List<com.example.api.model.EcosystemLog>): Int {
        val loggedDays = logs.map { it.recordedAt.toLocalDate() }.toSet()
        val latestDay = logs.firstOrNull()?.recordedAt?.toLocalDate() ?: return 0
        var streak = 0
        var cursor = latestDay

        while (cursor in loggedDays) {
            streak += 1
            cursor = cursor.minusDays(1)
        }

        return streak
    }
}
