package com.example.api.controller

import com.example.api.dto.CreateEcosystemRequest
import com.example.api.dto.EcosystemResponse
import com.example.api.dto.EcosystemWorkspaceCardResponse
import com.example.api.dto.EcosystemWorkspaceOverviewResponse
import com.example.api.dto.PagedResponse
import com.example.api.exception.GlobalExceptionHandler
import com.example.api.service.EcosystemService
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean
import org.springframework.web.server.ResponseStatusException
import java.time.LocalDateTime
import java.util.UUID

/**
 * Covers controller-level request validation and error handling for ecosystem endpoints.
 */
class EcosystemControllerTest {

    private lateinit var ecosystemService: EcosystemService
    private lateinit var mockMvc: MockMvc

    /**
     * Builds a standalone MockMvc setup around the ecosystem controller.
     */
    @BeforeEach
    fun setUp() {
        ecosystemService = Mockito.mock(EcosystemService::class.java)

        val validator = LocalValidatorFactoryBean()
        validator.afterPropertiesSet()

        mockMvc = MockMvcBuilders
            .standaloneSetup(EcosystemController(ecosystemService))
            .setControllerAdvice(GlobalExceptionHandler())
            .setValidator(validator)
            .build()
    }

    @Test
    fun `create ecosystem returns 400 with validation details when payload is invalid`() {
        val request = CreateEcosystemRequest(
            name = " ",
            type = "",
            description = "x".repeat(501)
        )
        val requestJson = """
            {
              "name": "${request.name}",
              "type": "${request.type}",
              "description": "${request.description}"
            }
        """.trimIndent()

        mockMvc.perform(
            post("/api/v1/ecosystems")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson)
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.message").value("Validation failed"))
            .andExpect(jsonPath("$.path").value("/api/v1/ecosystems"))
            .andExpect(jsonPath("$.validationErrors.name").exists())
            .andExpect(jsonPath("$.validationErrors.type").exists())
            .andExpect(jsonPath("$.validationErrors.description").exists())
    }

    @Test
    fun `delete ecosystem returns 404 with standard error body when ecosystem does not exist`() {
        val ecosystemId = UUID.randomUUID()

        Mockito.doThrow(ResponseStatusException(HttpStatus.NOT_FOUND, "Ecosystem not found"))
            .`when`(ecosystemService)
            .deleteEcosystem(ecosystemId)

        mockMvc.perform(delete("/api/v1/ecosystems/$ecosystemId"))
            .andExpect(status().isNotFound)
            .andExpect(jsonPath("$.status").value(404))
            .andExpect(jsonPath("$.error").value("Not Found"))
            .andExpect(jsonPath("$.message").value("Ecosystem not found"))
            .andExpect(jsonPath("$.path").value("/api/v1/ecosystems/$ecosystemId"))
    }

    @Test
    fun `create ecosystem returns created response when payload is valid`() {
        val request = CreateEcosystemRequest(
            name = "Rainforest Terrarium",
            type = "FLORARIUM",
            description = "Tropical glass setup"
        )
        val response = EcosystemResponse(
            id = UUID.randomUUID(),
            name = request.name,
            type = request.type,
            description = request.description,
            createdByUsername = null,
            createdByDisplayName = null,
            createdAt = LocalDateTime.now()
        )
        val requestJson = """
            {
              "name": "${request.name}",
              "type": "${request.type}",
              "description": "${request.description}"
            }
        """.trimIndent()

        Mockito.`when`(ecosystemService.createEcosystem(null, request)).thenReturn(response)

        mockMvc.perform(
            post("/api/v1/ecosystems")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson)
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.name").value("Rainforest Terrarium"))
            .andExpect(jsonPath("$.type").value("FLORARIUM"))
    }

    @Test
    fun `workspace cards endpoint returns enriched ecosystem cards`() {
        val workspaceCard = EcosystemWorkspaceCardResponse(
            id = UUID.randomUUID(),
            name = "Rainforest Terrarium",
            type = "FLORARIUM",
            description = "Tropical glass setup",
            status = "STABLE",
            lastRecordedAt = LocalDateTime.now(),
            logsLast7Days = 4,
            openTasks = 2,
            overdueTasks = 1,
            createdAt = LocalDateTime.now().minusDays(1)
        )
        val pagedResponse = PagedResponse(
            page = 0,
            size = 9,
            totalElements = 1,
            totalPages = 1,
            hasNext = false,
            hasPrevious = false,
            items = listOf(workspaceCard)
        )

        Mockito.`when`(ecosystemService.getWorkspaceCards(null, null, null, 0, 9)).thenReturn(pagedResponse)

        mockMvc.perform(get("/api/v1/ecosystems/cards"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.items[0].name").value("Rainforest Terrarium"))
            .andExpect(jsonPath("$.items[0].status").value("STABLE"))
            .andExpect(jsonPath("$.items[0].logsLast7Days").value(4))
            .andExpect(jsonPath("$.items[0].overdueTasks").value(1))
            .andExpect(jsonPath("$.totalElements").value(1))
            .andExpect(jsonPath("$.hasNext").value(false))
    }

    @Test
    fun `workspace overview endpoint returns aggregated counters`() {
        val overview = EcosystemWorkspaceOverviewResponse(
            totalEcosystems = 8,
            needsAttention = 2,
            stable = 4,
            noRecentData = 2,
            openTasks = 11,
            overdueTasks = 3
        )

        Mockito.`when`(ecosystemService.getWorkspaceOverview(null, null)).thenReturn(overview)

        mockMvc.perform(get("/api/v1/ecosystems/overview"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.totalEcosystems").value(8))
            .andExpect(jsonPath("$.needsAttention").value(2))
            .andExpect(jsonPath("$.stable").value(4))
            .andExpect(jsonPath("$.openTasks").value(11))
            .andExpect(jsonPath("$.overdueTasks").value(3))
    }

    @Test
    fun `workspace cards endpoint forwards search status and sort filters`() {
        Mockito.`when`(
            ecosystemService.getWorkspaceCards("fern", "STABLE", "NAME", 2, 6)
        ).thenReturn(
            PagedResponse(
                page = 2,
                size = 6,
                totalElements = 0,
                totalPages = 0,
                hasNext = false,
                hasPrevious = true,
                items = emptyList()
            )
        )

        mockMvc.perform(get("/api/v1/ecosystems/cards?search=fern&status=STABLE&sort=NAME&page=2&size=6"))
            .andExpect(status().isOk)

        Mockito.verify(ecosystemService).getWorkspaceCards("fern", "STABLE", "NAME", 2, 6)
    }
}
