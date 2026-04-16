package com.example.api.controller

import com.example.api.dto.CreateEcosystemRequest
import com.example.api.dto.CreateMaintenanceTaskRequest
import com.example.api.dto.LogRequest
import com.example.api.dto.RegisterUserRequest
import com.example.api.dto.UpdateEcosystemRequest
import com.example.api.dto.UpdateLogRequest
import com.example.api.dto.UpdateMaintenanceTaskRequest
import com.example.api.dto.UpdateMaintenanceTaskStatusRequest
import com.example.api.dto.UpdateUserProfileRequest
import com.example.api.service.AuthService
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

/**
 * Serves the SSR UI backed by Freemarker templates and htmx fragment updates.
 */
@Controller
class UiController(
    private val ecosystemService: EcosystemService,
    private val ecosystemLogService: EcosystemLogService,
    private val maintenanceTaskService: MaintenanceTaskService,
    private val authService: AuthService,
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

        return executeRefresh { ecosystemService.updateEcosystem(id, request) }
    }

    @DeleteMapping("/ui/ecosystems/{id}")
    fun deleteEcosystem(@PathVariable id: UUID): ResponseEntity<String> =
        executeRedirect("/") { ecosystemService.deleteEcosystem(id) }

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

        return executeRefresh { ecosystemLogService.updateLog(ecosystemId, logId, request) }
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

    @PatchMapping("/ui/ecosystems/{ecosystemId}/tasks/{taskId}")
    fun updateTask(
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

        return executeRefresh { maintenanceTaskService.updateTask(ecosystemId, taskId, request) }
    }

    @PatchMapping("/ui/ecosystems/{ecosystemId}/tasks/{taskId}/status")
    fun updateTaskStatus(
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

        return executeRefresh { maintenanceTaskService.updateTaskStatus(ecosystemId, taskId, request) }
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
            okError(listOf(exception.reason ?: "Could not create the user."))
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

    private fun <T : Any> validate(target: T): List<String> =
        validator.validate(target)
            .map { it.message }
            .distinct()

    private fun executeRefresh(block: () -> Any): ResponseEntity<String> =
        try {
            block()
            ResponseEntity.ok()
                .header("HX-Refresh", "true")
                .body("")
        } catch (exception: ResponseStatusException) {
            okError(listOf(exception.reason ?: "Could not complete the action."))
        }

    private fun executeRedirect(location: String, block: () -> Unit): ResponseEntity<String> =
        try {
            block()
            ResponseEntity.ok()
                .header("HX-Redirect", location)
                .body("")
        } catch (exception: ResponseStatusException) {
            okError(listOf(exception.reason ?: "Could not complete the action."))
        }

    private fun okError(messages: List<String>): ResponseEntity<String> =
        ResponseEntity.ok(alertHtml(messages, "danger"))

    private fun alertHtml(messages: List<String>, variant: String): String {
        val safeMessages = messages
            .filter { it.isNotBlank() }
            .joinToString("") { "<li>${HtmlUtils.htmlEscape(it)}</li>" }

        return """
            <div class="alert alert-$variant mb-0" role="alert">
                <ul class="mb-0 ps-3">$safeMessages</ul>
            </div>
        """.trimIndent()
    }
}
