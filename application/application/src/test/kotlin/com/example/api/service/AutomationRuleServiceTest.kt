package com.example.api.service

import com.example.api.dto.CreateAutomationRuleRequest
import com.example.api.dto.UpdateAutomationRuleRequest
import com.example.api.model.AuditEntityTypes
import com.example.api.model.AutomationRule
import com.example.api.repository.AutomationRuleRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.ArgumentCaptor
import org.mockito.Mockito
import java.util.Optional
import java.util.UUID

class AutomationRuleServiceTest {

    private val automationRuleRepository = Mockito.mock(AutomationRuleRepository::class.java)
    private val authService = Mockito.mock(AuthService::class.java)
    private val auditLogService = Mockito.mock(AuditLogService::class.java)

    private val service = AutomationRuleService(
        automationRuleRepository = automationRuleRepository,
        authService = authService,
        auditLogService = auditLogService
    )

    private val actor = AuthService.ActorSnapshot(user = null, username = "demo_user", displayName = "Demo User")

    @BeforeEach
    fun resetMocks() {
        Mockito.reset(automationRuleRepository, authService, auditLogService)
        Mockito.`when`(authService.resolveActorSnapshot("demo_user")).thenReturn(actor)
        Mockito.`when`(automationRuleRepository.save(anyAutomationRule())).thenAnswer { invocation ->
            val rule = invocation.getArgument<AutomationRule>(0)
            rule.copy(id = rule.id ?: UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa"))
        }
    }

    @Test
    fun `create automation rule writes audit entry`() {
        service.createRule("demo_user", createRequest())

        Mockito.verify(auditLogService).recordCreated(
            entityType = eq(AuditEntityTypes.AUTOMATION_RULE),
            entityId = eq(UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa")),
            entityName = eq("Watering follow up"),
            newValue = containsText("Created automation rule Watering follow up"),
            actor = eq(actor)
        )
    }

    @Test
    fun `update automation rule writes changed fields to audit`() {
        val ruleId = UUID.fromString("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb")
        val existing = automationRule(id = ruleId, name = "Old rule", enabled = false, delayDays = 1)
        Mockito.`when`(automationRuleRepository.findById(ruleId)).thenReturn(Optional.of(existing))

        service.updateRule(
            "demo_user",
            ruleId,
            UpdateAutomationRuleRequest(
                name = "Updated rule",
                enabled = true,
                scopeType = AutomationRuleService.SCOPE_ALL_ECOSYSTEMS,
                triggerType = AutomationRuleService.TRIGGER_AFTER_EVENT,
                eventType = "WATERING",
                delayDays = 2,
                taskTitle = "Inspect moisture",
                taskType = "INSPECTION",
                preventDuplicates = true
            )
        )

        @Suppress("UNCHECKED_CAST")
        val changesCaptor = ArgumentCaptor.forClass(List::class.java) as ArgumentCaptor<List<AuditFieldChange>>
        Mockito.verify(auditLogService).recordUpdated(
            entityType = eq(AuditEntityTypes.AUTOMATION_RULE),
            entityId = eq(ruleId),
            entityName = eq("Updated rule"),
            changes = captureChanges(changesCaptor),
            actor = eq(actor)
        )

        val changes = changesCaptor.value
        assertEquals("Old rule", changes.single { it.fieldName == "name" }.oldValue)
        assertEquals("Updated rule", changes.single { it.fieldName == "name" }.newValue)
        assertEquals(false, changes.single { it.fieldName == "enabled" }.oldValue)
        assertEquals(true, changes.single { it.fieldName == "enabled" }.newValue)
        assertTrue(changes.any { it.fieldName == "delayDays" && it.oldValue == 1 && it.newValue == 2 })
    }

    private fun createRequest(): CreateAutomationRuleRequest =
        CreateAutomationRuleRequest(
            name = "Watering follow up",
            enabled = true,
            scopeType = AutomationRuleService.SCOPE_ALL_ECOSYSTEMS,
            triggerType = AutomationRuleService.TRIGGER_AFTER_EVENT,
            eventType = "WATERING",
            delayDays = 1,
            taskTitle = "Inspect moisture",
            taskType = "INSPECTION",
            preventDuplicates = true
        )

    private fun automationRule(
        id: UUID,
        name: String,
        enabled: Boolean,
        delayDays: Int
    ): AutomationRule =
        AutomationRule(
            id = id,
            name = name,
            enabled = enabled,
            scopeType = AutomationRuleService.SCOPE_ALL_ECOSYSTEMS,
            triggerType = AutomationRuleService.TRIGGER_AFTER_EVENT,
            eventType = "WATERING",
            delayDays = delayDays,
            taskTitle = "Inspect moisture",
            taskType = "INSPECTION",
            preventDuplicates = true
        )

    private fun anyAutomationRule(): AutomationRule =
        Mockito.any(AutomationRule::class.java) ?: automationRule(
            id = UUID.fromString("cccccccc-cccc-cccc-cccc-cccccccccccc"),
            name = "matcher",
            enabled = true,
            delayDays = 0
        )

    private fun <T> eq(value: T): T =
        Mockito.eq(value) ?: value

    private fun containsText(value: String): String =
        Mockito.contains(value) ?: value

    private fun captureChanges(captor: ArgumentCaptor<List<AuditFieldChange>>): List<AuditFieldChange> =
        captor.capture() ?: emptyList()
}
