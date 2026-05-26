package com.example.api.service

import com.example.api.model.Ecosystem
import com.example.api.repository.EcosystemLogRepository
import com.example.api.repository.EcosystemRepository
import com.example.api.repository.MaintenanceTaskRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito
import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException
import java.time.LocalDateTime
import java.util.UUID

class EcosystemServiceTest {

    private val ecosystemRepository = Mockito.mock(EcosystemRepository::class.java)
    private val ecosystemLogRepository = Mockito.mock(EcosystemLogRepository::class.java)
    private val maintenanceTaskRepository = Mockito.mock(MaintenanceTaskRepository::class.java)
    private val authService = Mockito.mock(AuthService::class.java)
    private val auditLogService = Mockito.mock(AuditLogService::class.java)

    private val service = EcosystemService(
        ecosystemRepository = ecosystemRepository,
        ecosystemLogRepository = ecosystemLogRepository,
        maintenanceTaskRepository = maintenanceTaskRepository,
        authService = authService,
        auditLogService = auditLogService
    )

    @Test
    fun `workspace cards keep loading when ecosystem createdAt is missing`() {
        val olderId = UUID.randomUUID()
        val newerId = UUID.randomUUID()

        Mockito.`when`(ecosystemRepository.findAll()).thenReturn(
            listOf(
                Ecosystem(
                    id = olderId,
                    name = "Legacy setup",
                    type = "FLORARIUM",
                    description = "Imported from an older local database",
                    createdAt = null
                ),
                Ecosystem(
                    id = newerId,
                    name = "Fresh setup",
                    type = "FLORARIUM",
                    description = "Recently created",
                    createdAt = LocalDateTime.now()
                )
            )
        )
        Mockito.`when`(ecosystemLogRepository.findLatestLogSnapshots()).thenReturn(emptyList())
        Mockito.`when`(
            ecosystemLogRepository.countLogsByEcosystemRecordedAfter(any(LocalDateTime::class.java) ?: LocalDateTime.MIN)
        ).thenReturn(emptyList())
        Mockito.`when`(
            maintenanceTaskRepository.findTaskCountsByEcosystem(any(java.time.LocalDate::class.java) ?: java.time.LocalDate.MIN)
        ).thenReturn(emptyList())

        val cards = service.getWorkspaceCards(sort = "NEWEST").items

        assertEquals(2, cards.size)
        assertEquals(newerId, cards[0].id)
        assertEquals(olderId, cards[1].id)
        assertEquals(null, cards[1].createdAt)
    }

    @Test
    fun `create ecosystem rejects duplicate name regardless of case`() {
        val existing = Ecosystem(
            id = UUID.randomUUID(),
            name = "Rainforest Capsule",
            type = "FLORARIUM",
            description = "Existing fixture"
        )
        Mockito.`when`(authService.resolveActorSnapshot(null))
            .thenReturn(AuthService.ActorSnapshot(user = null, username = null, displayName = null))
        Mockito.`when`(ecosystemRepository.findByNameIgnoreCase("Rainforest Capsule")).thenReturn(existing)

        val exception = assertThrows(ResponseStatusException::class.java) {
            service.createEcosystem(
                null,
                com.example.api.dto.CreateEcosystemRequest(
                    name = "Rainforest Capsule",
                    type = "FLORARIUM",
                    description = "New fixture"
                )
            )
        }

        assertEquals(HttpStatus.CONFLICT, exception.statusCode)
        assertEquals("Ecosystem name already exists", exception.reason)
    }
}
