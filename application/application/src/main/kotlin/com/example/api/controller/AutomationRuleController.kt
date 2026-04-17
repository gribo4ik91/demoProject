package com.example.api.controller

import com.example.api.dto.AutomationRuleResponse
import com.example.api.dto.CreateAutomationRuleRequest
import com.example.api.dto.UpdateAutomationRuleEnabledRequest
import com.example.api.dto.UpdateAutomationRuleRequest
import com.example.api.service.AutomationRuleService
import jakarta.validation.Valid
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
    override fun createRule(@Valid request: CreateAutomationRuleRequest): AutomationRuleResponse =
        automationRuleService.createRule(request)

    /**
     * Updates an existing automation rule.
     */
    override fun updateRule(id: UUID, @Valid request: UpdateAutomationRuleRequest): AutomationRuleResponse =
        automationRuleService.updateRule(id, request)

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
    override fun updateRuleEnabled(id: UUID, request: UpdateAutomationRuleEnabledRequest): AutomationRuleResponse =
        automationRuleService.setRuleEnabled(id, request.enabled)

    /**
     * Deletes an automation rule.
     */
    override fun deleteRule(id: UUID) =
        automationRuleService.deleteRule(id)
}
