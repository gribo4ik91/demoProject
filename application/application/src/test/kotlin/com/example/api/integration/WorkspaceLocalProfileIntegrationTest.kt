package com.example.api.integration

import com.example.api.dto.CreateEcosystemRequest
import com.example.api.dto.CreateMaintenanceTaskRequest
import com.example.api.dto.LogRequest
import com.example.api.service.EcosystemLogService
import com.example.api.service.EcosystemService
import com.example.api.service.MaintenanceTaskService
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.ActiveProfiles

@SpringBootTest(properties = ["app.auth.enabled=false"])
@ActiveProfiles("local")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class WorkspaceLocalProfileIntegrationTest {

    @Autowired
    private lateinit var ecosystemService: EcosystemService

    @Autowired
    private lateinit var ecosystemLogService: EcosystemLogService

    @Autowired
    private lateinit var maintenanceTaskService: MaintenanceTaskService

    @Test
    fun `workspace cards load on local profile`() {
        val ecosystem = ecosystemService.createEcosystem(
            username = null,
            request = CreateEcosystemRequest(
                name = "Local workspace ecosystem",
                type = "FLORARIUM",
                description = "Reproduces the home page workspace"
            )
        )

        ecosystemLogService.addLog(
            username = null,
            ecosystemId = ecosystem.id!!,
            request = LogRequest(
                temperatureC = 21.0,
                humidityPercent = 55,
                eventType = "OBSERVATION",
                notes = "Healthy local profile setup"
            )
        )

        maintenanceTaskService.createTask(
            username = null,
            ecosystemId = ecosystem.id,
            request = CreateMaintenanceTaskRequest(
                title = "Inspect local workspace",
                taskType = "INSPECTION",
                dueDate = null
            )
        )

        ecosystemService.getWorkspaceCards()
        ecosystemService.getWorkspaceOverview()
    }
}
