package com.example.api.repository

import com.example.api.model.EcosystemLog
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.util.UUID

/**
 * Provides persistence operations and read models for ecosystem log entries.
 */
@Repository
interface EcosystemLogRepository : JpaRepository<EcosystemLog, UUID> {
    /**
     * Returns one log by id for the selected ecosystem, if present.
     */
    fun findByIdAndEcosystemId(id: UUID, ecosystemId: UUID): EcosystemLog?

    /**
     * Returns all logs for one ecosystem ordered from newest to oldest.
     */
    fun findByEcosystemIdOrderByRecordedAtDesc(ecosystemId: UUID): List<EcosystemLog>

    /**
     * Returns a page of logs for one ecosystem.
     */
    fun findByEcosystemId(ecosystemId: UUID, pageable: Pageable): Page<EcosystemLog>

    /**
     * Returns a page of logs for one ecosystem filtered by event type.
     */
    fun findByEcosystemIdAndEventType(ecosystemId: UUID, eventType: String, pageable: Pageable): Page<EcosystemLog>

    /**
     * Returns the five most recent logs for one ecosystem.
     */
    fun findTop5ByEcosystemIdOrderByRecordedAtDesc(ecosystemId: UUID): List<EcosystemLog>

    /**
     * Returns the most recent log for one ecosystem, if available.
     */
    fun findTopByEcosystemIdOrderByRecordedAtDesc(ecosystemId: UUID): EcosystemLog?

    /**
     * Counts logs recorded after the supplied timestamp for one ecosystem.
     */
    fun countByEcosystemIdAndRecordedAtAfter(ecosystemId: UUID, recordedAt: LocalDateTime): Long

    /**
     * Returns the latest log snapshot for each ecosystem.
     */
    @Query(
        value = """
            SELECT DISTINCT ON (ecosystem_id)
                ecosystem_id AS ecosystemId,
                recorded_at AS lastRecordedAt,
                temperature_c AS temperatureC,
                humidity_percent AS humidityPercent
            FROM logs
            ORDER BY ecosystem_id, recorded_at DESC
        """,
        nativeQuery = true
    )
    fun findLatestLogSnapshots(): List<EcosystemLatestLogView>

    /**
     * Returns recent log counts grouped by ecosystem after the supplied timestamp.
     */
    @Query(
        value = """
            SELECT
                ecosystem_id AS ecosystemId,
                COUNT(*) AS logsLast7Days
            FROM logs
            WHERE recorded_at > :recordedAfter
            GROUP BY ecosystem_id
        """,
        nativeQuery = true
    )
    fun countLogsByEcosystemRecordedAfter(recordedAfter: LocalDateTime): List<EcosystemRecentLogCountView>

    /**
     * Deletes all logs associated with a specific ecosystem.
     */
    @Transactional
    @Modifying
    fun deleteByEcosystemId(ecosystemId: UUID)
}

/**
 * Projection for the latest known log snapshot per ecosystem.
 */
interface EcosystemLatestLogView {
    /**
     * Returns the ecosystem identifier for the projected row.
     */
    fun getEcosystemId(): UUID

    /**
     * Returns when the latest projected log was recorded.
     */
    fun getLastRecordedAt(): LocalDateTime

    /**
     * Returns the latest projected temperature reading, if available.
     */
    fun getTemperatureC(): Double?

    /**
     * Returns the latest projected humidity reading, if available.
     */
    fun getHumidityPercent(): Int?
}

/**
 * Projection for recent log counts grouped by ecosystem.
 */
interface EcosystemRecentLogCountView {
    /**
     * Returns the ecosystem identifier for the grouped counter row.
     */
    fun getEcosystemId(): UUID

    /**
     * Returns the number of logs recorded within the recent counting window.
     */
    fun getLogsLast7Days(): Long
}
