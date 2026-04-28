package com.example.api.service

import com.example.api.model.Ecosystem
import com.example.api.repository.EcosystemLogRepository
import com.example.api.repository.EcosystemRepository
import com.example.api.repository.MaintenanceTaskRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito
import java.time.LocalDateTime
import java.util.UUID

class EcosystemServiceTest {

    private val ecosystemRepository = Mockito.mock(EcosystemRepository::class.java)
    private val ecosystemLogRepository = Mockito.mock(EcosystemLogRepository::class.java)
    private val maintenanceTaskRepository = Mockito.mock(MaintenanceTaskRepository::class.java)
    private val authService = Mockito.mock(AuthService::class.java)

    private val service = EcosystemService(
        ecosystemRepository = ecosystemRepository,
        ecosystemLogRepository = ecosystemLogRepository,
        maintenanceTaskRepository = maintenanceTaskRepository,
        authService = authService
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
}
