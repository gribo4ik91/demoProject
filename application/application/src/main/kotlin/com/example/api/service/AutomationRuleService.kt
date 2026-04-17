package com.example.api.service

import com.example.api.dto.AutomationRuleResponse
import com.example.api.dto.CreateAutomationRuleRequest
import com.example.api.dto.UpdateAutomationRuleRequest
import com.example.api.mapper.toResponse
import com.example.api.model.AutomationRule
import com.example.api.repository.AutomationRuleRepository
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException
import java.time.LocalDateTime
import java.util.UUID

/**
 * Handles CRUD operations and filtering for configurable suggested-task automation rules.
 */
@Service
class AutomationRuleService(
    private val automationRuleRepository: AutomationRuleRepository
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    companion object {
        const val SCOPE_ALL_ECOSYSTEMS = "ALL_ECOSYSTEMS"
        const val SCOPE_ECOSYSTEM_TYPE = "ECOSYSTEM_TYPE"
        const val TRIGGER_AFTER_EVENT = "AFTER_EVENT"
        const val TRIGGER_AFTER_INACTIVITY = "AFTER_INACTIVITY"
    }

    private val allowedScopes = setOf(SCOPE_ALL_ECOSYSTEMS, SCOPE_ECOSYSTEM_TYPE)
    private val allowedTriggers = setOf(TRIGGER_AFTER_EVENT, TRIGGER_AFTER_INACTIVITY)
    private val allowedEvents = setOf("OBSERVATION", "FEEDING", "WATERING")
    private val allowedTaskTypes = setOf("WATERING", "FEEDING", "CLEANING", "INSPECTION")
    private val allowedEcosystemTypes = setOf("FORMICARIUM", "FLORARIUM", "INDOOR_PLANTS", "DIY_INCUBATOR")

    /**
     * Returns rules filtered for the list page.
     */
    @Transactional(readOnly = true)
    fun getRules(status: String?, trigger: String?): List<AutomationRuleResponse> {
        val normalizedStatus = normalizeFilter(status, "ALL")
        val normalizedTrigger = normalizeFilter(trigger, "ALL")

        return automationRuleRepository.findAllByOrderByUpdatedAtDescCreatedAtDesc()
            .asSequence()
            .filter { matchesStatus(it, normalizedStatus) }
            .filter { matchesTrigger(it, normalizedTrigger) }
            .map { it.toResponse() }
            .toList()
    }

    /**
     * Returns one rule by id.
     */
    @Transactional(readOnly = true)
    fun getRule(id: UUID): AutomationRuleResponse =
        automationRuleRepository.findById(id)
            .orElseThrow { notFound() }
            .toResponse()

    /**
     * Creates and persists a new rule.
     */
    @Transactional
    fun createRule(request: CreateAutomationRuleRequest): AutomationRuleResponse {
        logger.info("Creating automation rule name={} triggerType={}", request.name.trim(), request.triggerType.trim())
        val now = LocalDateTime.now()
        val normalized = normalizePayload(
            name = request.name,
            enabled = request.enabled,
            scopeType = request.scopeType,
            ecosystemType = request.ecosystemType,
            triggerType = request.triggerType,
            eventType = request.eventType,
            inactivityDays = request.inactivityDays,
            delayDays = request.delayDays,
            taskTitle = request.taskTitle,
            taskType = request.taskType,
            preventDuplicates = request.preventDuplicates
        )

        return automationRuleRepository.save(
            AutomationRule(
                name = normalized.name,
                enabled = normalized.enabled,
                scopeType = normalized.scopeType,
                ecosystemType = normalized.ecosystemType,
                triggerType = normalized.triggerType,
                eventType = normalized.eventType,
                inactivityDays = normalized.inactivityDays,
                delayDays = normalized.delayDays,
                taskTitle = normalized.taskTitle,
                taskType = normalized.taskType,
                preventDuplicates = normalized.preventDuplicates,
                createdAt = now,
                updatedAt = now
            )
        ).toResponse()
    }

    /**
     * Updates an existing rule.
     */
    @Transactional
    fun updateRule(id: UUID, request: UpdateAutomationRuleRequest): AutomationRuleResponse {
        logger.info("Updating automation rule id={}", id)
        val existing = automationRuleRepository.findById(id)
            .orElseThrow { notFound() }
        val normalized = normalizePayload(
            name = request.name,
            enabled = request.enabled,
            scopeType = request.scopeType,
            ecosystemType = request.ecosystemType,
            triggerType = request.triggerType,
            eventType = request.eventType,
            inactivityDays = request.inactivityDays,
            delayDays = request.delayDays,
            taskTitle = request.taskTitle,
            taskType = request.taskType,
            preventDuplicates = request.preventDuplicates
        )

        return automationRuleRepository.save(
            existing.copy(
                name = normalized.name,
                enabled = normalized.enabled,
                scopeType = normalized.scopeType,
                ecosystemType = normalized.ecosystemType,
                triggerType = normalized.triggerType,
                eventType = normalized.eventType,
                inactivityDays = normalized.inactivityDays,
                delayDays = normalized.delayDays,
                taskTitle = normalized.taskTitle,
                taskType = normalized.taskType,
                preventDuplicates = normalized.preventDuplicates,
                updatedAt = LocalDateTime.now()
            )
        ).toResponse()
    }

    /**
     * Enables or disables a rule.
     */
    @Transactional
    fun setRuleEnabled(id: UUID, enabled: Boolean): AutomationRuleResponse {
        val existing = automationRuleRepository.findById(id)
            .orElseThrow { notFound() }

        return automationRuleRepository.save(
            existing.copy(
                enabled = enabled,
                updatedAt = LocalDateTime.now()
            )
        ).toResponse()
    }

    /**
     * Deletes a rule.
     */
    @Transactional
    fun deleteRule(id: UUID) {
        if (!automationRuleRepository.existsById(id)) {
            throw notFound()
        }
        automationRuleRepository.deleteById(id)
    }

    /**
     * Returns active event-based rules relevant for one ecosystem type and activity event.
     */
    @Transactional(readOnly = true)
    fun getActiveEventRules(ecosystemType: String, eventType: String): List<AutomationRuleResponse> =
        automationRuleRepository.findByEnabledTrueAndTriggerTypeAndEventTypeOrderByUpdatedAtDescCreatedAtDesc(
            TRIGGER_AFTER_EVENT,
            eventType.trim().uppercase()
        )
            .filter { rule ->
                rule.scopeType == SCOPE_ALL_ECOSYSTEMS ||
                    (rule.scopeType == SCOPE_ECOSYSTEM_TYPE && rule.ecosystemType == ecosystemType.trim().uppercase())
            }
            .map { it.toResponse() }

    private fun normalizeFilter(value: String?, fallback: String): String =
        value?.trim()?.uppercase()?.takeIf { it.isNotEmpty() } ?: fallback

    private fun matchesStatus(rule: AutomationRule, status: String): Boolean =
        when (status) {
            "ACTIVE" -> rule.enabled
            "DISABLED" -> !rule.enabled
            else -> true
        }

    private fun matchesTrigger(rule: AutomationRule, trigger: String): Boolean =
        trigger == "ALL" || rule.triggerType == trigger

    private fun normalizePayload(
        name: String,
        enabled: Boolean,
        scopeType: String,
        ecosystemType: String?,
        triggerType: String,
        eventType: String?,
        inactivityDays: Int?,
        delayDays: Int?,
        taskTitle: String,
        taskType: String,
        preventDuplicates: Boolean
    ): NormalizedRulePayload {
        val normalizedScope = scopeType.trim().uppercase()
        val normalizedTrigger = triggerType.trim().uppercase()
        val normalizedEvent = eventType?.trim()?.uppercase()?.takeIf { it.isNotEmpty() }
        val normalizedTaskType = taskType.trim().uppercase()
        val normalizedEcosystemType = ecosystemType?.trim()?.uppercase()?.takeIf { it.isNotEmpty() }

        if (normalizedScope !in allowedScopes) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Unsupported rule scope")
        }
        if (normalizedTrigger !in allowedTriggers) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Unsupported trigger type")
        }
        if (normalizedTaskType !in allowedTaskTypes) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Unsupported generated task type")
        }
        if (normalizedScope == SCOPE_ECOSYSTEM_TYPE) {
            if (normalizedEcosystemType == null || normalizedEcosystemType !in allowedEcosystemTypes) {
                throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Valid ecosystem type is required for this scope")
            }
        }
        if (normalizedScope == SCOPE_ALL_ECOSYSTEMS && normalizedEcosystemType != null) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Ecosystem type can only be set for type-specific scope")
        }
        if (normalizedEvent == null || normalizedEvent !in allowedEvents) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Event type is required")
        }

        val normalizedDelayDays = when (normalizedTrigger) {
            TRIGGER_AFTER_EVENT -> delayDays ?: 0
            else -> null
        }
        val normalizedInactivityDays = when (normalizedTrigger) {
            TRIGGER_AFTER_INACTIVITY -> inactivityDays
            else -> null
        }

        if (normalizedTrigger == TRIGGER_AFTER_EVENT && inactivityDays != null) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Inactivity days are only valid for inactivity rules")
        }
        if (normalizedTrigger == TRIGGER_AFTER_INACTIVITY && delayDays != null) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Delay days are only valid for event-based rules")
        }
        if (normalizedTrigger == TRIGGER_AFTER_INACTIVITY && normalizedInactivityDays == null) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Inactivity days are required for inactivity rules")
        }

        return NormalizedRulePayload(
            name = name.trim(),
            enabled = enabled,
            scopeType = normalizedScope,
            ecosystemType = normalizedEcosystemType,
            triggerType = normalizedTrigger,
            eventType = normalizedEvent,
            inactivityDays = normalizedInactivityDays,
            delayDays = normalizedDelayDays,
            taskTitle = taskTitle.trim(),
            taskType = normalizedTaskType,
            preventDuplicates = preventDuplicates
        )
    }

    private fun notFound(): ResponseStatusException =
        ResponseStatusException(HttpStatus.NOT_FOUND, "Automation rule not found")

    private data class NormalizedRulePayload(
        val name: String,
        val enabled: Boolean,
        val scopeType: String,
        val ecosystemType: String?,
        val triggerType: String,
        val eventType: String,
        val inactivityDays: Int?,
        val delayDays: Int?,
        val taskTitle: String,
        val taskType: String,
        val preventDuplicates: Boolean
    )
}
