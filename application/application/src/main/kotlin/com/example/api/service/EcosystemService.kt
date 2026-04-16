package com.example.api.service

import com.example.api.dto.CreateEcosystemRequest
import com.example.api.dto.EcosystemResponse
import com.example.api.dto.EcosystemSummaryResponse
import com.example.api.dto.EcosystemWorkspaceCardResponse
import com.example.api.dto.EcosystemWorkspaceOverviewResponse
import com.example.api.dto.PagedResponse
import com.example.api.dto.UpdateEcosystemRequest
import com.example.api.mapper.applyTo
import com.example.api.mapper.toEntity
import com.example.api.mapper.toResponse
import com.example.api.model.EcosystemLog
import com.example.api.repository.EcosystemLogRepository
import com.example.api.repository.MaintenanceTaskRepository
import com.example.api.repository.EcosystemRepository
import org.slf4j.LoggerFactory
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

    private val logger = LoggerFactory.getLogger(javaClass)

    /**
     * Creates and persists a new ecosystem.
     */
    @Transactional
    fun createEcosystem(request: CreateEcosystemRequest): EcosystemResponse {
        logger.info("Creating ecosystem name={} type={}", request.name.trim(), request.type.trim())
        return ecosystemRepository.save(request.toEntity()).toResponse()
    }

    /**
     * Updates and persists an existing ecosystem.
     */
    @Transactional
    fun updateEcosystem(id: UUID, request: UpdateEcosystemRequest): EcosystemResponse {
        logger.info("Updating ecosystem id={}", id)
        val existing = ecosystemRepository.findById(id)
            .orElseThrow { notFound() }

        return ecosystemRepository.save(request.applyTo(existing)).toResponse()
    }

    /**
     * Returns all stored ecosystems mapped to API responses.
     */
    @Transactional(readOnly = true)
    fun getAllEcosystems(): List<EcosystemResponse> {
        logger.info("Loading ecosystem list")
        return ecosystemRepository.findAll().map { it.toResponse() }
    }

    /**
     * Returns enriched workspace cards for the home page in one payload.
     */
    @Transactional(readOnly = true)
    fun getWorkspaceCards(
        search: String? = null,
        status: String? = null,
        sort: String? = null,
        page: Int = 0,
        size: Int = 9
    ): PagedResponse<EcosystemWorkspaceCardResponse> {
        logger.info("Loading ecosystem workspace cards search={} status={} sort={} page={} size={}", search, status, sort, page, size)
        val safePage = page.coerceAtLeast(0)
        val safeSize = size.coerceIn(1, 24)
        val filteredCards = getFilteredWorkspaceCards(search = search, status = status, sort = sort)

        return paginateWorkspaceCards(filteredCards, safePage, safeSize)
    }

    /**
     * Builds the shared workspace card snapshot before client-selected filtering.
     */
    private fun buildWorkspaceCardsSnapshot(): List<EcosystemWorkspaceCardResponse> {
        val latestLogsByEcosystem = ecosystemLogRepository.findLatestLogSnapshots()
            .associateBy { it.getEcosystemId() }
        val recentLogCountsByEcosystem = ecosystemLogRepository.countLogsByEcosystemRecordedAfter(LocalDateTime.now().minusDays(7))
            .associate { it.getEcosystemId() to it.getLogsLast7Days() }
        val taskCountsByEcosystem = maintenanceTaskRepository.findTaskCountsByEcosystem(LocalDate.now())
            .associateBy { it.getEcosystemId() }

        return ecosystemRepository.findAll().map { ecosystem ->
            val ecosystemId = ecosystem.id ?: error("Expected generated ecosystem id")
            val latestLog = latestLogsByEcosystem[ecosystemId]
            val logsLast7Days = recentLogCountsByEcosystem[ecosystemId] ?: 0L
            val taskCounts = taskCountsByEcosystem[ecosystemId]
            val openTasks = taskCounts?.getOpenTasks() ?: 0L
            val overdueTasks = taskCounts?.getOverdueTasks() ?: 0L

            EcosystemWorkspaceCardResponse(
                id = ecosystemId,
                name = ecosystem.name,
                type = ecosystem.type,
                description = ecosystem.description,
                status = deriveStatus(
                    latestLog = latestLog?.toSummaryLog(),
                    logsLast7Days = logsLast7Days,
                    openTasks = openTasks,
                    overdueTasks = overdueTasks
                ),
                lastRecordedAt = latestLog?.getLastRecordedAt(),
                logsLast7Days = logsLast7Days,
                openTasks = openTasks,
                overdueTasks = overdueTasks,
                createdAt = ecosystem.createdAt
            )
        }
    }

    /**
     * Returns aggregated workspace counters for the home page overview.
     */
    @Transactional(readOnly = true)
    fun getWorkspaceOverview(search: String? = null, status: String? = null): EcosystemWorkspaceOverviewResponse {
        logger.info("Loading ecosystem workspace overview search={} status={}", search, status)
        val cards = getFilteredWorkspaceCards(search = search, status = status, sort = "PRIORITY")
        return EcosystemWorkspaceOverviewResponse(
            totalEcosystems = cards.size,
            needsAttention = cards.count { it.status == "NEEDS_ATTENTION" },
            stable = cards.count { it.status == "STABLE" },
            noRecentData = cards.count { it.status == "NO_RECENT_DATA" },
            openTasks = cards.sumOf { it.openTasks },
            overdueTasks = cards.sumOf { it.overdueTasks }
        )
    }

    /**
     * Returns all filtered workspace cards before page slicing for shared aggregations.
     */
    private fun getFilteredWorkspaceCards(
        search: String? = null,
        status: String? = null,
        sort: String? = null
    ): List<EcosystemWorkspaceCardResponse> {
        val normalizedSearch = search?.trim()?.lowercase()?.takeIf { it.isNotEmpty() }
        val normalizedStatus = status?.trim()?.uppercase()?.takeIf { it.isNotEmpty() && it != "ALL" }
        val normalizedSort = sort?.trim()?.uppercase()?.takeIf { it.isNotEmpty() } ?: "PRIORITY"

        return buildWorkspaceCardsSnapshot()
            .asSequence()
            .filter { card -> matchesWorkspaceSearch(card, normalizedSearch) }
            .filter { card -> matchesWorkspaceStatus(card, normalizedStatus) }
            .sortedWith(workspaceCardComparator(normalizedSort))
            .toList()
    }

    /**
     * Returns one ecosystem by id or throws a not-found error.
     */
    @Transactional(readOnly = true)
    fun getEcosystem(id: UUID): EcosystemResponse {
        logger.info("Loading ecosystem details id={}", id)
        return ecosystemRepository.findById(id)
            .orElseThrow { notFound() }
            .toResponse()
    }

    /**
     * Builds a compact dashboard summary from recent logs and task counters.
     */
    @Transactional(readOnly = true)
    fun getEcosystemSummary(id: UUID): EcosystemSummaryResponse {
        logger.info("Building ecosystem summary id={}", id)
        if (!ecosystemRepository.existsById(id)) {
            throw notFound()
        }

        return buildSummary(id)
    }

    /**
     * Builds the shared ecosystem summary payload used by detail and workspace views.
     */
    private fun buildSummary(id: UUID): EcosystemSummaryResponse {

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
        logger.info("Deleting ecosystem id={}", id)
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

    /**
     * Converts the latest log projection into the minimal shape used by status derivation.
     */
    private fun com.example.api.repository.EcosystemLatestLogView.toSummaryLog(): EcosystemLog =
        EcosystemLog(
            ecosystem = com.example.api.model.Ecosystem(
                id = getEcosystemId(),
                name = "",
                type = "",
                description = null
            ),
            temperatureC = getTemperatureC(),
            humidityPercent = getHumidityPercent(),
            eventType = "OBSERVATION",
            recordedAt = getLastRecordedAt()
        )

    /**
     * Checks whether a workspace card matches the active search term.
     */
    private fun matchesWorkspaceSearch(card: EcosystemWorkspaceCardResponse, search: String?): Boolean {
        if (search == null) {
            return true
        }

        return listOf(card.name, card.type, card.description.orEmpty())
            .joinToString(" ")
            .lowercase()
            .contains(search)
    }

    /**
     * Checks whether a workspace card matches the requested status filter.
     */
    private fun matchesWorkspaceStatus(card: EcosystemWorkspaceCardResponse, status: String?): Boolean {
        if (status == null) {
            return true
        }

        if (status == "OVERDUE") {
            return card.overdueTasks > 0
        }

        return card.status == status
    }

    /**
     * Builds the card comparator for workspace-level sorting.
     */
    private fun workspaceCardComparator(sort: String): Comparator<EcosystemWorkspaceCardResponse> =
        when (sort) {
            "NAME" -> compareBy<EcosystemWorkspaceCardResponse> { it.name.lowercase() }
            "NEWEST" -> compareByDescending<EcosystemWorkspaceCardResponse> { it.createdAt }
            "LAST_ACTIVITY" -> compareBy<EcosystemWorkspaceCardResponse> { it.lastRecordedAt == null }
                .thenByDescending { it.lastRecordedAt }
            else -> compareBy<EcosystemWorkspaceCardResponse> { workspaceStatusRank(it.status) }
                .thenByDescending { it.overdueTasks }
                .thenByDescending { it.openTasks }
                .thenBy { it.lastRecordedAt == null }
                .thenByDescending { it.lastRecordedAt }
                .thenBy { it.name.lowercase() }
        }

    /**
     * Maps workspace statuses to priority ranks for sorting.
     */
    private fun workspaceStatusRank(status: String): Int =
        when (status) {
            "NEEDS_ATTENTION" -> 0
            "NO_RECENT_DATA" -> 1
            else -> 2
        }

    /**
     * Slices filtered cards into a generic paged API wrapper.
     */
    private fun paginateWorkspaceCards(
        cards: List<EcosystemWorkspaceCardResponse>,
        page: Int,
        size: Int
    ): PagedResponse<EcosystemWorkspaceCardResponse> {
        val fromIndex = (page * size).coerceAtMost(cards.size)
        val toIndex = (fromIndex + size).coerceAtMost(cards.size)
        val totalPages = if (cards.isEmpty()) 0 else ((cards.size + size - 1) / size)

        return PagedResponse(
            page = page,
            size = size,
            totalElements = cards.size.toLong(),
            totalPages = totalPages,
            hasNext = toIndex < cards.size,
            hasPrevious = page > 0 && cards.isNotEmpty(),
            items = cards.subList(fromIndex, toIndex)
        )
    }
}
