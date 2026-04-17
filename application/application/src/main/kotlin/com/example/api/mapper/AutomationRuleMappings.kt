package com.example.api.mapper

import com.example.api.dto.AutomationRuleResponse
import com.example.api.model.AutomationRule

/**
 * Converts an automation rule entity into its response representation.
 */
fun AutomationRule.toResponse(): AutomationRuleResponse =
    AutomationRuleResponse(
        id = id,
        name = name,
        enabled = enabled,
        scopeType = scopeType,
        ecosystemType = ecosystemType,
        triggerType = triggerType,
        eventType = eventType,
        inactivityDays = inactivityDays,
        delayDays = delayDays,
        taskTitle = taskTitle,
        taskType = taskType,
        preventDuplicates = preventDuplicates,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
