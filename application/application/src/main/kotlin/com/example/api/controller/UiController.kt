package com.example.api.controller

import com.example.api.dto.CreateEcosystemRequest
import com.example.api.dto.CreateAutomationRuleRequest
import com.example.api.dto.CreateMaintenanceTaskRequest
import com.example.api.dto.LogRequest
import com.example.api.dto.RegisterUserRequest
import com.example.api.dto.UpdateAutomationRuleRequest
import com.example.api.dto.UpdateEcosystemRequest
import com.example.api.dto.UpdateLogRequest
import com.example.api.dto.UpdateMaintenanceTaskRequest
import com.example.api.dto.UpdateMaintenanceTaskStatusRequest
import com.example.api.dto.UpdateUserProfileRequest
import com.example.api.dto.UpdateUserRoleRequest
import com.example.api.service.AuditLogService
import com.example.api.service.AuthService
import com.example.api.service.AutomationRuleService
import com.example.api.service.EcosystemLogService
import com.example.api.service.EcosystemService
import com.example.api.service.MaintenanceTaskService
import jakarta.validation.Validator
import org.springframework.http.HttpHeaders
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.server.ResponseStatusException
import org.springframework.web.util.HtmlUtils
import java.time.LocalDate
import java.util.UUID

private data class UiFormError(
    val field: String?,
    val label: String,
    val message: String
)

/**
 * Serves the SSR UI backed by Freemarker templates and htmx fragment updates.
 */
@Controller
class UiController(
    private val ecosystemService: EcosystemService,
    private val ecosystemLogService: EcosystemLogService,
    private val maintenanceTaskService: MaintenanceTaskService,
    private val automationRuleService: AutomationRuleService,
    private val authService: AuthService,
    private val auditLogService: AuditLogService,
    private val validator: Validator
) {

    @GetMapping("/")
    fun homePage(
        @RequestParam(required = false, defaultValue = "") search: String,
        @RequestParam(required = false, defaultValue = "ALL") status: String,
        @RequestParam(required = false, defaultValue = "PRIORITY") sort: String,
        @RequestParam(required = false, defaultValue = "0") page: Int,
        model: Model
    ): String {
        populateHomeModel(model, search, status, sort, page)
        return "pages/home"
    }

    @GetMapping("/index.html")
    fun legacyHomeRedirect(): String = "redirect:/"

    @GetMapping("/ecosystems/{id}")
    fun ecosystemPage(
        @PathVariable id: UUID,
        @RequestParam(required = false, defaultValue = "ALL") taskFilter: String,
        @RequestParam(required = false, defaultValue = "ALL") taskSource: String,
        @RequestParam(required = false, defaultValue = "") taskSearch: String,
        @RequestParam(required = false, defaultValue = "") eventType: String,
        @RequestParam(required = false, defaultValue = "0") logPage: Int,
        model: Model
    ): String {
        populateEcosystemModel(model, id, taskFilter, taskSource, taskSearch, eventType, logPage)
        return "pages/ecosystem"
    }

    @GetMapping("/ecosystem.html")
    fun legacyEcosystemRedirect(@RequestParam id: UUID): String = "redirect:/ecosystems/$id"

    @GetMapping("/register.html")
    fun legacyRegisterRedirect(): String = "redirect:/register"

    @GetMapping("/profile.html")
    fun legacyProfileRedirect(): String = "redirect:/profile"

    @GetMapping("/users.html")
    fun legacyUsersRedirect(): String = "redirect:/users"

    @GetMapping("/ui/workspace")
    fun workspaceFragment(
        @RequestParam(required = false, defaultValue = "") search: String,
        @RequestParam(required = false, defaultValue = "ALL") status: String,
        @RequestParam(required = false, defaultValue = "PRIORITY") sort: String,
        @RequestParam(required = false, defaultValue = "0") page: Int,
        model: Model
    ): String {
        populateHomeModel(model, search, status, sort, page)
        return "fragments/workspace-panel"
    }

    @GetMapping("/audit")
    fun auditPage(
        @RequestParam(required = false, defaultValue = "0") page: Int,
        model: Model
    ): String {
        populateAuditModel(model, page)
        return "pages/audit"
    }

    @GetMapping("/ui/audit")
    fun auditListFragment(
        @RequestParam(required = false, defaultValue = "0") page: Int,
        model: Model
    ): String {
        populateAuditModel(model, page)
        return "fragments/audit-list"
    }

    @GetMapping("/ui/ecosystems/{id}/tasks")
    fun tasksFragment(
        @PathVariable id: UUID,
        @RequestParam(required = false, defaultValue = "ALL") taskFilter: String,
        @RequestParam(required = false, defaultValue = "ALL") taskSource: String,
        @RequestParam(required = false, defaultValue = "") taskSearch: String,
        model: Model
    ): String {
        val tasks = tasksFor(id, taskFilter, taskSource, taskSearch)
        model.addAttribute("ecosystemId", id)
        model.addAttribute("taskFilter", normalizeFilter(taskFilter, "ALL"))
        model.addAttribute("taskSource", normalizeFilter(taskSource, "ALL"))
        model.addAttribute("taskSearch", taskSearch.trim())
        model.addAttribute("tasks", tasks)
        model.addAttribute("openTaskCount", tasks.count { it.status == "OPEN" })
        return "fragments/task-list"
    }

    @GetMapping("/ui/ecosystems/{id}/logs")
    fun logsFragment(
        @PathVariable id: UUID,
        @RequestParam(required = false, defaultValue = "") eventType: String,
        @RequestParam(required = false, defaultValue = "0") page: Int,
        model: Model
    ): String {
        val normalizedEvent = eventType.trim().takeIf { it.isNotEmpty() }
        val logPage = ecosystemLogService.getLogs(id, normalizedEvent, page, 5)
        model.addAttribute("ecosystemId", id)
        model.addAttribute("logEventType", normalizedEvent ?: "")
        model.addAttribute("logPage", logPage)
        return "fragments/log-list"
    }

    @GetMapping("/register")
    fun registerPage(): String = "pages/register"

    @GetMapping("/profile")
    fun profilePage(authentication: Authentication, model: Model): String {
        model.addAttribute("profile", authService.getCurrentUserProfile(authentication.name))
        return "pages/profile"
    }

    @GetMapping("/users")
    fun usersPage(authentication: Authentication, model: Model): String {
        model.addAttribute("users", authService.getAllUsers(authentication.name))
        return "pages/users"
    }

    @GetMapping("/automation-rules")
    fun automationRulesPage(
        @RequestParam(required = false, defaultValue = "ALL") statusFilter: String,
        @RequestParam(required = false, defaultValue = "ALL") triggerFilter: String,
        model: Model
    ): String {
        populateAutomationRulesModel(model, statusFilter, triggerFilter)
        model.addAttribute("editingRule", null)
        return "pages/automation-rules"
    }

    @GetMapping("/rules")
    fun legacyAutomationRulesRedirect(): String = "redirect:/automation-rules"

    @GetMapping("/ui/automation-rules/list")
    fun automationRulesListFragment(
        @RequestParam(required = false, defaultValue = "ALL") statusFilter: String,
        @RequestParam(required = false, defaultValue = "ALL") triggerFilter: String,
        model: Model
    ): String {
        populateAutomationRulesModel(model, statusFilter, triggerFilter)
        return "fragments/rule-list"
    }

    @GetMapping("/ui/automation-rules/editor")
    fun automationRuleCreateEditor(model: Model): String {
        model.addAttribute("editingRule", null)
        return "fragments/rule-editor"
    }

    @GetMapping("/ui/automation-rules/editor/empty")
    fun automationRuleEmptyEditor(): String = "fragments/rule-editor-empty"

    @GetMapping("/ui/automation-rules/{id}/editor")
    fun automationRuleEditEditor(@PathVariable id: UUID, model: Model): String {
        model.addAttribute("editingRule", automationRuleService.getRule(id))
        return "fragments/rule-editor"
    }

    @PostMapping("/ui/ecosystems")
    fun createEcosystem(
        authentication: Authentication?,
        @RequestParam name: String,
        @RequestParam type: String,
        @RequestParam(required = false) description: String?
    ): ResponseEntity<String> {
        val request = CreateEcosystemRequest(name = name, type = type, description = description)
        val errors = validate(request)
        if (errors.isNotEmpty()) {
            return okError(errors)
        }

        return executeRefresh { ecosystemService.createEcosystem(authentication?.name, request) }
    }

    @PatchMapping("/ui/ecosystems/{id}")
    fun updateEcosystem(
        authentication: Authentication?,
        @PathVariable id: UUID,
        @RequestParam name: String,
        @RequestParam type: String,
        @RequestParam(required = false) description: String?
    ): ResponseEntity<String> {
        val request = UpdateEcosystemRequest(name = name, type = type, description = description)
        val errors = validate(request)
        if (errors.isNotEmpty()) {
            return okError(errors)
        }

        return executeRefresh { ecosystemService.updateEcosystem(authentication?.name, id, request) }
    }

    @DeleteMapping("/ui/ecosystems/{id}")
    fun deleteEcosystem(authentication: Authentication?, @PathVariable id: UUID): ResponseEntity<String> =
        executeRedirect("/") { ecosystemService.deleteEcosystem(authentication?.name, id) }

    @PostMapping("/ui/ecosystems/{id}/logs")
    fun createLog(
        authentication: Authentication?,
        @PathVariable id: UUID,
        @RequestParam(required = false) temperatureC: Double?,
        @RequestParam(required = false) humidityPercent: Int?,
        @RequestParam eventType: String,
        @RequestParam(required = false) notes: String?
    ): ResponseEntity<String> {
        val request = LogRequest(
            temperatureC = temperatureC,
            humidityPercent = humidityPercent,
            eventType = eventType,
            notes = notes
        )
        val errors = validate(request)
        if (errors.isNotEmpty()) {
            return okError(errors)
        }

        return executeRefresh { ecosystemLogService.addLog(authentication?.name, id, request) }
    }

    @PatchMapping("/ui/ecosystems/{ecosystemId}/logs/{logId}")
    fun updateLog(
        authentication: Authentication?,
        @PathVariable ecosystemId: UUID,
        @PathVariable logId: UUID,
        @RequestParam(required = false) temperatureC: Double?,
        @RequestParam(required = false) humidityPercent: Int?,
        @RequestParam eventType: String,
        @RequestParam(required = false) notes: String?
    ): ResponseEntity<String> {
        val request = UpdateLogRequest(
            temperatureC = temperatureC,
            humidityPercent = humidityPercent,
            eventType = eventType,
            notes = notes
        )
        val errors = validate(request)
        if (errors.isNotEmpty()) {
            return okError(errors)
        }

        return executeRefresh { ecosystemLogService.updateLog(authentication?.name, ecosystemId, logId, request) }
    }

    @PostMapping("/ui/ecosystems/{id}/tasks")
    fun createTask(
        authentication: Authentication?,
        @PathVariable id: UUID,
        @RequestParam title: String,
        @RequestParam taskType: String,
        @RequestParam(required = false) dueDate: LocalDate?
    ): ResponseEntity<String> {
        val request = CreateMaintenanceTaskRequest(title = title, taskType = taskType, dueDate = dueDate)
        val errors = validate(request)
        if (errors.isNotEmpty()) {
            return okError(errors)
        }

        return executeRefresh { maintenanceTaskService.createTask(authentication?.name, id, request) }
    }

    @PostMapping("/ui/automation-rules")
    fun createAutomationRule(
        authentication: Authentication?,
        @RequestParam name: String,
        @RequestParam(defaultValue = "false") enabled: Boolean,
        @RequestParam scopeType: String,
        @RequestParam(required = false) ecosystemType: String?,
        @RequestParam triggerType: String,
        @RequestParam eventType: String,
        @RequestParam(required = false) inactivityDays: Int?,
        @RequestParam(required = false) delayDays: Int?,
        @RequestParam taskTitle: String,
        @RequestParam taskType: String,
        @RequestParam(defaultValue = "false") preventDuplicates: Boolean
    ): ResponseEntity<String> {
        val request = CreateAutomationRuleRequest(
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
            preventDuplicates = preventDuplicates
        )
        val errors = validate(request)
        if (errors.isNotEmpty()) {
            return okError(errors)
        }

        return executeRefresh { automationRuleService.createRule(authentication?.name, request) }
    }

    @PatchMapping("/ui/automation-rules/{id}")
    fun updateAutomationRule(
        authentication: Authentication?,
        @PathVariable id: UUID,
        @RequestParam name: String,
        @RequestParam(defaultValue = "false") enabled: Boolean,
        @RequestParam scopeType: String,
        @RequestParam(required = false) ecosystemType: String?,
        @RequestParam triggerType: String,
        @RequestParam eventType: String,
        @RequestParam(required = false) inactivityDays: Int?,
        @RequestParam(required = false) delayDays: Int?,
        @RequestParam taskTitle: String,
        @RequestParam taskType: String,
        @RequestParam(defaultValue = "false") preventDuplicates: Boolean
    ): ResponseEntity<String> {
        val request = UpdateAutomationRuleRequest(
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
            preventDuplicates = preventDuplicates
        )
        val errors = validate(request)
        if (errors.isNotEmpty()) {
            return okError(errors)
        }

        return executeRefresh { automationRuleService.updateRule(authentication?.name, id, request) }
    }

    @PatchMapping("/ui/automation-rules/{id}/enabled")
    fun updateAutomationRuleEnabled(
        authentication: Authentication?,
        @PathVariable id: UUID,
        @RequestParam enabled: Boolean
    ): ResponseEntity<String> =
        executeRefresh { automationRuleService.setRuleEnabled(authentication?.name, id, enabled) }

    @DeleteMapping("/ui/automation-rules/{id}")
    fun deleteAutomationRule(authentication: Authentication?, @PathVariable id: UUID): ResponseEntity<String> =
        executeRefresh { automationRuleService.deleteRule(authentication?.name, id) }

    @PatchMapping("/ui/ecosystems/{ecosystemId}/tasks/{taskId}")
    fun updateTask(
        authentication: Authentication?,
        @PathVariable ecosystemId: UUID,
        @PathVariable taskId: UUID,
        @RequestParam title: String,
        @RequestParam taskType: String,
        @RequestParam(required = false) dueDate: LocalDate?
    ): ResponseEntity<String> {
        val request = UpdateMaintenanceTaskRequest(title = title, taskType = taskType, dueDate = dueDate)
        val errors = validate(request)
        if (errors.isNotEmpty()) {
            return okError(errors)
        }

        return executeRefresh { maintenanceTaskService.updateTask(authentication?.name, ecosystemId, taskId, request) }
    }

    @PatchMapping("/ui/ecosystems/{ecosystemId}/tasks/{taskId}/status")
    fun updateTaskStatus(
        authentication: Authentication?,
        @PathVariable ecosystemId: UUID,
        @PathVariable taskId: UUID,
        @RequestParam status: String,
        @RequestParam(required = false) dismissalReason: String?
    ): ResponseEntity<String> {
        val request = UpdateMaintenanceTaskStatusRequest(status = status, dismissalReason = dismissalReason)
        val errors = validate(request)
        if (errors.isNotEmpty()) {
            return okError(errors)
        }

        return executeRefresh { maintenanceTaskService.updateTaskStatus(authentication?.name, ecosystemId, taskId, request) }
    }

    @PostMapping("/ui/register")
    fun register(
        @RequestParam displayName: String,
        @RequestParam username: String,
        @RequestParam firstName: String,
        @RequestParam lastName: String,
        @RequestParam email: String,
        @RequestParam(required = false) location: String?,
        @RequestParam(required = false) bio: String?,
        @RequestParam password: String
    ): ResponseEntity<String> {
        val request = RegisterUserRequest(
            displayName = displayName,
            username = username,
            firstName = firstName,
            lastName = lastName,
            email = email,
            location = location,
            bio = bio,
            password = password
        )
        val errors = validate(request)
        if (errors.isNotEmpty()) {
            return okError(errors)
        }

        return try {
            authService.registerUser(request)
            ResponseEntity.ok()
                .header("HX-Redirect", "/login?registered=${username.trim()}")
                .body("")
        } catch (exception: ResponseStatusException) {
            okBusinessError(exception.reason ?: "Could not create the user.")
        }
    }

    @PutMapping("/ui/profile")
    fun updateProfile(
        authentication: Authentication,
        @RequestParam displayName: String,
        @RequestParam firstName: String,
        @RequestParam lastName: String,
        @RequestParam email: String,
        @RequestParam(required = false) location: String?,
        @RequestParam(required = false) bio: String?
    ): ResponseEntity<String> {
        val request = UpdateUserProfileRequest(
            displayName = displayName,
            firstName = firstName,
            lastName = lastName,
            email = email,
            location = location,
            bio = bio
        )
        val errors = validate(request)
        if (errors.isNotEmpty()) {
            return okError(errors)
        }

        return executeRefresh { authService.updateCurrentUserProfile(authentication.name, request) }
    }

    @DeleteMapping("/ui/users/{userId}")
    fun deleteUser(authentication: Authentication, @PathVariable userId: UUID): ResponseEntity<String> =
        executeRefresh { authService.deleteUser(authentication.name, userId) }

    @PutMapping("/ui/users/{userId}/role")
    fun updateUserRole(
        authentication: Authentication,
        @PathVariable userId: UUID,
        @RequestParam role: String
    ): ResponseEntity<String> =
        executeRefresh { authService.updateUserRole(authentication.name, userId, UpdateUserRoleRequest(role)) }

    private fun populateHomeModel(
        model: Model,
        search: String,
        status: String,
        sort: String,
        page: Int
    ) {
        val normalizedSearch = search.trim()
        val normalizedStatus = normalizeFilter(status, "ALL")
        val normalizedSort = normalizeFilter(sort, "PRIORITY")
        val pageData = ecosystemService.getWorkspaceCards(normalizedSearch, normalizedStatus, normalizedSort, page, 9)
        val cards = pageData.items

        model.addAttribute("search", normalizedSearch)
        model.addAttribute("status", normalizedStatus)
        model.addAttribute("sort", normalizedSort)
        model.addAttribute("workspacePage", pageData)
        model.addAttribute("workspaceOverview", ecosystemService.getWorkspaceOverview(normalizedSearch, normalizedStatus))
        model.addAttribute("priorityCards", cards.filter { it.status == "NEEDS_ATTENTION" || it.overdueTasks > 0 }.take(4))
        model.addAttribute("auditPreview", auditLogService.getAuditLogs(0, 3))
    }

    private fun populateAuditModel(model: Model, page: Int) {
        model.addAttribute("auditPage", auditLogService.getAuditLogs(page, 12))
    }

    private fun populateEcosystemModel(
        model: Model,
        id: UUID,
        taskFilter: String,
        taskSource: String,
        taskSearch: String,
        eventType: String,
        logPage: Int
    ) {
        val normalizedTaskFilter = normalizeFilter(taskFilter, "ALL")
        val normalizedTaskSource = normalizeFilter(taskSource, "ALL")
        val normalizedTaskSearch = taskSearch.trim()
        val normalizedEventType = eventType.trim().takeIf { it.isNotEmpty() }
        val tasks = tasksFor(id, normalizedTaskFilter, normalizedTaskSource, normalizedTaskSearch)

        model.addAttribute("ecosystem", ecosystemService.getEcosystem(id))
        model.addAttribute("summary", ecosystemService.getEcosystemSummary(id))
        model.addAttribute("tasks", tasks)
        model.addAttribute("openTaskCount", tasks.count { it.status == "OPEN" })
        model.addAttribute("taskFilter", normalizedTaskFilter)
        model.addAttribute("taskSource", normalizedTaskSource)
        model.addAttribute("taskSearch", normalizedTaskSearch)
        model.addAttribute("logEventType", normalizedEventType ?: "")
        model.addAttribute("logPage", ecosystemLogService.getLogs(id, normalizedEventType, logPage, 5))
    }

    private fun populateAutomationRulesModel(
        model: Model,
        statusFilter: String,
        triggerFilter: String
    ) {
        val normalizedStatus = normalizeFilter(statusFilter, "ALL")
        val normalizedTrigger = normalizeFilter(triggerFilter, "ALL")
        val rules = automationRuleService.getRules(normalizedStatus, normalizedTrigger)

        model.addAttribute("ruleStatusFilter", normalizedStatus)
        model.addAttribute("ruleTriggerFilter", normalizedTrigger)
        model.addAttribute("rules", rules)
        model.addAttribute("activeRuleCount", rules.count { it.enabled })
        model.addAttribute("eventRuleCount", rules.count { it.triggerType == "AFTER_EVENT" })
        model.addAttribute("inactivityRuleCount", rules.count { it.triggerType == "AFTER_INACTIVITY" })
    }

    private fun tasksFor(id: UUID, taskFilter: String, taskSource: String, taskSearch: String) =
        maintenanceTaskService.getTasks(id, taskFilter)
            .filter { task ->
                when (taskSource) {
                    "MANUAL" -> !task.autoCreated
                    "SUGGESTED" -> task.autoCreated
                    else -> true
                }
            }
            .filter { task ->
                taskSearch.isBlank() || task.title.contains(taskSearch, ignoreCase = true)
            }

    private fun normalizeFilter(value: String?, fallback: String): String =
        value?.trim()?.uppercase()?.takeIf { it.isNotEmpty() } ?: fallback

    private fun <T : Any> validate(target: T): List<UiFormError> =
        validator.validate(target)
            .map {
                val field = it.propertyPath.toString().takeIf { path -> path.isNotBlank() }
                UiFormError(
                    field = field,
                    label = fieldLabel(field),
                    message = it.message
                )
            }
            .distinctBy { it.field to it.message }

    private fun executeRefresh(block: () -> Any): ResponseEntity<String> =
        try {
            block()
            ResponseEntity.ok()
                .header("HX-Refresh", "true")
                .body("")
        } catch (exception: ResponseStatusException) {
            okBusinessError(exception.reason ?: "Could not complete the action.")
        }

    private fun executeRedirect(location: String, block: () -> Unit): ResponseEntity<String> =
        try {
            block()
            ResponseEntity.ok()
                .header("HX-Redirect", location)
                .body("")
        } catch (exception: ResponseStatusException) {
            okBusinessError(exception.reason ?: "Could not complete the action.")
        }

    private fun okBusinessError(message: String): ResponseEntity<String> =
        okError(errorsForBusinessMessage(message))

    private fun okError(errors: List<UiFormError>): ResponseEntity<String> =
        ResponseEntity.ok(alertHtml(errors, "danger"))

    private fun alertHtml(errors: List<UiFormError>, variant: String): String {
        val safeMessages = errors
            .filter { it.message.isNotBlank() }
            .joinToString("") { error ->
                val safeLabel = HtmlUtils.htmlEscape(error.label)
                val safeMessage = HtmlUtils.htmlEscape(error.message)
                if (error.field == null) "<li>$safeMessage</li>" else "<li><strong>$safeLabel</strong>: $safeMessage</li>"
            }
        val fieldPayload = errors
            .filter { !it.field.isNullOrBlank() && it.message.isNotBlank() }
            .joinToString("") { error ->
                val field = error.field ?: return@joinToString ""
                """
                    <span data-field-error
                          data-field="${HtmlUtils.htmlEscape(field)}"
                          data-label="${HtmlUtils.htmlEscape(error.label)}"
                          data-message="${HtmlUtils.htmlEscape(error.message)}"></span>
                """.trimIndent()
            }

        return """
            <div class="alert alert-$variant mb-0" role="alert">
                <ul class="mb-0 ps-3">$safeMessages</ul>
            </div>
            <div class="d-none" data-field-error-payload>$fieldPayload</div>
        """.trimIndent()
    }

    private fun errorsForBusinessMessage(message: String): List<UiFormError> =
        when (message) {
            "Username already exists" -> listOf(fieldError("username", message))
            "Email already exists" -> listOf(fieldError("email", message))
            "Ecosystem name already exists" -> listOf(fieldError("name", message))
            "Open task already exists for this ecosystem" -> listOf(fieldError("title", message))
            "Log must include temperature, humidity, or notes" -> listOf(
                fieldError("temperatureC", message),
                fieldError("humidityPercent", message),
                fieldError("notes", message)
            )
            "Unsupported task status" -> listOf(fieldError("status", message))
            "Dismissal reason is required for dismissed suggestions",
            "Unsupported dismissal reason",
            "Dismissal reason can only be set when dismissing a suggestion" -> listOf(fieldError("dismissalReason", message))
            "Unsupported rule scope" -> listOf(fieldError("scopeType", message))
            "Unsupported trigger type" -> listOf(fieldError("triggerType", message))
            "Unsupported generated task type" -> listOf(fieldError("taskType", message))
            "Valid ecosystem type is required for this scope",
            "Ecosystem type can only be set for type-specific scope" -> listOf(fieldError("ecosystemType", message))
            "Event type is required" -> listOf(fieldError("eventType", message))
            "Inactivity days are only valid for inactivity rules",
            "Inactivity days are required for inactivity rules" -> listOf(fieldError("inactivityDays", message))
            "Delay days are only valid for event-based rules" -> listOf(fieldError("delayDays", message))
            else -> listOf(UiFormError(field = null, label = "Form", message = message))
        }

    private fun fieldError(field: String, message: String): UiFormError =
        UiFormError(field = field, label = fieldLabel(field), message = message)

    private fun fieldLabel(field: String?): String =
        when (field) {
            "displayName" -> "Display name"
            "username" -> "Login"
            "firstName" -> "First name"
            "lastName" -> "Last name"
            "email" -> "Email"
            "location" -> "Location"
            "bio" -> "Bio"
            "password" -> "Password"
            "name" -> "Name"
            "type" -> "Type"
            "description" -> "Description"
            "temperatureC" -> "Temperature"
            "humidityPercent" -> "Humidity"
            "eventType" -> "Event type"
            "notes" -> "Notes"
            "title" -> "Task title"
            "taskType" -> "Task type"
            "dueDate" -> "Due date"
            "status" -> "Status"
            "dismissalReason" -> "Dismissal reason"
            "scopeType" -> "Scope"
            "ecosystemType" -> "Ecosystem type"
            "triggerType" -> "Trigger"
            "inactivityDays" -> "Inactivity days"
            "delayDays" -> "Delay days"
            "taskTitle" -> "Suggested task title"
            "preventDuplicates" -> "Duplicate protection"
            "role" -> "Role"
            else -> "Form"
        }
}
