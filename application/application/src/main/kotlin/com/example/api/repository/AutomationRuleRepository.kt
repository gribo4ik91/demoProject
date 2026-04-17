package com.example.api.repository

import com.example.api.model.AutomationRule
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

/**
 * Provides persistence operations for configurable suggested-task automation rules.
 */
@Repository
interface AutomationRuleRepository : JpaRepository<AutomationRule, UUID> {
    /**
     * Returns all rules ordered by the latest updates first.
     */
    fun findAllByOrderByUpdatedAtDescCreatedAtDesc(): List<AutomationRule>

    /**
     * Returns enabled event-based rules for one event type ordered by most recent updates.
     */
    fun findByEnabledTrueAndTriggerTypeAndEventTypeOrderByUpdatedAtDescCreatedAtDesc(
        triggerType: String,
        eventType: String
    ): List<AutomationRule>
}
