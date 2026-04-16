package com.example.api.controller

import com.example.api.dto.CreateEcosystemRequest
import com.example.api.dto.EcosystemResponse
import com.example.api.dto.EcosystemSummaryResponse
import com.example.api.dto.EcosystemWorkspaceCardResponse
import com.example.api.dto.EcosystemWorkspaceOverviewResponse
import com.example.api.dto.PagedResponse
import com.example.api.dto.UpdateEcosystemRequest
import com.example.api.service.EcosystemService
import jakarta.validation.Valid
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

/**
 * Implements the ecosystem endpoint contract and delegates requests to [EcosystemService].
 */
@RestController
class EcosystemController(
    private val ecosystemService: EcosystemService
) : EcosystemControllerApi {

    /**
     * Creates a new ecosystem from the request payload.
     */
    override fun createEcosystem(authentication: Authentication?, @Valid request: CreateEcosystemRequest): EcosystemResponse =
        ecosystemService.createEcosystem(authentication?.name, request)

    /**
     * Updates an existing ecosystem from the request payload.
     */
    override fun updateEcosystem(
        id: UUID,
        @Valid request: UpdateEcosystemRequest
    ): EcosystemResponse = ecosystemService.updateEcosystem(id, request)

    /**
     * Returns all ecosystems currently tracked by the application.
     */
    override fun getAllEcosystems(): List<EcosystemResponse> = ecosystemService.getAllEcosystems()

    /**
     * Returns enriched ecosystem cards for the workspace home page.
     */
    override fun getWorkspaceCards(
        search: String?,
        status: String?,
        sort: String?,
        page: Int,
        size: Int
    ): PagedResponse<EcosystemWorkspaceCardResponse> = ecosystemService.getWorkspaceCards(search, status, sort, page, size)

    /**
     * Returns aggregated workspace counters for the home page overview.
     */
    override fun getWorkspaceOverview(
        search: String?,
        status: String?
    ): EcosystemWorkspaceOverviewResponse = ecosystemService.getWorkspaceOverview(search, status)

    /**
     * Returns a single ecosystem identified by its id.
     */
    override fun getEcosystem(
        id: UUID
    ): EcosystemResponse = ecosystemService.getEcosystem(id)

    /**
     * Returns a compact dashboard summary for the selected ecosystem.
     */
    override fun getEcosystemSummary(
        id: UUID
    ): EcosystemSummaryResponse = ecosystemService.getEcosystemSummary(id)

    /**
     * Deletes the selected ecosystem together with its dependent data.
     */
    override fun deleteEcosystem(id: UUID) = ecosystemService.deleteEcosystem(id)
}
