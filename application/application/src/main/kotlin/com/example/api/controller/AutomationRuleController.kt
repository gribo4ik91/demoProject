package com.example.api.controller

import com.example.api.dto.AutomationRuleResponse
import com.example.api.dto.CreateAutomationRuleRequest
import com.example.api.dto.UpdateAutomationRuleEnabledRequest
import com.example.api.dto.UpdateAutomationRuleRequest
import com.example.api.service.AutomationRuleService
import jakarta.validation.Valid
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

/**
 * Implements the automation-rule endpoint contract and delegates requests to [AutomationRuleService].
 */
@RestController
class AutomationRuleController(
    private val automationRuleService: AutomationRuleService
) : AutomationRuleControllerApi {

    /**
     * Creates a new automation rule from the request payload.
     */
    override fun createRule(authentication: Authentication?, @Valid request: CreateAutomationRuleRequest): AutomationRuleResponse =
        automationRuleService.createRule(authentication?.name, request)

    /**
     * Updates an existing automation rule.
     */
    override fun updateRule(authentication: Authentication?, id: UUID, @Valid request: UpdateAutomationRuleRequest): AutomationRuleResponse =
        automationRuleService.updateRule(authentication?.name, id, request)

    /**
     * Returns automation rules, optionally filtered by status and trigger family.
     */
    override fun getRules(status: String?, trigger: String?): List<AutomationRuleResponse> =
        automationRuleService.getRules(status, trigger)

    /**
     * Returns one automation rule by id.
     */
    override fun getRule(id: UUID): AutomationRuleResponse =
        automationRuleService.getRule(id)

    /**
     * Enables or disables an existing automation rule.
     */
    override fun updateRuleEnabled(
        authentication: Authentication?,
        id: UUID,
        request: UpdateAutomationRuleEnabledRequest
    ): AutomationRuleResponse =
        automationRuleService.setRuleEnabled(authentication?.name, id, request.enabled)

    /**
     * Deletes an automation rule.
     */
    override fun deleteRule(authentication: Authentication?, id: UUID) =
        automationRuleService.deleteRule(authentication?.name, id)
}
