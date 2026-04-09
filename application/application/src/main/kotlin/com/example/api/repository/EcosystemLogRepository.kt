package com.example.api.repository

import com.example.api.model.EcosystemLog
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

/**
 * Provides persistence operations and read models for ecosystem log entries.
 */
@Repository
interface EcosystemLogRepository : JpaRepository<EcosystemLog, UUID> {

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
    fun countByEcosystemIdAndRecordedAtAfter(ecosystemId: UUID, recordedAt: java.time.LocalDateTime): Long

    /**
     * Deletes all logs associated with a specific ecosystem.
     */
    @Transactional
    @Modifying
    fun deleteByEcosystemId(ecosystemId: UUID)
}
