package com.example.api.integration

import com.example.api.repository.EcosystemLogRepository
import com.example.api.repository.EcosystemRepository
import com.example.api.repository.MaintenanceTaskRepository
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.time.LocalDate
import java.time.LocalDateTime

/**
 * Exercises the main ecosystem, log, summary, and maintenance task flows end to end.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ApiIntegrationTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var ecosystemRepository: EcosystemRepository

    @Autowired
    private lateinit var ecosystemLogRepository: EcosystemLogRepository

    @Autowired
    private lateinit var maintenanceTaskRepository: MaintenanceTaskRepository

    /**
     * Clears persisted test data before each integration scenario.
     */
    @BeforeEach
    fun cleanDatabase() {
        maintenanceTaskRepository.deleteAll()
        ecosystemLogRepository.deleteAll()
        ecosystemRepository.deleteAll()
    }

    @Test
    fun `create ecosystem persists data and returns created payload`() {
        val payload = """
            {
              "name": "Rainforest Capsule",
              "type": "FLORARIUM",
              "description": "High humidity setup for moss and ferns"
            }
        """.trimIndent()

        mockMvc.perform(
            post("/api/v1/ecosystems")
                .contentType("application/json")
                .content(payload)
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.id").isNotEmpty)
            .andExpect(jsonPath("$.name").value("Rainforest Capsule"))
            .andExpect(jsonPath("$.type").value("FLORARIUM"))

        val persisted = ecosystemRepository.findAll().single()
        check(persisted.name == "Rainforest Capsule")
        check(persisted.description == "High humidity setup for moss and ferns")
    }

    @Test
    fun `invalid log payload returns validation error and does not create log`() {
        val ecosystemId = createEcosystem()
        val payload = """
            {
              "temperatureC": 21.5,
              "humidityPercent": 180,
              "eventType": "",
              "notes": "x"
            }
        """.trimIndent()

        mockMvc.perform(
            post("/api/v1/ecosystems/$ecosystemId/logs")
                .contentType("application/json")
                .content(payload)
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.message").value("Validation failed"))
            .andExpect(jsonPath("$.validationErrors.humidityPercent").exists())
            .andExpect(jsonPath("$.validationErrors.eventType").exists())

        check(ecosystemLogRepository.count() == 0L)
    }

    @Test
    fun `logs are returned newest first and deleting ecosystem removes its logs`() {
        val ecosystemId = createEcosystem()

        addLog(
            ecosystemId = ecosystemId,
            payload = """
                {
                  "temperatureC": 23.1,
                  "humidityPercent": 55,
                  "eventType": "OBSERVATION",
                  "notes": "Initial reading"
                }
            """.trimIndent()
        )

        addLog(
            ecosystemId = ecosystemId,
            payload = """
                {
                  "temperatureC": 24.0,
                  "humidityPercent": 58,
                  "eventType": "WATERING",
                  "notes": "After misting"
                }
            """.trimIndent()
        )

        mockMvc.perform(get("/api/v1/ecosystems/$ecosystemId/logs"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.items[0].eventType").value("WATERING"))
            .andExpect(jsonPath("$.items[1].eventType").value("OBSERVATION"))

        check(ecosystemLogRepository.count() == 2L)

        mockMvc.perform(delete("/api/v1/ecosystems/$ecosystemId"))
            .andExpect(status().isNoContent)

        check(ecosystemRepository.count() == 0L)
        check(ecosystemLogRepository.count() == 0L)
    }

    @Test
    fun `summary endpoint returns status latest reading and activity window`() {
        val ecosystemId = createEcosystem()

        addLog(
            ecosystemId = ecosystemId,
            payload = """
                {
                  "temperatureC": 21.8,
                  "humidityPercent": 62,
                  "eventType": "OBSERVATION",
                  "notes": "Initial baseline"
                }
            """.trimIndent()
        )

        addLog(
            ecosystemId = ecosystemId,
            payload = """
                {
                  "temperatureC": 24.2,
                  "humidityPercent": 59,
                  "eventType": "WATERING",
                  "notes": "After light misting"
                }
            """.trimIndent()
        )

        mockMvc.perform(get("/api/v1/ecosystems/$ecosystemId/summary"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.ecosystemId").value(ecosystemId.toString()))
            .andExpect(jsonPath("$.status").value("STABLE"))
            .andExpect(jsonPath("$.latestEventType").value("WATERING"))
            .andExpect(jsonPath("$.currentTemperatureC").value(24.2))
            .andExpect(jsonPath("$.currentHumidityPercent").value(59))
            .andExpect(jsonPath("$.logsLast7Days").value(2))
    }

    @Test
    fun `logs endpoint supports filtering and pagination`() {
        val ecosystemId = createEcosystem()

        addLog(
            ecosystemId = ecosystemId,
            payload = """
                {
                  "temperatureC": 22.0,
                  "humidityPercent": 60,
                  "eventType": "OBSERVATION",
                  "notes": "First observation"
                }
            """.trimIndent()
        )

        addLog(
            ecosystemId = ecosystemId,
            payload = """
                {
                  "temperatureC": 23.5,
                  "humidityPercent": 57,
                  "eventType": "FEEDING",
                  "notes": "Protein meal"
                }
            """.trimIndent()
        )

        addLog(
            ecosystemId = ecosystemId,
            payload = """
                {
                  "temperatureC": 24.1,
                  "humidityPercent": 55,
                  "eventType": "WATERING",
                  "notes": "Misted walls"
                }
            """.trimIndent()
        )

        mockMvc.perform(get("/api/v1/ecosystems/$ecosystemId/logs?page=0&size=2"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.page").value(0))
            .andExpect(jsonPath("$.size").value(2))
            .andExpect(jsonPath("$.totalElements").value(3))
            .andExpect(jsonPath("$.totalPages").value(2))
            .andExpect(jsonPath("$.hasNext").value(true))
            .andExpect(jsonPath("$.items.length()").value(2))
            .andExpect(jsonPath("$.items[0].eventType").value("WATERING"))
            .andExpect(jsonPath("$.items[1].eventType").value("FEEDING"))

        mockMvc.perform(get("/api/v1/ecosystems/$ecosystemId/logs?eventType=OBSERVATION&page=0&size=5"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.totalElements").value(1))
            .andExpect(jsonPath("$.hasNext").value(false))
            .andExpect(jsonPath("$.items.length()").value(1))
            .andExpect(jsonPath("$.items[0].eventType").value("OBSERVATION"))
    }

    @Test
    fun `maintenance tasks can be created listed and marked done`() {
        val ecosystemId = createEcosystem()

        val createPayload = """
            {
              "title": "Refill water reservoir",
              "taskType": "WATERING",
              "dueDate": "2026-04-10"
            }
        """.trimIndent()

        val createdTaskJson = mockMvc.perform(
            post("/api/v1/ecosystems/$ecosystemId/tasks")
                .contentType("application/json")
                .content(createPayload)
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.title").value("Refill water reservoir"))
            .andExpect(jsonPath("$.status").value("OPEN"))
            .andReturn()
            .response
            .contentAsString

        val taskId = """"id":"([^"]+)"""".toRegex().find(createdTaskJson)?.groupValues?.get(1)
            ?: error("Expected task id in response")

        mockMvc.perform(get("/api/v1/ecosystems/$ecosystemId/tasks"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.length()").value(1))
            .andExpect(jsonPath("$[0].taskType").value("WATERING"))
            .andExpect(jsonPath("$[0].status").value("OPEN"))
            .andExpect(jsonPath("$[0].autoCreated").value(false))

        mockMvc.perform(
            org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch("/api/v1/ecosystems/$ecosystemId/tasks/$taskId/status")
                .contentType("application/json")
                .content("""{"status":"DONE"}""")
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.status").value("DONE"))

        check(maintenanceTaskRepository.count() == 1L)
    }

    @Test
    fun `summary moves to needs attention when there is an overdue open task`() {
        val ecosystemId = createEcosystem()

        addLog(
            ecosystemId = ecosystemId,
            payload = """
                {
                  "temperatureC": 22.5,
                  "humidityPercent": 61,
                  "eventType": "OBSERVATION",
                  "notes": "Healthy baseline"
                }
            """.trimIndent()
        )

        mockMvc.perform(
            post("/api/v1/ecosystems/$ecosystemId/tasks")
                .contentType("application/json")
                .content(
                    """
                        {
                          "title": "Inspect condensation level",
                          "taskType": "INSPECTION",
                          "dueDate": "2026-04-01"
                        }
                    """.trimIndent()
                )
        )
            .andExpect(status().isCreated)

        mockMvc.perform(get("/api/v1/ecosystems/$ecosystemId/summary"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.status").value("NEEDS_ATTENTION"))
            .andExpect(jsonPath("$.openTasks").value(1))
            .andExpect(jsonPath("$.overdueTasks").value(1))
    }

    @Test
    fun `maintenance task endpoint supports overdue filter`() {
        val ecosystemId = createEcosystem()

        mockMvc.perform(
            post("/api/v1/ecosystems/$ecosystemId/tasks")
                .contentType("application/json")
                .content("""{"title":"Past due inspection","taskType":"INSPECTION","dueDate":"2026-04-01"}""")
        ).andExpect(status().isCreated)

        mockMvc.perform(
            post("/api/v1/ecosystems/$ecosystemId/tasks")
                .contentType("application/json")
                .content("""{"title":"Future watering","taskType":"WATERING","dueDate":"2026-04-20"}""")
        ).andExpect(status().isCreated)

        mockMvc.perform(get("/api/v1/ecosystems/$ecosystemId/tasks?filter=OVERDUE"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.length()").value(1))
            .andExpect(jsonPath("$[0].title").value("Past due inspection"))

        mockMvc.perform(get("/api/v1/ecosystems/$ecosystemId/tasks?filter=OPEN"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.length()").value(2))
    }

    @Test
    fun `maintenance task endpoint supports dismissed filter`() {
        val ecosystemId = createEcosystem()

        addLog(
            ecosystemId = ecosystemId,
            payload = """
                {
                  "temperatureC": 24.1,
                  "humidityPercent": 55,
                  "eventType": "WATERING",
                  "notes": "Misted walls"
                }
            """.trimIndent()
        )

        val taskId = maintenanceTaskRepository.findAll().single().id ?: error("Expected generated task id")

        mockMvc.perform(
            org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch("/api/v1/ecosystems/$ecosystemId/tasks/$taskId/status")
                .contentType("application/json")
                .content("""{"status":"DISMISSED","dismissalReason":"ALREADY_HANDLED"}""")
        ).andExpect(status().isOk)

        mockMvc.perform(get("/api/v1/ecosystems/$ecosystemId/tasks?filter=DISMISSED"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.length()").value(1))
            .andExpect(jsonPath("$[0].status").value("DISMISSED"))
    }

    @Test
    fun `watering log creates follow up inspection task`() {
        val ecosystemId = createEcosystem()
        val expectedDueDate = LocalDate.now().plusDays(1).toString()

        addLog(
            ecosystemId = ecosystemId,
            payload = """
                {
                  "temperatureC": 24.1,
                  "humidityPercent": 55,
                  "eventType": "WATERING",
                  "notes": "Misted walls"
                }
            """.trimIndent()
        )

        mockMvc.perform(get("/api/v1/ecosystems/$ecosystemId/tasks"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.length()").value(1))
            .andExpect(jsonPath("$[0].taskType").value("INSPECTION"))
            .andExpect(jsonPath("$[0].title").value("Inspect moisture balance after watering"))
            .andExpect(jsonPath("$[0].status").value("OPEN"))
            .andExpect(jsonPath("$[0].autoCreated").value(true))
            .andExpect(jsonPath("$[0].dueDate").value(expectedDueDate))
    }

    @Test
    fun `repeated watering logs do not create duplicate follow up tasks`() {
        val ecosystemId = createEcosystem()

        addLog(
            ecosystemId = ecosystemId,
            payload = """
                {
                  "temperatureC": 23.8,
                  "humidityPercent": 57,
                  "eventType": "WATERING",
                  "notes": "Morning mist"
                }
            """.trimIndent()
        )

        addLog(
            ecosystemId = ecosystemId,
            payload = """
                {
                  "temperatureC": 24.0,
                  "humidityPercent": 56,
                  "eventType": "WATERING",
                  "notes": "Evening mist"
                }
            """.trimIndent()
        )

        mockMvc.perform(get("/api/v1/ecosystems/$ecosystemId/tasks?filter=OPEN"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.length()").value(1))
            .andExpect(jsonPath("$[0].taskType").value("INSPECTION"))

        check(maintenanceTaskRepository.count() == 1L)
    }

    @Test
    fun `feeding log creates follow up inspection task`() {
        val ecosystemId = createEcosystem()
        val expectedDueDate = LocalDate.now().plusDays(1).toString()

        addLog(
            ecosystemId = ecosystemId,
            payload = """
                {
                  "temperatureC": 23.5,
                  "humidityPercent": 57,
                  "eventType": "FEEDING",
                  "notes": "Protein meal"
                }
            """.trimIndent()
        )

        mockMvc.perform(get("/api/v1/ecosystems/$ecosystemId/tasks"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.length()").value(1))
            .andExpect(jsonPath("$[0].taskType").value("INSPECTION"))
            .andExpect(jsonPath("$[0].title").value("Log feeding response check"))
            .andExpect(jsonPath("$[0].autoCreated").value(true))
            .andExpect(jsonPath("$[0].dueDate").value(expectedDueDate))
    }

    @Test
    fun `suggested task can be dismissed without counting as open`() {
        val ecosystemId = createEcosystem()

        addLog(
            ecosystemId = ecosystemId,
            payload = """
                {
                  "temperatureC": 24.1,
                  "humidityPercent": 55,
                  "eventType": "WATERING",
                  "notes": "Misted walls"
                }
            """.trimIndent()
        )

        val taskId = maintenanceTaskRepository.findAll().single().id ?: error("Expected generated task id")

        mockMvc.perform(
            org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch("/api/v1/ecosystems/$ecosystemId/tasks/$taskId/status")
                .contentType("application/json")
                .content("""{"status":"DISMISSED","dismissalReason":"ALREADY_HANDLED"}""")
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.status").value("DISMISSED"))
            .andExpect(jsonPath("$.autoCreated").value(true))
            .andExpect(jsonPath("$.dismissalReason").value("ALREADY_HANDLED"))

        mockMvc.perform(get("/api/v1/ecosystems/$ecosystemId/tasks?filter=OPEN"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.length()").value(0))

        mockMvc.perform(get("/api/v1/ecosystems/$ecosystemId/tasks"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.length()").value(1))
            .andExpect(jsonPath("$[0].status").value("DISMISSED"))

        mockMvc.perform(get("/api/v1/ecosystems/$ecosystemId/summary"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.openTasks").value(0))
    }

    @Test
    fun `manual task cannot be dismissed`() {
        val ecosystemId = createEcosystem()

        val createdTaskJson = mockMvc.perform(
            post("/api/v1/ecosystems/$ecosystemId/tasks")
                .contentType("application/json")
                .content("""{"title":"Refill water reservoir","taskType":"WATERING","dueDate":"2026-04-10"}""")
        )
            .andExpect(status().isCreated)
            .andReturn()
            .response
            .contentAsString

        val taskId = """"id":"([^"]+)"""".toRegex().find(createdTaskJson)?.groupValues?.get(1)
            ?: error("Expected task id in response")

        mockMvc.perform(
            org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch("/api/v1/ecosystems/$ecosystemId/tasks/$taskId/status")
                .contentType("application/json")
                .content("""{"status":"DISMISSED","dismissalReason":"NOT_RELEVANT"}""")
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.message").value("Only suggested tasks can be dismissed"))
    }

    @Test
    fun `dismissed suggestion requires supported reason`() {
        val ecosystemId = createEcosystem()

        addLog(
            ecosystemId = ecosystemId,
            payload = """
                {
                  "temperatureC": 24.1,
                  "humidityPercent": 55,
                  "eventType": "WATERING",
                  "notes": "Misted walls"
                }
            """.trimIndent()
        )

        val taskId = maintenanceTaskRepository.findAll().single().id ?: error("Expected generated task id")

        mockMvc.perform(
            org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch("/api/v1/ecosystems/$ecosystemId/tasks/$taskId/status")
                .contentType("application/json")
                .content("""{"status":"DISMISSED"}""")
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.message").value("Dismissal reason is required for dismissed suggestions"))

        mockMvc.perform(
            org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch("/api/v1/ecosystems/$ecosystemId/tasks/$taskId/status")
                .contentType("application/json")
                .content("""{"status":"DISMISSED","dismissalReason":"SOMETHING_ELSE"}""")
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.message").value("Unsupported dismissal reason"))
    }

    @Test
    fun `too soon dismissal delays duplicate suggestion recreation`() {
        val ecosystemId = createEcosystem()

        addLog(
            ecosystemId = ecosystemId,
            payload = """
                {
                  "temperatureC": 24.1,
                  "humidityPercent": 55,
                  "eventType": "WATERING",
                  "notes": "Misted walls"
                }
            """.trimIndent()
        )

        val originalTask = maintenanceTaskRepository.findAll().single()
        val taskId = originalTask.id ?: error("Expected generated task id")

        mockMvc.perform(
            org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch("/api/v1/ecosystems/$ecosystemId/tasks/$taskId/status")
                .contentType("application/json")
                .content("""{"status":"DISMISSED","dismissalReason":"TOO_SOON"}""")
        )
            .andExpect(status().isOk)

        addLog(
            ecosystemId = ecosystemId,
            payload = """
                {
                  "temperatureC": 24.3,
                  "humidityPercent": 54,
                  "eventType": "WATERING",
                  "notes": "Follow-up mist"
                }
            """.trimIndent()
        )

        check(maintenanceTaskRepository.count() == 1L)
        mockMvc.perform(get("/api/v1/ecosystems/$ecosystemId/tasks?filter=OPEN"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.length()").value(0))
    }

    @Test
    fun `expired dismissal cooldown allows suggestion to reappear`() {
        val ecosystemId = createEcosystem()

        addLog(
            ecosystemId = ecosystemId,
            payload = """
                {
                  "temperatureC": 23.5,
                  "humidityPercent": 57,
                  "eventType": "FEEDING",
                  "notes": "Protein meal"
                }
            """.trimIndent()
        )

        val originalTask = maintenanceTaskRepository.findAll().single()
        val taskId = originalTask.id ?: error("Expected generated task id")

        mockMvc.perform(
            org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch("/api/v1/ecosystems/$ecosystemId/tasks/$taskId/status")
                .contentType("application/json")
                .content("""{"status":"DISMISSED","dismissalReason":"ALREADY_HANDLED"}""")
        )
            .andExpect(status().isOk)

        maintenanceTaskRepository.deleteAll()
        maintenanceTaskRepository.save(
            com.example.api.model.MaintenanceTask(
                ecosystem = originalTask.ecosystem,
                title = originalTask.title,
                taskType = originalTask.taskType,
                dueDate = originalTask.dueDate,
                status = "DISMISSED",
                autoCreated = true,
                dismissalReason = "ALREADY_HANDLED",
                createdAt = LocalDateTime.now().minusDays(8)
            )
        )

        addLog(
            ecosystemId = ecosystemId,
            payload = """
                {
                  "temperatureC": 23.2,
                  "humidityPercent": 58,
                  "eventType": "FEEDING",
                  "notes": "Another meal"
                }
            """.trimIndent()
        )

        check(maintenanceTaskRepository.count() == 2L)
        mockMvc.perform(get("/api/v1/ecosystems/$ecosystemId/tasks?filter=OPEN"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.length()").value(1))
            .andExpect(jsonPath("$[0].status").value("OPEN"))
    }

    /**
     * Creates a persisted ecosystem fixture and returns its generated identifier.
     */
    private fun createEcosystem(): java.util.UUID {
        val ecosystem = ecosystemRepository.save(
            com.example.api.model.Ecosystem(
                name = "Ant Colony Lab",
                type = "FORMICARIUM",
                description = "Test fixture ecosystem"
            )
        )

        return ecosystem.id ?: error("Expected generated ecosystem id")
    }

    /**
     * Posts a log payload for the supplied ecosystem and expects successful creation.
     */
    private fun addLog(ecosystemId: java.util.UUID, payload: String) {
        mockMvc.perform(
            post("/api/v1/ecosystems/$ecosystemId/logs")
                .contentType("application/json")
                .content(payload)
        )
            .andExpect(status().isCreated)
    }
}
